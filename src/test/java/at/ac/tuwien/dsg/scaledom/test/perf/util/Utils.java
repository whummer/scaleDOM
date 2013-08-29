package at.ac.tuwien.dsg.scaledom.test.perf.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

/**
 * General utility methods.
 * 
 * @author Dominik Rauch
 */
public class Utils {

	private static final long MS_PER_SECOND = 1000;
	private static final long MS_PER_MINUTE = MS_PER_SECOND * 60;
	private static final long MS_PER_HOUR = MS_PER_MINUTE * 60;

	private static final String[] BYTE_UNITS = { "B", "KiB", "MiB", "GiB" };
	private static final long BYTE_FAC = 1024;

	/**
	 * Returns whether the given charset is fixed-width.
	 * 
	 * @param charsetName a charset name.
	 * @return true if the charset is fixed-width, false if the charset is variable-width.
	 */
	public static boolean isFixedWith(final String charsetName) {
		checkNotNull(charsetName, "Argument charsetName must not be null.");
		checkArgument(Charset.isSupported(charsetName), "Charset '%s' is not supported.", charsetName);

		final Charset charset = Charset.forName(charsetName);
		final CharsetEncoder encoder = charset.newEncoder();
		return encoder.averageBytesPerChar() == encoder.maxBytesPerChar();
	}

	/**
	 * Returns the number of bytes per character in the given charset.
	 * 
	 * @param charsetName a charset name.
	 * @return the number of fixed-width bytes per character in the charset or -1 if charset is variable-width.
	 */
	public static int getNumberOfBytesPerCharacter(final String charsetName) {
		if (!isFixedWith(charsetName)) {
			return -1;
		}

		final Charset charset = Charset.forName(charsetName);
		final CharsetEncoder encoder = charset.newEncoder();
		return (int) encoder.maxBytesPerChar();
	}

	/**
	 * Returns a human-readable version of the given time span.
	 * 
	 * @param ms time span in milliseconds, must be positive.
	 * @return Time span in ms, s, min or h - including the time unit.
	 */
	public static String toHumanReadableTime(final long ms) {
		checkArgument(ms >= 0, "Argument ms must be greater or equal 0.");

		if (ms < MS_PER_SECOND) {
			return ms + " ms";
		} else if (ms < MS_PER_MINUTE) {
			return (ms / MS_PER_SECOND) + " s";
		} else if (ms < MS_PER_HOUR) {
			return (ms / MS_PER_MINUTE) + " min";
		} else {
			return (ms / MS_PER_HOUR) + " h";
		}
	}

	/**
	 * Returns a human-readable version of the given byte count.
	 * 
	 * @param bytes byte count, may be negative as well.
	 * @return Byte count in B, KiB, MiB or GiB - including the unit.
	 */
	public static String toHumanReadableByteCount(final long bytes) {
		double value = bytes;

		int i = 0;
		for (; Math.abs(value) > BYTE_FAC && i < BYTE_UNITS.length; ++i) {
			value /= BYTE_FAC;
		}

		return ((long) value) + " " + BYTE_UNITS[i];
	}
}
