package at.ac.tuwien.dsg.scaledom.parser;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.Reader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;

import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

/**
 * Base class for all <code>XmlParser</code> implementations.<br/>
 * <code>ScaleDomDocument</code> uses the smae XmlParser for initial loading as well as node reloading. Therefore
 * consecutive calls to parse() must be possible, however, implementations do not have to be thread-safe. Furthermore
 * <code>ScaleDomDocumentBuilder</code> also uses the very same XmlParser instance to answer configuration queries from
 * the user.
 * 
 * @author Dominik Rauch
 */
public abstract class XmlParser {

	/**
	 * Default constructor.
	 * 
	 * Should configure the parser according to the configuration options set in the given
	 * <code>DocumentBuilderFactory</code>. If an implementation does not support a feature, it may ignore this feature.
	 * The actual parser configuration must therefore be queried with all the get* and is* methods on this interface.
	 * 
	 * Note: The very same constructor signature has to be provided by *all* implementation classes.
	 * 
	 * @param factory the factory which holds the requested configuration.
	 */
	public XmlParser(final DocumentBuilderFactory factory) {
		checkNotNull(factory, "Argument factory must not be null.");
	}

	/**
	 * Parses the <code>source</code> and calls the <code>eventListener</code> accordingly on each event.
	 * 
	 * @param reader the XML source.
	 * @param eventListener the observer, called for each occurring XmlEvent.
	 * @return true if the event listener processed all events, false if the event listener demanded an abortion.
	 * @throws SAXException If any parse error occurs.
	 * @throws IOException If any I/O error occurs.
	 */
	public abstract boolean parse(final Reader reader, final XmlParserEventListener eventListener) throws SAXException,
			IOException;

	/**
	 * @see javax.xml.parsers.DocumentBuilder#getSchema()
	 * @see javax.xml.parsers.DocumentBuilderFactory#getSchema()
	 */
	public abstract Schema getSchema();

	/**
	 * @see javax.xml.parsers.DocumentBuilderFactory#isCoalescing()
	 */
	public abstract boolean isCoalescing();

	/**
	 * @see javax.xml.parsers.DocumentBuilderFactory#isExpandEntityReferences()
	 */
	public abstract boolean isExpandEntityReferences();

	/**
	 * @see javax.xml.parsers.DocumentBuilderFactory#isIgnoringComments()
	 */
	public abstract boolean isIgnoringComments();

	/**
	 * @see javax.xml.parsers.DocumentBuilderFactory#isIgnoringElementContentWhitespace()
	 */
	public abstract boolean isIgnoringElementContentWhitespace();

	/**
	 * @see javax.xml.parsers.DocumentBuilder#isNamespaceAware()
	 * @see javax.xml.parsers.DocumentBuilderFactory#isNamespaceAware()
	 */
	public abstract boolean isNamespaceAware();

	/**
	 * @see javax.xml.parsers.DocumentBuilder#isValidating()
	 * @see javax.xml.parsers.DocumentBuilderFactory#isValidating()
	 */
	public abstract boolean isValidating();

	/**
	 * @see javax.xml.parsers.DocumentBuilder#isXIncludeAware()
	 * @see javax.xml.parsers.DocumentBuilderFactory#isXIncludeAware()
	 */
	public abstract boolean isXIncludeAware();

	/**
	 * @see javax.xml.parsers.DocumentBuilder#setEntityResolver(EntityResolver)
	 */
	public abstract void setEntityResolver(final EntityResolver er);

	/**
	 * @see javax.xml.parsers.DocumentBuilder#setErrorHandler(ErrorHandler)
	 */
	public abstract void setErrorHandler(final ErrorHandler eh);
}
