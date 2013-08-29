package at.ac.tuwien.dsg.scaledom.lazy.impl;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.scaledom.lazy.LazyLoadingStrategy;
import at.ac.tuwien.dsg.scaledom.parser.XmlEventLocation;

/**
 * This LLS
 * 
 * @author Dominik Rauch
 */
public class StepLazyLoadingStrategy extends LazyLoadingStrategy {

	/** Logger. */
	private final static Logger log = LoggerFactory.getLogger(StepLazyLoadingStrategy.class);

	/** Maximum depth to load. */
	private final int depth;
	/** Current level. */
	private int level;

	/**
	 * Default constructor.
	 * 
	 * @see LazyLoadingStrategy#LazyLoadingStrategy(int)
	 */
	public StepLazyLoadingStrategy(final int absoluteLevel) {
		super(absoluteLevel);

		if (absoluteLevel == 0) {
			depth = 2;
		} else if (absoluteLevel == 1) {
			depth = 1;
		} else {
			depth = Math.min(absoluteLevel * 3, 9);
		}

		level = 0;

		log.debug("Reloading on level " + absoluteLevel + ", determined loading depth: " + depth);
	}

	@Override
	public boolean shouldLoad(final XMLEvent event, final XmlEventLocation eventLocation) {
		if (event.getEventType() == XMLStreamConstants.END_ELEMENT) {
			--level;
		}

		final boolean shouldLoad = level < depth;

		if (event.getEventType() == XMLStreamConstants.START_ELEMENT) {
			++level;
		}

		return shouldLoad;
	}
}
