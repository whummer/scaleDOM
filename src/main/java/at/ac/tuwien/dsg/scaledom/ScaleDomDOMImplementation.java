package at.ac.tuwien.dsg.scaledom;

import static com.google.common.base.Preconditions.checkNotNull;

import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

/**
 * ScaleDOM implementation of <code>DOMImplementation</code>. As ScaleDOM does not reimplement the whole DOM
 * implementation from scratch, all calls are forwarded to the implementation ScaleDOM is based upon.
 * 
 * @author Dominik Rauch
 * @see DOMImplementation
 */
public class ScaleDomDOMImplementation implements DOMImplementation {

	/** Base DOM implementation */
	private final DOMImplementation baseDomImplementation;

	/**
	 * Default constructor.
	 */
	ScaleDomDOMImplementation(final DOMImplementation baseDomImplementation) {
		checkNotNull(baseDomImplementation, "Argument baseDomImplementation must not be null.");
		this.baseDomImplementation = baseDomImplementation;
	}

	@Override
	public Document createDocument(final String namespaceURI, final String qualifiedName, final DocumentType doctype)
			throws DOMException {
		return baseDomImplementation.createDocument(namespaceURI, qualifiedName, doctype);
	}

	@Override
	public DocumentType createDocumentType(final String qualifiedName, final String publicId, final String systemId)
			throws DOMException {
		return baseDomImplementation.createDocumentType(qualifiedName, publicId, systemId);
	}

	@Override
	public Object getFeature(final String feature, final String version) {
		return baseDomImplementation.getFeature(feature, version);
	}

	@Override
	public boolean hasFeature(final String feature, final String version) {
		return baseDomImplementation.hasFeature(feature, version);
	}
}
