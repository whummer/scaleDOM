package at.ac.tuwien.dsg.scaledom.dom;

import org.w3c.dom.Document;

import at.ac.tuwien.dsg.scaledom.ScaleDomDocumentSource;

public interface ScaleDomDocumentInterface extends Document {

	/**
	 * Returns the underlying document source.
	 * 
	 * @return the underlying document source.
	 */
	ScaleDomDocumentSource getDocumentSource();

	/**
	 * Returns whether the <code>ScaleDomDocument</code> is still consistent. If consistency is lost (e.g. due to memory
	 * shortage during node loading), an error is logged to the logging system.
	 * 
	 * @return true if the document is still consistent, otherwise false.
	 */
	boolean isConsistent();

	/**
	 * Returns the currently configured load type.
	 * 
	 * @return the currently configured load type.
	 */
	LoadType getLoadType();

	/**
	 * Sets the load type to be used.
	 * 
	 * @param loadType the desired load type.
	 * @throws IllegalArgumentException If loadType is LoadType.INITIAL.
	 */
	void setLoadType(final LoadType loadType);
}
