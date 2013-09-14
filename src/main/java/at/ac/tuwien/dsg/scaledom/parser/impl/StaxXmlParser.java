package at.ac.tuwien.dsg.scaledom.parser.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.Reader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.Location;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.validation.Schema;

import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import at.ac.tuwien.dsg.scaledom.io.ReaderWithSystemID;
import at.ac.tuwien.dsg.scaledom.parser.XmlEventCharLocation;
import at.ac.tuwien.dsg.scaledom.parser.XmlEventLocation;
import at.ac.tuwien.dsg.scaledom.parser.XmlParser;
import at.ac.tuwien.dsg.scaledom.parser.XmlParserEventListener;

/**
 * <code>XmlParser</code> implementation using StAX.<br/>
 * Warnings:
 * <ul>
 * <li>DocumentBuilderFactory.isXIncludeAware is ignored</li>
 * <li>DocumentBuilderFactory.schema is ignored</li>
 * <li>EntityResolver and ErrorHandler are used as well as possible</li>
 * </ul>
 * 
 * @author Dominik Rauch
 */
public class StaxXmlParser extends XmlParser {

	/** StAX input factory. */
	private final XMLInputFactory inputFactory;

	private final boolean isIgnoringComments;
	private final boolean isIgnoringElementContentWhitespace;

	/**
	 * Default constructor.
	 * 
	 * @see XmlParser#XmlParser(DocumentBuilderFactory)
	 */
	public StaxXmlParser(final DocumentBuilderFactory factory) {
		super(factory);
		inputFactory = XMLInputFactory.newInstance();

		inputFactory.setProperty(XMLInputFactory.IS_COALESCING, factory.isCoalescing());
		inputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, factory.isNamespaceAware());
		// TODO: Are the following two properties correctly mapped?
		inputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, factory.isExpandEntityReferences());
		inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, factory.isExpandEntityReferences());
		inputFactory.setProperty(XMLInputFactory.IS_VALIDATING, factory.isValidating());
		isIgnoringComments = factory.isIgnoringComments();
		isIgnoringElementContentWhitespace = factory.isIgnoringElementContentWhitespace();
	}

	@Override
	public boolean parse(final Reader reader, final XmlParserEventListener eventListener) throws SAXException,
			IOException {
		checkNotNull(reader, "Argument reader must not be null.");
		checkNotNull(eventListener, "Argument eventListener must not be null.");

		XMLEventReader xmlEventReader = null;
		try {
			// Create event reader
			if(reader instanceof ReaderWithSystemID) {
				ReaderWithSystemID _reader = (ReaderWithSystemID)reader;
				xmlEventReader = inputFactory.createXMLEventReader(
						_reader.getSystemID(), _reader.getReader());
			} else {
				xmlEventReader = inputFactory.createXMLEventReader(reader);
			}

			// Do parsing
			while (xmlEventReader.hasNext()) {
				final XMLEvent event = xmlEventReader.nextEvent();

				if (isIgnoringComments && event.getEventType() == XMLStreamConstants.COMMENT) {
					continue;
				}

				if (isIgnoringElementContentWhitespace && event.isCharacters()
						&& event.asCharacters().isIgnorableWhiteSpace()) {
					continue;
				}

				// Unfortunately StAX is not able to provide byte offsets
				final long startingCharOffset = event.getLocation().getCharacterOffset();
				final XmlEventLocation location = new XmlEventCharLocation(startingCharOffset);

				final boolean abortParsing = eventListener.process(event, location);
				if (abortParsing) {
					return false;
				}
			}

			return true;
		} catch (final XMLStreamException ex) {
			throw new SAXException(ex);
		} finally {
			if (xmlEventReader != null) {
				try {
					// Close event reader
					xmlEventReader.close();
				} catch (final XMLStreamException ex) {
					throw new IOException(ex);
				}
			}
		}
	}

	@Override
	public Schema getSchema() {
		return null;
	}

	@Override
	public boolean isCoalescing() {
		return (boolean)(Boolean) inputFactory.getProperty(XMLInputFactory.IS_COALESCING);
	}

	@Override
	public boolean isExpandEntityReferences() {
		// TODO: Is the following property correctly mapped?
		return (boolean)(Boolean) inputFactory.getProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES);
	}

	@Override
	public boolean isIgnoringComments() {
		return isIgnoringComments;
	}

	@Override
	public boolean isIgnoringElementContentWhitespace() {
		return isIgnoringElementContentWhitespace;
	}

	@Override
	public boolean isNamespaceAware() {
		return (boolean)(Boolean) inputFactory.getProperty(XMLInputFactory.IS_NAMESPACE_AWARE);
	}

	@Override
	public boolean isValidating() {
		return (boolean)(Boolean) inputFactory.getProperty(XMLInputFactory.IS_VALIDATING);
	}

	@Override
	public boolean isXIncludeAware() {
		return false;
	}

	@Override
	public void setEntityResolver(final EntityResolver er) {
		checkNotNull(er, "Argument er must not be null.");

		inputFactory.setXMLResolver(new XMLResolver() {
			@Override
			public Object resolveEntity(final String publicID, final String systemID, final String baseURI,
					final String namespace) throws XMLStreamException {
				try {
					return er.resolveEntity(publicID, systemID);
				} catch (final SAXException ex) {
					throw new XMLStreamException(ex);
				} catch (final IOException ex) {
					throw new XMLStreamException(ex);
				}
			}
		});
	}

	@Override
	public void setErrorHandler(final ErrorHandler eh) {
		checkNotNull(eh, "Argument eh must not be null.");

		inputFactory.setXMLReporter(new XMLReporter() {
			@Override
			public void report(final String message, final String errorType, final Object relatedInformation,
					final Location location) throws XMLStreamException {
				try {
					eh.error(new SAXParseException(message, location.getPublicId(), location.getSystemId(), location
							.getLineNumber(), location.getColumnNumber()));
				} catch (final SAXException ex) {
					throw new XMLStreamException(ex);
				}
			}
		});
	}
}
