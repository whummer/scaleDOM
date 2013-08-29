package at.ac.tuwien.dsg.scaledom.dom;

import java.util.LinkedList;

import org.apache.xerces.dom.ParentNode;

public class LoadProcessState {

	/** Parent node hierarchy of the current load process from root node down to the current parent node. */
	private final LinkedList<ParentNode> hierarchy;
	/** Previous parent node. */
	private ParentNode previousParentNode;
	/** Number of events to skip. */
	private int eventsToSkip;
	/** Last occurred event type. */
	private int lastEventType;
	/** Number of created nodes. */
	private long numberOfCreatedNodes;

	public LoadProcessState(final ParentNode root, final int elementsToSkip) {
		hierarchy = new LinkedList<ParentNode>();
		hierarchy.add(root);
		previousParentNode = null;
		this.eventsToSkip = elementsToSkip;
		lastEventType = -1;
		numberOfCreatedNodes = 0;
	}

	public boolean shouldSkipEvent() {
		return eventsToSkip > 0;
	}

	public void skippedEvent() {
		--eventsToSkip;
	}

	public int getLastEventType() {
		return lastEventType;
	}

	public void setLastEventType(final int eventType) {
		lastEventType = eventType;
	}

	public ParentNode getPreviousParentNode() {
		return previousParentNode;
	}

	public ParentNode getCurrentParentNode() {
		if (hierarchy.isEmpty()) {
			// May be empty if there are more EndElement events processed than StartElement events, this is possible if
			// eventsToSkip > 0 and StartElement events have been skipped
			return null;
		}

		return hierarchy.getLast();
	}

	public void addParentNodeAtBottom(final ParentNode parent) {
		hierarchy.addLast(parent);
	}

	public void removeCurrentParentNode() {
		if (hierarchy.isEmpty()) {
			// May be empty if there are more EndElement events processed than StartElement events, this is possible if
			// eventsToSkip > 0 and StartElement events have been skipped
			return;
		}

		previousParentNode = hierarchy.removeLast();
	}

	public void createdNode() {
		++numberOfCreatedNodes;
	}

	public long getNumberOfCreatedNodes() {
		return numberOfCreatedNodes;
	}
}
