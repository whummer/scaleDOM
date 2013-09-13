package at.ac.tuwien.dsg.scaledom.dom;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.XMLConstants;
import javax.xml.stream.events.Namespace;

import org.apache.xerces.dom.ChildNode;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.dom.ParentNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import at.ac.tuwien.dsg.scaledom.ScaleDomDocumentSource;
import at.ac.tuwien.dsg.scaledom.io.NodeLocation;
import at.ac.tuwien.dsg.scaledom.io.ReaderFactory;
import at.ac.tuwien.dsg.scaledom.lazy.LazyLoadingStrategy;
import at.ac.tuwien.dsg.scaledom.parser.XmlParser;
import at.ac.tuwien.dsg.scaledom.util.ComponentFactory;
import at.ac.tuwien.dsg.scaledom.util.CompositeReader;
import at.ac.tuwien.dsg.scaledom.util.DOMUtils;
import at.ac.tuwien.dsg.scaledom.util.ReferenceQueueLogThread;

/**
 * TODO: Class documentation.
 * 
 * @author Dominik Rauch
 */
public class ScaleDomDocument extends DocumentImpl implements ScaleDomDocumentInterface {

	/** Serialization version. */
	private final static long serialVersionUID = -6590166195993960408L;

	/** Logger. */
	private final static Logger log = LoggerFactory.getLogger(ScaleDomDocument.class);

	/** Log message template for unloaded objects. */
	private final static String UNLOAD_LOG_MESSAGE = "Children of '%s' have been unloaded.";

	/** Underlying source. */
	private final ScaleDomDocumentSource source;
	/** Reusable XmlParser. */
	private final XmlParser parser;
	/** Reusable ComponentFactory. */
	private final ComponentFactory componentFactory;
	/** Reusable ReaderFactory. */
	private final ReaderFactory readerFactory;

	/**
	 * Strong references to children lists which should never be unloaded:
	 * <ul>
	 * <li>Root level: the level of the XML's document element.</li>
	 * <li>All levels with modified nodes.</li>
	 * </ul>
	 */
	private final List<LinkedList<ChildNode>> persistentChildrenLists;
	/** Reference queue for logging unloaded children. */
	private final ReferenceQueue<LinkedList<ChildNode>> unloadQueue;

	/**
	 * Flag whether the document is consistent, if not, some requested children have not been loaded (e.g. due to memory
	 * shortage) and the DOM is incomplete.
	 */
	private boolean consistent;

	/** Current load type. */
	private LoadType loadType;
	/** Flag whether the document is currently loading nodes. */
	private boolean isLoading;

	/**
	 * Default constructor.
	 * 
	 * @param source the underlying document source.
	 * @param parser a reusable XmlParser.
	 * @param componentFactory a factory for creating required components.
	 * @throws InstantiationException If the component factory failed to instantiate necessary components.
	 */
	public ScaleDomDocument(final ScaleDomDocumentSource source, final XmlParser parser,
			final ComponentFactory componentFactory) throws InstantiationException {
		this.source = source;
		this.parser = parser;
		this.componentFactory = componentFactory;
		this.readerFactory = componentFactory.getNewInstance(ReaderFactory.class, source);
		this.persistentChildrenLists = new ArrayList<LinkedList<ChildNode>>();
		this.unloadQueue = new ReferenceQueue<LinkedList<ChildNode>>();
		consistent = true;

		// Start reference queue log thread
		final Thread unloadQueueLogThread = new ReferenceQueueLogThread(unloadQueue, UNLOAD_LOG_MESSAGE);
		// TODO: Exit thread when ScaleDomDocumentImpl is garbage collected instead of at the end of the application
		unloadQueueLogThread.setDaemon(true);
		unloadQueueLogThread.start();

		// Do initial loading
		loadType = LoadType.INITIAL;
		isLoading = true;
		initialLoad();
		isLoading = false;
		loadType = LoadType.RELOAD;
	}

	/**
	 * Loads an initial part of the DOM into memory. The configured LazyLoadingStrategy decides how many nodes are
	 * loaded, however, it is guaranteed that at least everything on the level of the XML's document element or above is
	 * loaded (and kept loaded).
	 * 
	 * @throws InstantiationException If the component factory failed to instantiate necessary components.
	 */
	private void initialLoad() throws InstantiationException {
		log.debug("Initial loading...");

		// Without holding a strong reference to the direct children, they could be removed immediately, before they
		// are flagged as persistent.
		final LinkedList<ChildNode> preventReferenceRemoval = this.getLoadedChildNodes();

		Reader reader = null;
		try {
			reader = readerFactory.newReader();
			final LazyLoadingStrategy strategy = componentFactory.getNewInstance(LazyLoadingStrategy.class, 0);
			LoadProcess process = null;
			try {
				process = new LoadProcess(this, this, loadType, strategy, readerFactory);
				final boolean ok = parser.parse(reader, process);
				if (!ok) {
					setInconsistent();
				}

				log.debug("Finished initial loading, created " + process.getNumberOfCreatedNodes() + " nodes.");
			} catch (final SAXException ex) {
				log.error("Could not load nodes due to parser exception.", ex);
				setInconsistent();
			} finally {
				if(process != null) {
					try {
						process.close();
					} catch (IOException e) { }
				}
			}
		} catch (final IOException ex) {
			log.error("Could not load nodes due to I/O exception.", ex);
			setInconsistent();
		} finally {
			// Mark all direct children of the document as persistent (= all nodes on the root level)
			persistentChildrenLists.add(preventReferenceRemoval);

			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) { }
			}

			log.debug(preventReferenceRemoval.size() + " nodes have been flagged as persistent.");
		}
	}

	/**
	 * Loads more child nodes, starting at the given parent node. Again, the configured LazyLoadingStrategy decides how
	 * many nodes are actually loaded, however, it is guaranteed that at least the direct children of parent node are
	 * loaded.
	 * 
	 * If loading fails (e.g. due to memory restrictions), an error is logged and the DOM remains incomplete.
	 * 
	 * @param parent the parent node.
	 */
	public void load(final ParentNode parent) {
		log.debug("Node '" + parent.getNodeName() + "' requested a reload of its child nodes...");

		if (loadType == LoadType.RELOAD_NOTHING) {
			log.debug("Current state ReloadState.RELOAD_NOTHING prevented reloading.");
			return;
		}

		isLoading = true;

		/**
		 * Hint: A strong reference to the direct children of parent is not required here, it is hold by the caller of
		 * {@link #load(ParentNode)} - see {@link ParentNode#getChildren(boolean)}.
		 */
		final NodeLocation location = parent.getNodeLocation();
		Reader readerForLocation = null;
		try {
			readerForLocation = readerFactory.newReaderForLocation(location);

			// Build fake root element containing all required namespace declarations
			final String fakeElementStart = "<ScaleDOM " + buildNamespaceDeclarations(parent) + ">";
			final String fakeElementEnd = "</ScaleDOM>";
			final long additionalOffset = fakeElementStart.length();

			final LazyLoadingStrategy strategy = componentFactory.getNewInstance(LazyLoadingStrategy.class,
					DOMUtils.getAbsoluteLevel(parent));
			final int elementsToSkip = 3; // StartDocument, StartElement for fakeElementStart, StartElement for parent
			
			LoadProcess process = null;
			try {
				process = new LoadProcess(this, parent, loadType, strategy, 
						readerFactory, elementsToSkip, additionalOffset);

				final Reader reader = new CompositeReader(new StringReader(fakeElementStart), readerForLocation,
						new StringReader(fakeElementEnd));
				final boolean ok = parser.parse(reader, process);
				if (!ok) {
					setInconsistent();
				}

				log.debug("Finished loading, created " + process.getNumberOfCreatedNodes() + " nodes.");
			} catch (final SAXException ex) {
				log.error("Could not load nodes due to parser exception.", ex);
				setInconsistent();
			} finally {
				if(process != null) {
					process.close();
				}
			}
		} catch (final IOException ex) {
			log.error("Could not load nodes due to I/O exception.", ex);
			setInconsistent();
		} catch (final InstantiationException ex) {
			log.error("Could not instantiate required components for the load process.", ex);
			setInconsistent();
		} finally {
			if(readerForLocation != null) {
				try {
					readerForLocation.close();
				} catch (IOException e) { }
			}
		}

		isLoading = false;
	}

	/**
	 * Returns whether the document is currently loading nodes.
	 * 
	 * @return true if the document is currently loading nodes, false otherwise.
	 */
	public boolean isLoading() {
		return isLoading;
	}

	private String buildNamespaceDeclarations(ParentNode parent) {
		final Map<String, String> namespaces = new HashMap<String, String>();

		for (parent = (ParentNode) parent.getParentNode(); parent != null; parent = (ParentNode) parent.getParentNode()) {
			// Only Element nodes may have namespace declarations on them
			if (!(parent instanceof Element)) {
				continue;
			}

			final List<Namespace> declaredNamespaces = parent.getDeclaredNamespaces();
			for (final Namespace declaredNamespace : declaredNamespaces) {
				final String key = declaredNamespace.getPrefix();
				if (namespaces.containsKey(key)) {
					continue; // NS has been redefined further down the tree
				}

				namespaces.put(key, declaredNamespace.getNamespaceURI());
			}
		}

		// Build namespace string
		final StringBuilder sb = new StringBuilder();
		for (final Entry<String, String> e : namespaces.entrySet()) {
			sb.append(XMLConstants.XMLNS_ATTRIBUTE);
			if (!e.getKey().isEmpty()) {
				sb.append(':');
				sb.append(e.getKey());
			}
			sb.append("=\"");
			sb.append(e.getValue());
			sb.append("\" ");
		}

		final String namespaceDeclarations = sb.toString();
		log.debug("Built namespace declaration string: " + namespaceDeclarations);
		return namespaceDeclarations;
	}

	private void setInconsistent() {
		consistent = false;
		log.error("Due to previous errors the document is not consistent anymore!");
	}

	@Override
	public ScaleDomDocumentSource getDocumentSource() {
		return source;
	}

	@Override
	public boolean isConsistent() {
		return consistent;
	}

	@Override
	public LoadType getLoadType() {
		return loadType;
	}

	@Override
	public void setLoadType(final LoadType loadType) {
		checkNotNull(loadType, "Argument loadType must not be null.");
		checkArgument(!(loadType == LoadType.INITIAL), "Argument loadType must not be LoadType.INITIAL.");
		this.loadType = loadType;
	}

	/**
	 * Returns the unload queue, such that the <code>WeakChildNodeList</code>-<code>SoftReference</code> can register to
	 * it.
	 * 
	 * @return returns the unload queue.
	 */
	public ReferenceQueue<LinkedList<ChildNode>> getUnloadQueue() {
		return unloadQueue;
	}

	/**
	 * <code>ParentNode</code> notifies us of modified children lists.
	 * 
	 * @param children the modified children list.
	 */
	public void modified(final LinkedList<ChildNode> children) {
		if (!isLoading) {
			// Unreloadable DOM modification by user -> persist children list
			persistentChildrenLists.add(children);
		}
	}
}
