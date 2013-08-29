package at.ac.tuwien.dsg.scaledom.util;

import org.w3c.dom.Node;

/**
 * DOM utility methods.
 * 
 * @author Dominik Rauch
 */
public class DOMUtils {

	/**
	 * Returns the absolute level of the given node in comparison to the document. The document is on level 0, the
	 * document element is on level 1.
	 * 
	 * @param node the node.
	 * @return the absolute level in comparison to the document.
	 */
	public static int getAbsoluteLevel(Node node) {
		int level = 0;
		while ((node = node.getParentNode()) != null) {
			++level;
		}

		return level;
	}
}
