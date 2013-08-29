package at.ac.tuwien.dsg.scaledom.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Closeable;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;

import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A low memory detector, based upon https://techblug.wordpress.com/2011/07/21/detecting-low-memory-in-java-part-2/ and
 * http://grepcode.com/file/repo1.maven.org/maven2/org.apache.pig/pig/0.10.1/org/apache/pig/impl/util/
 * SpillableMemoryManager.java?av=f
 * 
 * @author Dominik Rauch
 */
public class LowMemoryDetector implements NotificationListener, Closeable {

	/** Logger. */
	private final static Logger log = LoggerFactory.getLogger(LowMemoryDetector.class);

	/** MX bean for the tenured heap. */
	private final MemoryPoolMXBean tenuredHeap;
	/** Heap size in bytes. */
	private final long tenuredHeapSize;
	/** Heap threshold in bytes. */
	private final long memoryThreshold;

	/** Flag whether the system is low on memory. */
	private boolean isLowMemory;

	/**
	 * Default constructor.
	 * 
	 * @param memoryThresholdFactor amount of memory to be considered 'enough memory'.
	 * @throws IllegalArgumentException If memoryThresholdFactor is not strictly between 0.0 and 1.0.
	 */
	public LowMemoryDetector(final double memoryThresholdFactor) {
		checkArgument(memoryThresholdFactor > 0.0 && memoryThresholdFactor < 1.0,
				"Expected memoryThresholdFactor to be between 0.0 and 1.0, %s is not.", memoryThresholdFactor);

		isLowMemory = false;

		// Find the tenured heap
		tenuredHeap = findTenuredHeap();
		checkNotNull(tenuredHeap, "Expected tenuredHeap to be not null.");
		tenuredHeapSize = tenuredHeap.getUsage().getMax();
		log.debug("Determined the tenured heap as '{}' (size: {} B).", tenuredHeap.getName(), tenuredHeapSize);

		// Monitor tenured heap
		memoryThreshold = (long) (tenuredHeapSize * memoryThresholdFactor);
		tenuredHeap.setCollectionUsageThreshold(memoryThreshold);
		log.debug("Low memory threshold is {} B.", memoryThreshold);

		// Add notification listener
		final NotificationEmitter notificationEmitter = (NotificationEmitter) ManagementFactory.getMemoryMXBean();
		notificationEmitter.addNotificationListener(this, null, null);
	}

	/**
	 * Returns whether the system has run into low-memory problems at least once.
	 * 
	 * @return true if a low-memory situation has occurred at least once, false otherwise.
	 */
	public boolean isLowMemory() {
		return isLowMemory;
	}

	@Override
	public void close() throws IOException {
		try {
			// Remove notification listener
			final NotificationEmitter notificationEmitter = (NotificationEmitter) ManagementFactory.getMemoryMXBean();
			notificationEmitter.removeNotificationListener(this);
		} catch (final ListenerNotFoundException ex) {
			log.warn("Somebody else already removed the notification listener from the MemoryMXBean.", ex);
		}
	}

	@Override
	public void handleNotification(final Notification notification, final Object handback) {
		if (isLowMemory) {
			return;
		}

		final long currentUsage = tenuredHeap.getCollectionUsage().getUsed();
		if (currentUsage > memoryThreshold) {
			isLowMemory = true;
			log.debug("System is running low on memory! {} of {} B are already in use.", currentUsage, tenuredHeapSize);
		}
	}

	private static MemoryPoolMXBean findTenuredHeap() {
		// Find tenured heap by assuming it is the biggest heap in the system
		MemoryPoolMXBean biggestMemoryPool = null;
		long biggestMemoryPoolSize = Long.MIN_VALUE;

		for (final MemoryPoolMXBean memoryPool : ManagementFactory.getMemoryPoolMXBeans()) {
			if (memoryPool.getType() != MemoryType.HEAP) {
				continue;
			}

			// Check if collection usage threshold is supported
			final String memoryPoolName = memoryPool.getName();
			final long memoryPoolSize = memoryPool.getUsage().getMax();
			if (!memoryPool.isCollectionUsageThresholdSupported()) {
				log.debug("Found heap '{}' (size: {}), but no support for collection usage threshold.", memoryPoolName,
						Utils.toHumanReadableByteCount(memoryPoolSize));
				continue;
			}

			log.debug("Found heap '{}' (size: {}).", memoryPoolName, Utils.toHumanReadableByteCount(memoryPoolSize));

			if (memoryPoolSize > biggestMemoryPoolSize) {
				biggestMemoryPool = memoryPool;
				biggestMemoryPoolSize = memoryPoolSize;
			}
		}

		return biggestMemoryPool;
	}
}
