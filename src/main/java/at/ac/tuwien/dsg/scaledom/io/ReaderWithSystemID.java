package at.ac.tuwien.dsg.scaledom.io;

import java.io.IOException;
import java.io.Reader;

/**
 * Wraps a Reader to hold the systemID string of the underlying document.
 * 
 * @author Waldemar Hummer
 */
public class ReaderWithSystemID extends Reader {

	private String systemID;
	private Reader reader;

	public ReaderWithSystemID(String systemID, Reader reader) {
		this.systemID = systemID;
		this.reader = reader;
	}
	
	public void close() throws IOException {
		reader.close();
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		return reader.read(cbuf, off, len);
	}

	public String getSystemID() {
		return systemID;
	}

	public Reader getReader() {
		return reader;
	}
}
