package at.ac.tuwien.dsg.scaledom.dom;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.ListIterator;

import org.apache.xerces.dom.ChildNode;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * List of a <code>ParentNode</code>'s children.
 * 
 * @author Dominik Rauch
 */
public class ChildNodeList implements NodeList {

	/** The children. */
	private List<ChildNode> children;

	/**
	 * Performance improvement: Items are supposably requested one after the other without in-between modifications of
	 * the list of child nodes => iterator is much more efficient. If ChildNodeList is used otherwise, iterator is
	 * recreated on-the-fly.
	 */
	private ListIterator<ChildNode> iterator;

	/**
	 * Default constructor.
	 * 
	 * @param children the list of children.
	 */
	public ChildNodeList(final List<ChildNode> children) {
		checkNotNull(children, "Expected children to be not null.");
		this.children = children;
	}

	@Override
	public Node item(final int index) {
		// Invalid indices must return null
		if (index < 0 || index > getLength()) {
			return null;
		}

		// Create new iterator starting on given index if this is the first call to #item() or index is unexpected
		if (iterator == null || iterator.nextIndex() != index) {
			iterator = children.listIterator(index);
		}

		try {
			// Return next iterator element
			return iterator.next();
		} catch (final ConcurrentModificationException ex) {
			// List has been modified since last call of #item()
			iterator = children.listIterator(index);
			return iterator.next();
		}
	}

	@Override
	public int getLength() {
		return children.size();
	}
}
