package at.ac.tuwien.dsg.scaledom.lazy;

import javax.xml.stream.events.XMLEvent;

import at.ac.tuwien.dsg.scaledom.parser.XmlEventLocation;

public abstract class LazyLoadingStrategy {

	private final int absoluteLevel;

	/**
	 * Default constructor.
	 * 
	 * @param absoluteLevel the level of the load process' root element (in comparison to the root element).
	 */
	public LazyLoadingStrategy(final int absoluteLevel) {
		this.absoluteLevel = absoluteLevel;
	}

	/**
	 * Decides whether an event should be processed or not. The <code>LoadProcess</code> asks the strategy only for
	 * node-creating events (like StartElement, EndElement as well, Comment, etc.).
	 * 
	 * Implementation restrictions:
	 * <ul>
	 * <li>Must return the same value for a StartElement event and its corresponding EndElement event.</li>
	 * <li>Load either all or none direct children of a parent node.</li>
	 * </ul>
	 * 
	 * @param event the occurred event.
	 * @param location the event's location in the document source.
	 * @return true if the LoadProcess should process the event, false otherwise.
	 */
	public abstract boolean shouldLoad(final XMLEvent event, final XmlEventLocation location);

	/**
	 * Returns the absolute level of the current load process.
	 * 
	 * @return the absolute level of the current load process.
	 */
	public int getAbsoluteLevel() {
		return absoluteLevel;
	}
}
