package at.ac.tuwien.dsg.scaledom.dom;

import java.lang.ref.SoftReference;
import java.util.LinkedList;

import org.apache.xerces.dom.ChildNode;
import org.apache.xerces.dom.ParentNode;

/**
 * A <code>SoftReference</code> to a list of <code>ChildNode</code>. Holds some information for the reference queue in
 * order to log unloads.
 * 
 * @author Dominik Rauch
 */
public class WeakChildNodeList extends SoftReference<LinkedList<ChildNode>> {

	private final ParentNode parent;

	public WeakChildNodeList(final ScaleDomDocument doc, final ParentNode parent, final LinkedList<ChildNode> children) {
		super(children, doc != null ? doc.getUnloadQueue() : null);
		this.parent = parent;
	}

	@Override
	public String toString() {
		return parent.getNodeName();
	}
}
