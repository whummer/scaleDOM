package at.ac.tuwien.dsg.scaledom.util;

import java.lang.ref.ReferenceQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReferenceQueueLogThread extends Thread implements Runnable {

	/** Logger. */
	private final static Logger log = LoggerFactory.getLogger(ReferenceQueueLogThread.class);

	private final ReferenceQueue<?> referenceQueue;
	private final String unloadMessage;

	/**
	 * Default constructor.
	 * 
	 * @param referenceQueue the reference queue to be logged.
	 * @param unloadMessage a message to be logged, containing one placeholder such that
	 *            <code>String.format(unloadMessage, unloadedObj)</code> returns the desired log message.
	 */
	public ReferenceQueueLogThread(final ReferenceQueue<?> referenceQueue, final String unloadMessage) {
		this.referenceQueue = referenceQueue;
		this.unloadMessage = unloadMessage;
	}

	@Override
	public void run() {
		while (true) {
			try {
				final Object unloadedObj = referenceQueue.remove();
				if (unloadedObj != null) {
					log.debug(String.format(unloadMessage, unloadedObj));
				}
			} catch (final InterruptedException ex) {
				log.warn("Reference queue log thread has been unexpectedly interrupted.");
			}
		}
	}
}
