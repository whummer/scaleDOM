package at.ac.tuwien.dsg.scaledom.test.perf.util.dom;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import at.ac.tuwien.dsg.scaledom.util.DOMTraverserCallback;

public class MinimalOutputCallback implements DOMTraverserCallback {

	private final int maxLevelForDetailedOutput;
	private boolean lastNodeWasDetailed;

	public MinimalOutputCallback(final int maxLevelForDetailedOutput) {
		this.maxLevelForDetailedOutput = maxLevelForDetailedOutput;
		lastNodeWasDetailed = true;
	}

	@Override
	public void nodeTraversed(final Document doc, final Node node, final int level) {
		if (!(node instanceof Element)) {
			return;
		}

		if (level < maxLevelForDetailedOutput) {
			if (!lastNodeWasDetailed) {
				System.out.println();
				lastNodeWasDetailed = true;
			}
			System.out.println(level + "-" + node.getNodeName());
		} else {
			if (!lastNodeWasDetailed) {
				System.out.print('-');
			}
			System.out.print(level);
			lastNodeWasDetailed = false;
		}
	}
}
