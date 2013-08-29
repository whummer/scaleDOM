package at.ac.tuwien.dsg.scaledom;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import at.ac.tuwien.dsg.scaledom.io.ReaderFactory;
import at.ac.tuwien.dsg.scaledom.lazy.LazyLoadingStrategy;
import at.ac.tuwien.dsg.scaledom.parser.XmlParser;
import at.ac.tuwien.dsg.scaledom.util.ComponentFactory;

/**
 * ScaleDOM implementation of the <code>DocumentBuilderFactory</code>. Utilizes the <code>attributes</code> map for
 * configuration, see {@link ScaleDom} for possible configuration options.<br/>
 * Warnings:
 * <ul>
 * <li>DocumentBuilderFactory.isXIncludeAware is not supported</li>
 * <li>DocumentBuilderFactory.schema is not supported</li>
 * <li>http://javax.xml.XMLConstants/feature/secure-processing feature has no impact</li>
 * </ul>
 * 
 * @author Dominik Rauch
 * @see DocumentBuilderFactory
 * @see ScaleDom
 */
public class ScaleDomDocumentBuilderFactory extends DocumentBuilderFactory {

	private final Map<String, Object> attributes;
	private final Map<String, Boolean> features;

	/**
	 * Default constructor.
	 */
	public ScaleDomDocumentBuilderFactory() {
		attributes = new HashMap<>();
		features = new HashMap<>();

		// Add features which are required to be supported
		features.put(XMLConstants.FEATURE_SECURE_PROCESSING, false);

		// Add all configuration options using defaults as initial values
		attributes.put(ScaleDom.ATTRIBUTE_XMLPARSER_IMPLEMENTATION, ScaleDom.DEFAULT_XMLPARSER_IMPLEMENTATION);
		attributes.put(ScaleDom.ATTRIBUTE_READERFACTORY_IMPLEMENTATION, ScaleDom.DEFAULT_READERFACTORY_IMPLEMENTATION);
		attributes.put(ScaleDom.ATTRIBUTE_LAZYLOADINGSTRATEGY_IMPLEMENTATION,
				ScaleDom.DEFAULT_LAZYLOADINGSTRATEGY_IMPLEMENTATION);
		attributes.put(ScaleDom.ATTRIBUTE_DEFAULTENCODING, ScaleDom.DEFAULT_DEFAULTENCODING);
	}

	@Override
	@SuppressWarnings("unchecked")
	public DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
		try {
			final ComponentFactory componentFactory = new ComponentFactory();

			// Bind XmlParser instance
			componentFactory.bind(XmlParser.class,
					(Class<? extends XmlParser>) attributes.get(ScaleDom.ATTRIBUTE_XMLPARSER_IMPLEMENTATION), this);
			// Bind ReaderFactory implementation type
			componentFactory.bind(ReaderFactory.class,
					(Class<? extends ReaderFactory>) attributes.get(ScaleDom.ATTRIBUTE_READERFACTORY_IMPLEMENTATION));
			// Bind LazyLoadingStrategy implementation type
			componentFactory.bind(LazyLoadingStrategy.class, (Class<? extends LazyLoadingStrategy>) attributes
					.get(ScaleDom.ATTRIBUTE_LAZYLOADINGSTRATEGY_IMPLEMENTATION));

			final String defaultEncoding = (String) attributes.get(ScaleDom.ATTRIBUTE_DEFAULTENCODING);

			return new ScaleDomDocumentBuilder(componentFactory, defaultEncoding);
		} catch (final InstantiationException ex) {
			throw new ParserConfigurationException("Component 'XmlParser' could not be instantiated.");
		}
	}

	@Override
	public Object getAttribute(final String name) throws IllegalArgumentException {
		checkNotNull(name, "Argument name must not be null.");
		checkArgument(attributes.containsKey(name), "Attribute '%s' is not recognized.", name);

		return attributes.get(name);
	}

	@Override
	public void setAttribute(final String name, final Object value) throws IllegalArgumentException {
		checkNotNull(name, "Argument name must not be null.");
		checkArgument(attributes.containsKey(name), "Attribute '%s' is not recognized.", name);
		checkNotNull(value, "Argument value must not be null.");

		attributes.put(name, value);
	}

	@Override
	public boolean getFeature(final String name) throws ParserConfigurationException {
		checkNotNull(name, "Argument name must not be null.");
		if (!features.containsKey(name)) {
			throw new ParserConfigurationException("Feature '" + name + "' is not supported.");
		}

		return features.get(name);
	}

	@Override
	public void setFeature(final String name, final boolean value) throws ParserConfigurationException {
		checkNotNull(name, "Argument name must not be null.");
		if (!features.containsKey(name)) {
			throw new ParserConfigurationException("Feature '" + name + "' is not supported.");
		}
		checkNotNull(value, "Argument value must not be null.");

		features.put(name, value);
	}
}
