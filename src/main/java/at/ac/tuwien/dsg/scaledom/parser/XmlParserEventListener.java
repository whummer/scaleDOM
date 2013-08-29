package at.ac.tuwien.dsg.scaledom.parser;

import javax.xml.stream.events.XMLEvent;

/**
 * Called by an <code>XmlParser</code> implementation for each occurring event.
 * 
 * @author Dominik Rauch
 */
public interface XmlParserEventListener {

	public final static boolean ABORT_PARSING = true;
	public final static boolean CONTINUE_PARSING = false;

	/**
	 * Process the occurred event, e.g. by adjusting the currently generated <code>ScaleDomDocumentImpl</code>.
	 * 
	 * @param event the occurred event.
	 * @param location the event's location in the underlying source.
	 * @return true if the observer wants to abort the parsing, false otherwise.
	 */
	boolean process(final XMLEvent event, final XmlEventLocation location);
}
