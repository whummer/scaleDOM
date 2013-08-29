package at.ac.tuwien.dsg.scaledom.io.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import at.ac.tuwien.dsg.scaledom.io.NodeLocation;

public class FileNodeLocation extends NodeLocation {

	/** Special value, indicating the offset is not known yet. */
	public final static long OFFSET_UNKNOWN = Long.MIN_VALUE;

	private long startOffset;
	private long endOffset;

	FileNodeLocation(final long startOffset, final long endOffset) {
		this.startOffset = startOffset;
		this.endOffset = endOffset;
	}

	public long getStartOffset() {
		return startOffset;
	}

	public long getEndOffset() {
		return endOffset;
	}

	@Override
	public void setEndLocation(final NodeLocation location) {
		checkNotNull(location, "Argument location must not be null");
		checkArgument(location instanceof FileNodeLocation, "Argument location must be of type FileNodeLocation");

		if (endOffset == OFFSET_UNKNOWN) {
			final FileNodeLocation fileNodeLocation = (FileNodeLocation) location;
			endOffset = fileNodeLocation.getStartOffset();
		}
	}

	@Override
	public String toString() {
		// for debug use
		return "[" + startOffset + "/" + endOffset + "]";
	}
}
