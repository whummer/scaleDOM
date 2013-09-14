package at.ac.tuwien.dsg.scaledom.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;

import org.xml.sax.InputSource;

import at.ac.tuwien.dsg.scaledom.ScaleDomDocumentSource;
import at.ac.tuwien.dsg.scaledom.io.impl.FileDocumentSource;
import at.ac.tuwien.dsg.scaledom.io.impl.HttpDocumentSource;

/**
 * Various utilities for <code>InputSource</code> objects.
 * 
 * @author Dominik Rauch
 */
public class InputSourceUtils {

	/**
	 * Returns a <code>FileDocumentSource</code> implementation of <code>ScaleDomDocumentSource</code> for a given
	 * <code>InputSource</code>.
	 * 
	 * @param is the input source.
	 * @param defaultEncoding default encoding, used if input source does not provide the encoding.
	 * @return a file document source.
	 * @throws IOException If the input source is pointing to an invalid document source.
	 */
	public static ScaleDomDocumentSource inputSourceToDocumentSource(final InputSource is, final String defaultEncoding)
			throws IOException {
		checkNotNull(is, "Argument is must not be null.");
		checkNotNull(is, "Argument defaultEncoding must not be null.");
		checkArgument(Charset.isSupported(defaultEncoding), "Default encoding '%s' is not supported.", defaultEncoding);

		try {
			String uri = is.getSystemId().trim();
			if(uri.startsWith("file://")) {
				// Get File from InputSource
				final File file = new File(new URI(is.getSystemId()));
				final String isEncoding = is.getEncoding();
				final String encoding = isEncoding != null ? isEncoding : defaultEncoding;

				return new FileDocumentSource(file, encoding);
			} else {
				// Get connection from URL
				URL url = new URL(uri);
				final String isEncoding = is.getEncoding();
				final String encoding = isEncoding != null ? isEncoding : defaultEncoding;

				return new HttpDocumentSource(url, encoding);
			}
		} catch (final URISyntaxException ex) {
			throw new IOException(ex);
		}
	}
}
