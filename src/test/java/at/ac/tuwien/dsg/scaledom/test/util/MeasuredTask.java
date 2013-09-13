package at.ac.tuwien.dsg.scaledom.test.util;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import org.apache.commons.lang3.time.StopWatch;

import at.ac.tuwien.dsg.scaledom.util.Utils;

/**
 * TODO: Class documentation.
 * 
 * @author Dominik Rauch
 * @param <T> return type of the underlying task.
 */
public abstract class MeasuredTask<T> {

	/** Memory management MX bean. */
	private final static MemoryMXBean memoryMxBean = ManagementFactory.getMemoryMXBean();

	/** Name of the task. */
	private final String taskName;

	/**
	 * Default constructor.
	 * 
	 * @param taskName name of the task.
	 */
	public MeasuredTask(final String taskName) {
		this.taskName = taskName;
	}

	/**
	 * Executes the task by calling {@link #runTask()} and measures time and memory consumption.
	 * 
	 * @return the result of {@link #runTask()}
	 * @throws Exception If {@link #runTask()} throws an exception.
	 */
	public T run() throws Exception {
		System.out.println();
		System.out.println("MeasuredTask '" + taskName + "' started...");

		final StopWatch watch = new StopWatch();

		// Start memory measurement
		System.gc();
		final long initialHeapUsage = memoryMxBean.getHeapMemoryUsage().getUsed();
		final long initialNonHeapUsage = memoryMxBean.getNonHeapMemoryUsage().getUsed();

		// Start time measurement
		watch.start();

		Throwable catchedThrowable = null;
		try {
			// Execute task
			return runTask();
		} catch (final Throwable t) {
			// Store catched throwable
			catchedThrowable = t;
			throw new Exception(t);
		} finally {
			// Stop time measurement
			watch.stop();

			// Stop memory measurement
			final long finalHeapUsage = memoryMxBean.getHeapMemoryUsage().getUsed();
			final long finalNonHeapUsage = memoryMxBean.getNonHeapMemoryUsage().getUsed();

			// Print results
			System.out.println("MeasuredTask '" + taskName + "' finished, results:");
			System.out.print("  - Exited ");
			if (catchedThrowable == null) {
				System.out.println("successfully");
			} else {
				System.out.println("with '" + catchedThrowable.getClass().getName() + "': "
						+ catchedThrowable.getMessage());
			}
			System.out.println("  - Elapsed time: " + Utils.toHumanReadableTime(watch.getTime()));
			System.out.println("  - Additional heap usage: "
					+ Utils.toHumanReadableByteCount(finalHeapUsage - initialHeapUsage));
			System.out.println("    - Initial heap usage: " + Utils.toHumanReadableByteCount(initialHeapUsage));
			System.out.println("    - Final heap usage: " + Utils.toHumanReadableByteCount(finalHeapUsage));
			System.out.println("  - Additional non-heap usage: "
					+ Utils.toHumanReadableByteCount(finalNonHeapUsage - initialNonHeapUsage));
			System.out.println("    - Initial non-heap usage: " + Utils.toHumanReadableByteCount(initialNonHeapUsage));
			System.out.println("    - Final non-heap usage: " + Utils.toHumanReadableByteCount(finalNonHeapUsage));
		}
	}

	/**
	 * The task to be measured.
	 * 
	 * @return the result object of the task or null.
	 * @throws Exception If the underlying task throws an exception.
	 */
	protected abstract T runTask() throws Exception;
}
