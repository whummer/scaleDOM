package at.ac.tuwien.dsg.scaledom.io.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.scaledom.io.NodeLocation;
import at.ac.tuwien.dsg.scaledom.io.ReaderFactory;
import at.ac.tuwien.dsg.scaledom.io.ReaderWithSystemID;
import at.ac.tuwien.dsg.scaledom.parser.XmlEventCharLocation;
import at.ac.tuwien.dsg.scaledom.parser.XmlEventLocation;
import at.ac.tuwien.dsg.scaledom.util.Utils;

/**
 * <code>HttpReaderFactory</code> implementation.<br/>
 * This implementation provides a <code>Reader</code> based upon URL <code>InputStream</code> for 
 * full file reading and one based upon <code>RandomAccessFile</code> for file range reading.
 * 
 * @author Waldemar Hummer
 */
public class HttpReaderFactory extends ReaderFactory {

	/** Logger. */
	private final static Logger log = LoggerFactory.getLogger(HttpReaderFactory.class);

	/** Underlying URL as connection. */
	private HttpURLConnection urlConnection;
	/** Number of bytes per character. */
	private final int numberOfBytesPerCharacter;
	/** Start offset of the currently obtained reader. */
	private long readerOffset;
	/** Content length of the HTTP document. */
	private Long contentLength = null;

	/**
	 * Default constructor.
	 * 
	 * @param source the underlying document source.
	 * @throws IOException If some I/O error occurs.
	 */
	public HttpReaderFactory(final HttpDocumentSource source) throws IOException {
		super(source);
		numberOfBytesPerCharacter = Utils.getNumberOfBytesPerCharacter(source.getEncoding());
		readerOffset = 0;

		log.debug("Encoding " + source.getEncoding() + " is used for the document. Number of bytes per character: "
				+ numberOfBytesPerCharacter);
	}

	@Override
	public Reader newReader() throws IOException {
		final HttpDocumentSource source = (HttpDocumentSource) getDocumentSource();
		readerOffset = 0;
		InputStream is = source.getUrl().openConnection().getInputStream();
		Reader reader = new BufferedReader(new InputStreamReader(is, source.getEncoding()));
		return new ReaderWithSystemID(source.getUrl().toExternalForm(), reader);
	}

	@Override
	public Reader newReaderForLocation(final NodeLocation location) throws IOException {
		checkNotNull(location, "Argument location must not be null");
		checkArgument(location instanceof FileNodeLocation, "Argument location must be of type FileNodeLocation");

		final HttpDocumentSource source = (HttpDocumentSource) getDocumentSource();

		if(contentLength == null) {
			urlConnection = (HttpURLConnection)source.getUrl().openConnection();
			contentLength = urlConnection.getContentLengthLong();
		}
		// Close and re-open URL connection
		if (urlConnection != null) {
			urlConnection.disconnect();
		}
		urlConnection = (HttpURLConnection)source.getUrl().openConnection();

		final FileNodeLocation fileLocation = (FileNodeLocation) location;
		readerOffset = fileLocation.getStartOffset();

		Reader reader = new BufferedReader(new InputStreamReader(
				new HttpChannelRangeInputStream(urlConnection, contentLength,
				fileLocation.getStartOffset(), fileLocation.getEndOffset()), 
				source.getEncoding()));
		return new ReaderWithSystemID(source.getUrl().toExternalForm(), reader);
	}

	@Override
	public void close() throws IOException {
		if (urlConnection != null) {
			urlConnection.disconnect();
		}
	}

	@Override
	public NodeLocation eventLocationToNodeLocation(final XmlEventLocation location, final long additionalOffset) {
		final int sizefac = location instanceof XmlEventCharLocation ? numberOfBytesPerCharacter : 1;
		if (sizefac == -1) {
			// problem: got XmlEventCharLocation but file is variable-width encoded -> throw exception
			final HttpDocumentSource fds = (HttpDocumentSource) getDocumentSource();
			throw new IllegalArgumentException("File is variable-width encoded (" + fds.getEncoding()
					+ "), you must use an XmlParser implementation which is able to output byte locations.");
		}

		final long startingOffset = (location.getStartingOffset() + readerOffset - additionalOffset) * sizefac;
		return new FileNodeLocation(startingOffset, FileNodeLocation.OFFSET_UNKNOWN);
	}
}
