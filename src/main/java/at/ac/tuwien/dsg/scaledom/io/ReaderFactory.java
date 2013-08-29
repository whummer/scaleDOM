package at.ac.tuwien.dsg.scaledom.io;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;

import at.ac.tuwien.dsg.scaledom.ScaleDomDocumentSource;

/**
 * Base class for all <code>ReaderFactory</code> implementations.<br/>
 * Provides an interface for requesting a <code>Reader</code> for either the whole or only for a specific part of the
 * underlying document source. Implementations are allowed to open the document source just once for performance
 * reasons, it is guaranteed that previously obtained reader objects are no longer used and properly closed before a new
 * reader object is obtained from the factory.
 * 
 * @author Dominik Rauch
 */
public abstract class ReaderFactory implements Closeable, NodeLocationFactory {

	/** Underlying document source. */
	private final ScaleDomDocumentSource documentSource;

	/**
	 * Default constructor.
	 * 
	 * @param source the underlying document source.
	 * @throws IOException If some I/O error occurs.
	 */
	public ReaderFactory(final ScaleDomDocumentSource documentSource) throws IOException {
		checkNotNull(documentSource, "Argument documentSource must not be null.");

		this.documentSource = documentSource;
	}

	/**
	 * Returns the underlying document source.
	 * 
	 * @return the underlying document source.
	 */
	public ScaleDomDocumentSource getDocumentSource() {
		return documentSource;
	}

	/**
	 * Returns a new <code>Reader</code> for the whole document source.
	 * 
	 * @return a Reader for the document source.
	 * @throws IOException If some I/O error occurs.
	 */
	public abstract Reader newReader() throws IOException;

	/**
	 * Returns a new <code>Reader</code> for a range of bytes within the document source.
	 * 
	 * @param location the range in the document source.
	 * @return a Reader for the requested character sequence.
	 * @throws IOException If some I/O error occurs.
	 * @throws IndexOutOfBoundsException If <code>location</code> is not a valid location for the document source.
	 */
	public abstract Reader newReaderForLocation(final NodeLocation location) throws IOException;
}
