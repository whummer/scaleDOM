package at.ac.tuwien.dsg.scaledom.test.perf.util.dom;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author Dominik Rauch
 * @see DOMTraverser
 */
public interface DOMTraverserCallback {

	/**
	 * Called once for each traversed node.
	 * 
	 * Hint: If the document has been the root node, level is 0 when called for the document itself and 1 if the
	 * traversed node is the root element.
	 * 
	 * @param doc the corresponding document.
	 * @param node the traversed node.
	 * @param level the level of the node beyond the used root node (warning: root node may not be the document).
	 */
	void nodeTraversed(final Document doc, final Node node, final int level);
}
