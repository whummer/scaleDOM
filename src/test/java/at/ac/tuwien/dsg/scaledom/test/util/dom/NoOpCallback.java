package at.ac.tuwien.dsg.scaledom.test.util.dom;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class NoOpCallback implements DOMTraverserCallback {

	@Override
	public void nodeTraversed(final Document doc, final Node node, final int level) {
		// Do nothing
	}
}
