package at.ac.tuwien.dsg.scaledom.parser;

/**
 * The <code>XmlEventLocation</code> implementation for character offsets.
 * 
 * @author Dominik Rauch
 */
public class XmlEventCharLocation extends XmlEventLocation {

	/**
	 * Default constructor.
	 * 
	 * @param startingOffset the relative starting character offset of the occurred event.
	 */
	public XmlEventCharLocation(final long startingOffset) {
		super(startingOffset);
	}
}
