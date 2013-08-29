package at.ac.tuwien.dsg.scaledom.io;

import at.ac.tuwien.dsg.scaledom.parser.XmlEventLocation;
import at.ac.tuwien.dsg.scaledom.parser.XmlParser;

/**
 * The input to the <code>XmlParser</code> is a <code>Reader</code> created by a <code>ReaderFactory</code>, therefore
 * the parser is not aware of the actual underlying document source. The parser's outputted event locations (
 * <code>XmlEventLocation</code>) are accordingly relative to the given reader object and not relative to the underlying
 * document source. Only the creator of the reader object is therefore able to convert the parser's output location into
 * a real <code>NodeLocation</code> object which can be used later on to re-obtain a reader from the reader factory in
 * case the node has to be reloaded.
 * 
 * @author Dominik Rauch
 * @see XmlParser
 * @see ReaderFactory
 * @see XmlEventLocation
 * @see NodeLocation
 */
public interface NodeLocationFactory {

	/**
	 * Converts the parser's output (an <code>XmlEventLocation</code>) into a <code>NodeLocation</code>.
	 * 
	 * @param location the parser's outputted event location.
	 * @param additionalOffset the reader object created by the reader factory is sometimes extended with an additonal
	 * 
	 *            prefix (e.g. namespace declarations), the length of this prefix in bytes is given by this parameter
	 *            and should be subtracted from the location to retrieve the index within the reader created by the
	 *            reader factory.
	 * @return a <code>NodeLocation</code> which can be used to re-obtain a reader from the <code>ReaderFactory</code>.
	 */
	NodeLocation eventLocationToNodeLocation(final XmlEventLocation location, final long additionalOffset);
}
