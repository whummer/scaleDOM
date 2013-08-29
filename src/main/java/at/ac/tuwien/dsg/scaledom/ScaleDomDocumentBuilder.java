package at.ac.tuwien.dsg.scaledom;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.xml.parsers.DocumentBuilder;

import org.apache.xerces.dom.CoreDOMImplementationImpl;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import at.ac.tuwien.dsg.scaledom.dom.ScaleDomDocument;
import at.ac.tuwien.dsg.scaledom.parser.XmlParser;
import at.ac.tuwien.dsg.scaledom.util.ComponentFactory;
import at.ac.tuwien.dsg.scaledom.util.InputSourceUtils;

/**
 * The <code>ScaleDomDocumentBuilder</code> constructs DOM documents (<code>ScaleDomDocument</code>) based upon the
 * Apache Xerces DOM implementation.
 * 
 * @author Dominik Rauch
 * @see DocumentBuilder
 */
public class ScaleDomDocumentBuilder extends DocumentBuilder {

	private final XmlParser parser;
	private final ComponentFactory componentFactory;
	private final String defaultEncoding;

	/**
	 * Default constructor.
	 * 
	 * @param componentFactory a factory to obtain required sub components.
	 * @param defaultEncoding the default encoding to be used.
	 */
	ScaleDomDocumentBuilder(final ComponentFactory componentFactory, final String defaultEncoding) {
		checkNotNull(componentFactory, "Argument componentFactory must not be null.");
		checkNotNull(defaultEncoding, "Argument defaultEncoding must not be null.");
		checkArgument(Charset.isSupported(defaultEncoding), "Default encoding '%s' is not supported.", defaultEncoding);

		this.parser = componentFactory.getInstance(XmlParser.class);
		this.componentFactory = componentFactory;
		this.defaultEncoding = defaultEncoding;
	}

	@Override
	public Document parse(final InputSource is) throws SAXException, IOException {
		checkNotNull(is, "Argument is must not be null.");

		if (is.getCharacterStream() != null || is.getByteStream() != null) {
			throw new IOException("ScaleDOM supports only file-based input sources.");
		}

		try {
			final ScaleDomDocumentSource source = InputSourceUtils.inputSourceToDocumentSource(is, defaultEncoding);
			return new ScaleDomDocument(source, parser, componentFactory);
		} catch (final InstantiationException ex) {
			throw new SAXException(ex);
		}
	}

	/**
	 * Use this non-JAXP method if you want to parse a non-File source. Make sure you have a <code>ReaderFactory</code>
	 * configured, which is able to work with the given implementation of <code>ScaleDomDocumentSource</code>.
	 * 
	 * @param source the XML source.
	 * @return a new DOM Document object.
	 * @throws SAXException If any parse errors occur.
	 * @throws IOException If any I/O errors occur.
	 */
	public Document parse(final ScaleDomDocumentSource source) throws SAXException, IOException {
		checkNotNull(source, "Argument source must not be null.");

		try {
			return new ScaleDomDocument(source, parser, componentFactory);
		} catch (final InstantiationException ex) {
			throw new SAXException(ex);
		}
	}

	@Override
	public DOMImplementation getDOMImplementation() {
		// ScaleDOM is based upon Apache Xerces
		return new ScaleDomDOMImplementation(new CoreDOMImplementationImpl());
	}

	@Override
	public boolean isNamespaceAware() {
		return parser.isNamespaceAware();
	}

	@Override
	public boolean isValidating() {
		return parser.isValidating();
	}

	@Override
	public Document newDocument() {
		// No lazy loading possible if DOM is constructed from scratch => makes no sense
		throw new UnsupportedOperationException("ScaleDOM does not support DOM construction from scratch.");
	}

	@Override
	public void setEntityResolver(final EntityResolver er) {
		parser.setEntityResolver(er);
	}

	@Override
	public void setErrorHandler(final ErrorHandler eh) {
		parser.setErrorHandler(eh);
	}
}
