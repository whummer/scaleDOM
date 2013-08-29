package at.ac.tuwien.dsg.scaledom.parser;

/**
 * The <code>XmlEventLocation</code> implementation for byte offsets.
 * 
 * @author Dominik Rauch
 */
public class XmlEventByteLocation extends XmlEventLocation {

	/**
	 * Default constructor.
	 * 
	 * @param startingOffset the relative starting byte offset of the occurred event.
	 */
	public XmlEventByteLocation(final long startingOffset) {
		super(startingOffset);
	}
}
