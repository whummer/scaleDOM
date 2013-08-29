package at.ac.tuwien.dsg.scaledom.parser;

/**
 * Holds the relative starting offset of an occurred event within the parser's XML source (e.g. a <code>Reader</code>).
 * Parsers are encouraged to provide an <code>XmlEventByteLocation</code> whenever possible. Providing an
 * <code>XmlEventCharLocation</code> may result in aborts (current implementation) or at least entail severe performance
 * losses (planned future implementation).
 * 
 * @author Dominik Rauch
 */
public abstract class XmlEventLocation {

	private final long startingOffset;

	/**
	 * Default constructor.
	 * 
	 * @param startingOffset the relative starting offset of the occurred event.
	 */
	XmlEventLocation(final long startingOffset) {
		this.startingOffset = startingOffset;
	}

	/**
	 * Returns the starting offset.
	 * 
	 * @return the starting offset.
	 */
	public long getStartingOffset() {
		return startingOffset;
	}
}
