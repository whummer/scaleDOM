package at.ac.tuwien.dsg.scaledom;

import java.nio.charset.StandardCharsets;

import at.ac.tuwien.dsg.scaledom.io.ReaderFactory;
import at.ac.tuwien.dsg.scaledom.io.impl.DelegatorReaderFactory;
import at.ac.tuwien.dsg.scaledom.lazy.LazyLoadingStrategy;
import at.ac.tuwien.dsg.scaledom.lazy.impl.StepLazyLoadingStrategy;
import at.ac.tuwien.dsg.scaledom.parser.XmlParser;
import at.ac.tuwien.dsg.scaledom.parser.impl.StaxXmlParser;

/**
 * General constants for ScaleDOM.
 * 
 * @author Dominik Rauch
 */
public final class ScaleDom {

	// Internal constants

	/** Prefix for all attributes. */
	private static final String ATTRIBUTE_PREFIX = ScaleDom.class.getPackage().getName() + ".";

	// ScaleDOM configuration options

	/**
	 * <code>XmlParser</code> implementation.
	 */
	public static final String ATTRIBUTE_XMLPARSER_IMPLEMENTATION = ATTRIBUTE_PREFIX + XmlParser.class.getName();

	/**
	 * <code>ReaderFactory</code> implementation.<br/>
	 * Used by the parser to read from the underlying file. Implementations may hold the file open, returned
	 * <code>Reader</code> are only requested one after the other.
	 */
	public static final String ATTRIBUTE_READERFACTORY_IMPLEMENTATION = ATTRIBUTE_PREFIX
			+ ReaderFactory.class.getName();

	/**
	 * <code>LazyLoadingStrategy</code> implementation.<br/>
	 * Determines whether parsed nodes should be loaded into the DOM or be ignored for now.
	 */
	public static final String ATTRIBUTE_LAZYLOADINGSTRATEGY_IMPLEMENTATION = ATTRIBUTE_PREFIX
			+ LazyLoadingStrategy.class.getName();

	/** Default encoding to be used if not specified in <code>InputSource</code>. */
	public static final String ATTRIBUTE_DEFAULTENCODING = ATTRIBUTE_PREFIX + "DefaultEncoding";

	// ScaleDOM default configuration option values

	/** Default XmlParser implementation. */
	static final Class<? extends XmlParser> DEFAULT_XMLPARSER_IMPLEMENTATION = StaxXmlParser.class;

	/** Default ReaderFactory implementation. */
	static final Class<? extends ReaderFactory> DEFAULT_READERFACTORY_IMPLEMENTATION = DelegatorReaderFactory.class;

	/** Default LazyLoadingStrategy implementation. */
	static final Class<? extends LazyLoadingStrategy> DEFAULT_LAZYLOADINGSTRATEGY_IMPLEMENTATION = StepLazyLoadingStrategy.class;

	/** Default DefaultEncoding. */
	static final String DEFAULT_DEFAULTENCODING = StandardCharsets.ISO_8859_1.name();
}
