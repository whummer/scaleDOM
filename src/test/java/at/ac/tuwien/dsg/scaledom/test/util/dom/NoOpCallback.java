package at.ac.tuwien.dsg.scaledom.test.util.dom;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import at.ac.tuwien.dsg.scaledom.util.DOMTraverserCallback;

public class NoOpCallback implements DOMTraverserCallback {

	@Override
	public void nodeTraversed(final Document doc, final Node node, final int level) {
		// Do nothing
	}
}
