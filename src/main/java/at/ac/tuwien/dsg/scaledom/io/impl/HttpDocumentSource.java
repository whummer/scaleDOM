package at.ac.tuwien.dsg.scaledom.io.impl;

import java.net.URL;

import at.ac.tuwien.dsg.scaledom.ScaleDomDocumentSource;

public class HttpDocumentSource extends ScaleDomDocumentSource {

	private final URL url;
	private final String encoding;

	public HttpDocumentSource(final URL url, final String encoding) {
		this.url = url;
		this.encoding = encoding;
	}

	public URL getUrl() {
		return url;
	}

	public String getEncoding() {
		return encoding;
	}
}
