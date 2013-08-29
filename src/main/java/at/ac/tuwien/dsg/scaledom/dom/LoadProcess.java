package at.ac.tuwien.dsg.scaledom.dom;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.xerces.dom.ParentNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import at.ac.tuwien.dsg.scaledom.io.NodeLocation;
import at.ac.tuwien.dsg.scaledom.io.NodeLocationFactory;
import at.ac.tuwien.dsg.scaledom.lazy.LazyLoadingStrategy;
import at.ac.tuwien.dsg.scaledom.parser.XmlEventLocation;
import at.ac.tuwien.dsg.scaledom.parser.XmlParserEventListener;
import at.ac.tuwien.dsg.scaledom.util.LowMemoryDetector;

public class LoadProcess implements XmlParserEventListener, Closeable {

	/** Logger. */
	private final static Logger log = LoggerFactory.getLogger(LoadProcess.class);

	private final ScaleDomDocument doc;
	private final LoadType loadType;
	private final LazyLoadingStrategy strategy;
	private final NodeLocationFactory nodeLocationFactory;
	private final long additionalOffset;

	/** Current state, outsourced to own class. */
	private final LoadProcessState state;

	/** Low memory detector. */
	private final LowMemoryDetector lowMemoryDetector;

	public LoadProcess(final ScaleDomDocument doc, final ParentNode parent, final LoadType loadType,
			final LazyLoadingStrategy strategy, final NodeLocationFactory nodeLocationFactory) {
		this(doc, parent, loadType, strategy, nodeLocationFactory, 0, 0);
	}

	public LoadProcess(final ScaleDomDocument doc, final ParentNode parent, final LoadType loadType,
			final LazyLoadingStrategy strategy, final NodeLocationFactory nodeLocationFactory, final int eventsToSkip,
			final long additionalOffset) {
		this.doc = doc;
		this.loadType = loadType;
		this.nodeLocationFactory = nodeLocationFactory;
		this.strategy = strategy;
		this.additionalOffset = additionalOffset;

		state = new LoadProcessState(parent, eventsToSkip);

		// Use a low memory detector during load process
		lowMemoryDetector = new LowMemoryDetector(0.99);
	}

	/**
	 * Returns the number of created nodes.
	 * 
	 * @return the number of created nodes.
	 */
	public long getNumberOfCreatedNodes() {
		return state.getNumberOfCreatedNodes();
	}

	@Override
	public void close() throws IOException {
		// Remove low memory detector after loading is done
		lowMemoryDetector.close();
	}

	@Override
	public boolean process(final XMLEvent event, final XmlEventLocation eventLocation) {
		// The parser input may include additional nodes in the beginning (e.g. the parent node of the children to be
		// loaded, a root node with namespace declarations, etc.), skip those
		if (state.shouldSkipEvent()) {
			state.skippedEvent();
			return CONTINUE_PARSING;
		}

		// Convert the parser's outputted location into a node location
		final NodeLocation location = nodeLocationFactory.eventLocationToNodeLocation(eventLocation, additionalOffset);

		// The location of the event following an EndElement event, is the end location for the previous element node
		if (state.getLastEventType() == XMLStreamConstants.END_ELEMENT) {
			final ParentNode node = state.getPreviousParentNode();
			node.getNodeLocation().setEndLocation(location);
		}

		// Filter uninteresting events
		if (filter(event)) {
			return CONTINUE_PARSING;
		}

		// Inform the parent node of existing children - regardless of whether the child is loaded in the end or not
		if (isNodeCreatingEvent(event)) {
			state.getCurrentParentNode().parsedChild();
		}

		// Let the LazyLoadingStrategy decide whether event should be processed (and may result in a node)
		if (!strategy.shouldLoad(event, eventLocation)) {
			return CONTINUE_PARSING;
		}

		state.setLastEventType(event.getEventType());

		// Process events which do not result in a node
		if (event.getEventType() == XMLStreamConstants.START_DOCUMENT) {
			processStartDocument((StartDocument) event);
			return CONTINUE_PARSING;
		} else if (event.getEventType() == XMLStreamConstants.END_ELEMENT) {
			processEndElement(event.asEndElement());
			return CONTINUE_PARSING;
		}

		// Check the load type, whether a node should be created
		if (loadType == LoadType.RELOAD_ELEMENTS_ONLY && event.getEventType() != XMLStreamConstants.START_ELEMENT) {
			return CONTINUE_PARSING;
		}

		// Check if we have enough memory to create more nodes
		if (lowMemoryDetector.isLowMemory()) {
			final Node node = state.getCurrentParentNode();
			if (node != null) {
				// log.error("Reached memory threshold, stopped loading children of '"
				// + state.getCurrentParentNode().getNodeName() + "' to prevent OutOfMemoryError!");
			} else {
				log.error("Reached memory threshold, stopped loading.");
			}

			// TODO: Return ABORT_PARSING after LowMemoryDetector is properly working.
			// return ABORT_PARSING;
		}

		// Process events which result in a node
		switch (event.getEventType()) {
		case XMLStreamConstants.START_ELEMENT:
			processStartElement(event.asStartElement(), location);
			break;
		case XMLStreamConstants.CHARACTERS:
			final Characters text = event.asCharacters();
			processText(text, location);
			break;
		case XMLStreamConstants.CDATA:
			final Characters cdata = event.asCharacters();
			processCData(cdata, location);
			break;
		case XMLStreamConstants.COMMENT:
			final Comment comment = (Comment) event;
			processComment(comment, location);
			break;
		case XMLStreamConstants.PROCESSING_INSTRUCTION:
			final ProcessingInstruction processingInstruction = (ProcessingInstruction) event;
			processProcessingInstruction(processingInstruction, location);
			break;
		default:
			log.error("An XMLEvent of unknown type '" + event.getEventType() + "' did occur.");
			break;
		}

		return CONTINUE_PARSING;
	}

	private void processStartDocument(final StartDocument event) {
		doc.setDocumentURI(event.getSystemId());
		doc.setXmlStandalone(event.isStandalone());
		doc.setXmlVersion(event.getVersion());
	}

	private void processStartElement(final StartElement startElement, final NodeLocation location) {
		final QName name = startElement.getName();
		final Iterator<?> attributes = startElement.getAttributes();

		// Create Element node
		// TODO: Use createElement() if no namespace is present?
		final Element elementNode = doc.createElementNS(name.getNamespaceURI(), name.toString(), name.getLocalPart());
		final ParentNode asParentNode = (ParentNode) elementNode;
		asParentNode.setNodeLocation(location);

		// Add attributes
		while (attributes.hasNext()) {
			final Attribute attribute = (Attribute) attributes.next();

			// Create Attr node
			// TODO: Use createAttributeNS() if namespace is present?
			final Attr attrNode = doc.createAttribute(attribute.getName().getLocalPart());
			attrNode.setValue(attribute.getValue());

			elementNode.setAttributeNode(attrNode);
		}

		// Cache namespaces
		final Iterator<?> namespaces = startElement.getNamespaces();
		while (namespaces.hasNext()) {
			final Namespace namespace = (Namespace) namespaces.next();
			asParentNode.parsedNamespace(namespace);
		}

		// Update current hierarchy
		state.getCurrentParentNode().appendChild(elementNode);
		state.addParentNodeAtBottom(asParentNode);
		state.createdNode();
	}

	private void processEndElement(final EndElement endElement) {
		// Update current hierarchy
		state.removeCurrentParentNode();
	}

	private void processText(final Characters text, final NodeLocation location) {
		// Create and append Text node
		final Text textNode = doc.createTextNode(text.getData());
		state.getCurrentParentNode().appendChild(textNode);
		state.createdNode();
	}

	private void processCData(final Characters cdata, final NodeLocation location) {
		// Create and append CDATASection node
		final CDATASection cdataSectionNode = doc.createCDATASection(cdata.getData());
		state.getCurrentParentNode().appendChild(cdataSectionNode);
		state.createdNode();
	}

	private void processComment(final Comment comment, final NodeLocation location) {
		// Create and append Comment node
		final org.w3c.dom.Comment commentNode = doc.createComment(comment.getText());
		state.getCurrentParentNode().appendChild(commentNode);
		state.createdNode();
	}

	private void processProcessingInstruction(final ProcessingInstruction processingInstruction,
			final NodeLocation location) {
		// Create and append ProcessingInstruction node
		final org.w3c.dom.ProcessingInstruction processingInstructionNode = doc.createProcessingInstruction(
				processingInstruction.getTarget(), processingInstruction.getData());
		state.getCurrentParentNode().appendChild(processingInstructionNode);
		state.createdNode();
	}

	private boolean filter(final XMLEvent event) {
		switch (event.getEventType()) {
		// The following event types are not interesting for ScaleDOM:
		case XMLStreamConstants.END_DOCUMENT:
			return true;

			// The following event types are not supported by ScaleDOM:
		case XMLStreamConstants.DTD:
			log.debug("An XMLEvent of type 'DTD' has been ignored.");
			return true;

		case XMLStreamConstants.ENTITY_REFERENCE:
			log.warn("An XMLEvent of type 'ENTITY_REFERENCE' has been ignored, please instruct the parser to expand entity references.");
			return true;

			// The following event types do not occur as first-order events when parsing a document source:
		case XMLStreamConstants.ATTRIBUTE:
		case XMLStreamConstants.ENTITY_DECLARATION:
		case XMLStreamConstants.NAMESPACE:
		case XMLStreamConstants.NOTATION_DECLARATION:
			log.error("An XMLEvent of type '" + event.getEventType() + "' did occur as first-order event.");
			return true;

			// The following event types do not occur at all
			// TODO: Check http://stackoverflow.com/questions/15010864/
		case XMLStreamConstants.SPACE:
			log.error("An XMLEvent of type 'SPACE' did occur.");
			return true;
		}

		return false;
	}

	private boolean isNodeCreatingEvent(final XMLEvent event) {
		final int eventType = event.getEventType();
		return eventType == XMLStreamConstants.START_ELEMENT || eventType == XMLStreamConstants.CHARACTERS
				|| eventType == XMLStreamConstants.CDATA || eventType == XMLStreamConstants.COMMENT
				|| eventType == XMLStreamConstants.PROCESSING_INSTRUCTION;
	}
}
