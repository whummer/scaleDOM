package at.ac.tuwien.dsg.scaledom.test.util.dom;

import org.apache.xerces.dom.ParentNode;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class FullOutputCallback implements DOMTraverserCallback {

	@Override
	public void nodeTraversed(final Document doc, final Node node, final int level) {
		for (int i = 0; i < level; ++i) {
			System.out.print("   ");
		}

		switch (node.getNodeType()) {
		case Node.CDATA_SECTION_NODE:
			System.out.println("|- CD: " + node.getNodeValue());
			break;

		case Node.COMMENT_NODE:
			System.out.println("|- C: " + node.getNodeValue());
			break;

		case Node.DOCUMENT_NODE:
			System.out.println("|- D: " + node.getNodeName());
			break;

		case Node.DOCUMENT_TYPE_NODE:
			System.out.println("|- DT: " + node.getNodeName());
			break;

		case Node.ELEMENT_NODE:
			System.out.print("|- E: " + node.getNodeName());

			final Element element = (Element) node;
			if (element.hasAttributes()) {
				System.out.print(" (");
				final NamedNodeMap attrs = element.getAttributes();
				for (int i = 0; i < attrs.getLength(); ++i) {
					final Attr attr = (Attr) attrs.item(i);
					System.out.print(attr.getNodeName() + "=\"" + attr.getNodeValue() + "\"");

					if (i < attrs.getLength() - 1) {
						System.out.print(", ");
					}
				}
				System.out.print(')');
			}

			final ParentNode asParentNode = (ParentNode) element;
			System.out.println(" with " + asParentNode.getLength() + " children @ " + asParentNode.getNodeLocation());
			break;

		case Node.PROCESSING_INSTRUCTION_NODE:
			System.out.println("|- PI: " + node.getNodeName() + " / " + node.getNodeValue());
			break;

		case Node.TEXT_NODE:
			System.out.println("|- T: " + node.getNodeValue());
			break;
		}
	}
}
