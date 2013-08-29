package at.ac.tuwien.dsg.scaledom.io.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.nio.channels.FileChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.scaledom.io.NodeLocation;
import at.ac.tuwien.dsg.scaledom.io.ReaderFactory;
import at.ac.tuwien.dsg.scaledom.parser.XmlEventCharLocation;
import at.ac.tuwien.dsg.scaledom.parser.XmlEventLocation;
import at.ac.tuwien.dsg.scaledom.util.Utils;

/**
 * <code>FileReaderFactory</code> implementation.<br/>
 * This implementation provides a <code>Reader</code> based upon <code>FileInputStream</code> for full file reading and
 * one based upon <code>RandomAccessFile</code> for file range reading.
 * 
 * @author Dominik Rauch
 */
public class FileReaderFactory extends ReaderFactory {

	/** Logger. */
	private final static Logger log = LoggerFactory.getLogger(FileReaderFactory.class);

	/** Underlying file as RandomAccessFile. */
	private RandomAccessFile randomAccessFile;
	/** File channel. */
	private FileChannel channel;
	/** Number of bytes per character. */
	private final int numberOfBytesPerCharacter;
	/** Start offset of the currently obtained reader. */
	private long readerOffset;

	/**
	 * Default constructor.
	 * 
	 * @param source the underlying document source.
	 * @throws IOException If some I/O error occurs.
	 */
	public FileReaderFactory(final FileDocumentSource source) throws IOException {
		super(source);
		numberOfBytesPerCharacter = Utils.getNumberOfBytesPerCharacter(source.getEncoding());
		readerOffset = 0;

		log.debug("Encoding " + source.getEncoding() + " is used for the document. Number of bytes per character: "
				+ numberOfBytesPerCharacter);
	}

	@Override
	public Reader newReader() throws IOException {
		final FileDocumentSource source = (FileDocumentSource) getDocumentSource();
		readerOffset = 0;
		return new BufferedReader(new InputStreamReader(new FileInputStream(source.getFile()), source.getEncoding()));
	}

	@Override
	public Reader newReaderForLocation(final NodeLocation location) throws IOException {
		checkNotNull(location, "Argument location must not be null");
		checkArgument(location instanceof FileNodeLocation, "Argument location must be of type FileNodeLocation");

		final FileDocumentSource source = (FileDocumentSource) getDocumentSource();

		if (randomAccessFile == null) {
			// Open file only on first demand, however, keep it open until factory is closed
			randomAccessFile = new RandomAccessFile(source.getFile(), "r");
			channel = randomAccessFile.getChannel();
		}

		final FileNodeLocation fileLocation = (FileNodeLocation) location;
		readerOffset = fileLocation.getStartOffset();

		return new BufferedReader(new InputStreamReader(new FileChannelRangeInputStream(channel,
				fileLocation.getStartOffset(), fileLocation.getEndOffset()), source.getEncoding()));
	}

	@Override
	public void close() throws IOException {
		if (randomAccessFile != null) {
			randomAccessFile.close();
		}
	}

	@Override
	public NodeLocation eventLocationToNodeLocation(final XmlEventLocation location, final long additionalOffset) {
		final int sizefac = location instanceof XmlEventCharLocation ? numberOfBytesPerCharacter : 1;
		if (sizefac == -1) {
			// problem: got XmlEventCharLocation but file is variable-width encoded -> throw exception
			final FileDocumentSource fds = (FileDocumentSource) getDocumentSource();
			throw new IllegalArgumentException("File is variable-width encoded (" + fds.getEncoding()
					+ "), you must use an XmlParser implementation which is able to output byte locations.");
		}

		final long startingOffset = (location.getStartingOffset() + readerOffset - additionalOffset) * sizefac;
		return new FileNodeLocation(startingOffset, FileNodeLocation.OFFSET_UNKNOWN);
	}
}
