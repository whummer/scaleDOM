package at.ac.tuwien.dsg.scaledom.test.perf.util.dom;

import org.apache.xerces.dom.ParentNode;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This utility class traverses a DOM tree, starting at a given root node (may also be the Document itself). For each
 * traversed node a callback method is called (visitor pattern).
 * 
 * @author Dominik Rauch
 */
public class DOMTraverser {

	/** Determines whether the children should be traversed as well. */
	private final boolean deep;
	/** The callback interface. */
	private final DOMTraverserCallback callback;

	/**
	 * Calls this(true, true, callback).
	 * 
	 * @see #DOMTraverser(boolean, DOMTraverserCallback)
	 */
	public DOMTraverser(final DOMTraverserCallback callback) {
		this(true, callback);
	}

	/**
	 * Default constructor.
	 * 
	 * @param deep true if the children should be traversed as well, otherwise false.
	 * @param callback the callback interface, which is called for each traversed node.
	 */
	public DOMTraverser(final boolean deep, final DOMTraverserCallback callback) {
		this.deep = deep;
		this.callback = callback;
	}

	/**
	 * Starts the traversal at the given node.
	 * 
	 * @param node root node for this traversal.
	 */
	public void traverse(final Node node) {
		traverseInternal(node.getOwnerDocument(), node, 0);
	}

	private void traverseInternal(final Document doc, final Node node, final int level) {
		callback.nodeTraversed(doc, node, level);

		if (deep && node instanceof ParentNode) {
			// Get child nodes
			final ParentNode nodeAsParent = (ParentNode) node;
			final NodeList childNodes = nodeAsParent.getChildNodes();

			// Traverse child nodes
			for (int i = 0; i < childNodes.getLength(); ++i) {
				final Node childNode = childNodes.item(i);
				traverseInternal(doc, childNode, level + 1);
			}
		}
	}
}
