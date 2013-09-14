package at.ac.tuwien.dsg.scaledom.io.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>InputStream</code> implementation which is based upon a 
 * specific range within a <code>HttpURLConnection</code>.
 * 
 * @author Waldemar Hummer
 */
public class HttpChannelRangeInputStream extends InputStream {

	/** Logger. */
	private final static Logger log = LoggerFactory.getLogger(HttpChannelRangeInputStream.class);

	/** Underlying file channel */
	private final HttpURLConnection urlConnection;
	/** End of range */
	private final long maxPosition;
	/** Current position */
	private long currentPosition = 0;

	/**
	 * Default constructor.
	 * 
	 * @param urlConnection the URL connection.
	 * @param rangeStart the offset of the first byte of the first character.
	 * @param rangeEnd the offset of the last byte of the last character.
	 * @throws IOException If some I/O error occurs.
	 */
	public HttpChannelRangeInputStream(final HttpURLConnection urlConnection, 
			final long totalLength,
			final long rangeStart, final long rangeEnd) throws IOException {
		checkNotNull(urlConnection, "Expected channel to be not null.");
		final long fileSize = totalLength;
		checkArgument(rangeStart >= 0 && rangeStart <= fileSize,
				"Expected rangeStart to be a valid index, but %s is not. File size is %s.", rangeStart, fileSize);
		checkArgument(rangeEnd > 0 && rangeEnd <= fileSize,
				"Expected rangeEnd to be a valid index, but %s is not. File size is %s.", rangeEnd, fileSize);
		checkArgument(rangeStart < rangeEnd, "Expected rangeStart to be smaller than rangeEnd, but %s >= %s is not.",
				rangeStart, rangeEnd);

		this.urlConnection = urlConnection;
		this.maxPosition = rangeEnd;

		urlConnection.setRequestProperty("Range", "bytes=" + rangeStart + "-" + rangeEnd);
		currentPosition = rangeStart;
	}

	@Override
	public int read() throws IOException {
		if (getRemainingBytes() == 0) {
			return -1;
		}

		log.warn("The read() method should never be used due to performance reasons.");

		// Read a single byte from the channel
		return urlConnection.getInputStream().read();
	}

	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		final int remainingBytes = getRemainingBytes();
		if (remainingBytes == 0) {
			return -1;
		}

		// Load either len bytes or until the end of the range
		final int bytesToRead = Math.min(len, remainingBytes);

		// Read from the URL connection
		final int read = urlConnection.getInputStream().read(b, off, bytesToRead);
		currentPosition += read;

		final byte[] debugBytesRead = new byte[read];
		System.arraycopy(b, off, debugBytesRead, 0, read);
		log.debug("Read (len=" + bytesToRead + " from fileoff=" + currentPosition + "): "
				+ new String(debugBytesRead, "UTF-8"));
		return read;
	}

	/**
	 * Returns the number of remaining bytes until the end of the part has been reached.
	 * 
	 * @return number of remaining bytes.
	 * @throws IOException If some I/O error occurs.
	 */
	private int getRemainingBytes() throws IOException {
		return (int) (maxPosition - currentPosition);
	}
}
