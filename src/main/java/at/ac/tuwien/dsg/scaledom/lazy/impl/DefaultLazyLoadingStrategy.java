package at.ac.tuwien.dsg.scaledom.lazy.impl;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.XMLEvent;

import at.ac.tuwien.dsg.scaledom.lazy.LazyLoadingStrategy;
import at.ac.tuwien.dsg.scaledom.parser.XmlEventLocation;

/**
 * The default strategy loads the requested children only.
 * 
 * @author Dominik Rauch
 */
public class DefaultLazyLoadingStrategy extends LazyLoadingStrategy {

	/** Maximum depth to load. */
	private static final int DEPTH = 1;
	/** Current level. */
	private int level;

	/**
	 * Default constructor.
	 * 
	 * @see LazyLoadingStrategy#LazyLoadingStrategy(int)
	 */
	public DefaultLazyLoadingStrategy(final int absouteLevel) {
		super(absouteLevel);
		level = 0;
	}

	@Override
	public boolean shouldLoad(final XMLEvent event, final XmlEventLocation eventLocation) {
		if (event.getEventType() == XMLStreamConstants.END_ELEMENT) {
			--level;
		}

		final boolean shouldLoad = level < DEPTH;

		if (event.getEventType() == XMLStreamConstants.START_ELEMENT) {
			++level;
		}

		return shouldLoad;
	}
}
