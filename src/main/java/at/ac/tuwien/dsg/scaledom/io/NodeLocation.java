package at.ac.tuwien.dsg.scaledom.io;

/**
 * Base class for all <code>NodeLocation</code> implementations.<br/>
 * This class is merely a name for a node's location, implementations belong to a specific <code>ReaderFactory</code>
 * implementation which decides what sort of location information is required.
 * 
 * @author Dominik Rauch
 */
public abstract class NodeLocation {

	/**
	 * Many document sources represent nodes as ranges of characters (e.g. files). To correctly create a
	 * <code>Reader</code> it is not only required to know where the node starts but also where the node ends.
	 * 
	 * This method is called with the next node's location as soon as the next node has been parsed.
	 * 
	 * Warning: This method may be called multiple times due to the structure of the load process, the underlying
	 * implementation should use only the data received by the first call!
	 * 
	 * @param location the next node's location.
	 */
	public abstract void setEndLocation(final NodeLocation location);
}
