package at.ac.tuwien.dsg.scaledom.io.impl;

import java.io.IOException;
import java.io.Reader;

import at.ac.tuwien.dsg.scaledom.io.NodeLocation;
import at.ac.tuwien.dsg.scaledom.io.ReaderFactory;
import at.ac.tuwien.dsg.scaledom.parser.XmlEventLocation;

/**
 * Generic ReaderFactory implementation which delegates to specialized
 * ReaderFactory implementations depending on the ScaleDomDocumentSource
 * (e.g., local file or HTTP connection)
 * 
 * @author Waldemar Hummer
 */
public class DelegatorReaderFactory extends ReaderFactory {

	private ReaderFactory actualFactory;

	/**
	 * Construct a reader factory for a FileDocumentSource.
	 */
	public DelegatorReaderFactory(final FileDocumentSource source) throws IOException {
		super(null);
		actualFactory = new FileReaderFactory(source);
	}

	/**
	 * Construct a reader factory for a HttpDocumentSource.
	 */
	public DelegatorReaderFactory(final HttpDocumentSource source) throws IOException {
		super(source);
		actualFactory = new HttpReaderFactory(source);
	}

	@Override
	public Reader newReader() throws IOException {
		return actualFactory.newReader();
	}

	@Override
	public Reader newReaderForLocation(final NodeLocation location) throws IOException {
		return actualFactory.newReaderForLocation(location);
	}

	@Override
	public void close() throws IOException {
		actualFactory.close();
	}

	@Override
	public NodeLocation eventLocationToNodeLocation(final XmlEventLocation location, final long additionalOffset) {
		return actualFactory.eventLocationToNodeLocation(location, additionalOffset);
	}
}
