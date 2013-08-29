package at.ac.tuwien.dsg.scaledom.util;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.io.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility <code>Reader</code> implementation which joins one or more other <code>Reader</code> to appear as one.
 * 
 * @author Dominik Rauch
 */
public class CompositeReader extends Reader {

	/** Logger. */
	private final static Logger log = LoggerFactory.getLogger(CompositeReader.class);

	/** List of readers (in order). */
	private final Reader[] readers;
	/** Current index. */
	private int index;

	/**
	 * Default constructor.
	 * 
	 * @param readers ordered list of <code>Reader</code> to read from.
	 */
	public CompositeReader(final Reader... readers) {
		checkArgument(readers.length > 0, "Argument readers must not be empty.");

		this.readers = readers;
		index = 0;
	}

	@Override
	public int read(final char[] cbuf, final int off, final int len) throws IOException {
		int read = 0;
		while (read < len && index != readers.length) {
			final Reader reader = readers[index];
			final int readFromReader = reader.read(cbuf, off + read, len - read);

			if (readFromReader == -1) {
				++index;
			} else {
				read += readFromReader;
			}
		}

		if (read == 0) {
			return -1;
		}

		return read;
	}

	@Override
	public void close() throws IOException {
		IOException firstException = null;

		for (final Reader reader : readers) {
			try {
				reader.close();
			} catch (final IOException ex) {
				if (firstException != null) {
					log.warn("Multiple readers could not be closed, only first exception will be thrown.");
					firstException = ex;
				}
			}
		}

		if (firstException != null) {
			throw firstException;
		}
	}
}
