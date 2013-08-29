package at.ac.tuwien.dsg.scaledom.io.impl;

import java.io.File;

import at.ac.tuwien.dsg.scaledom.ScaleDomDocumentSource;

public class FileDocumentSource extends ScaleDomDocumentSource {

	private final File file;
	private final String encoding;

	public FileDocumentSource(final File file, final String encoding) {
		this.file = file;
		this.encoding = encoding;
	}

	public File getFile() {
		return file;
	}

	public String getEncoding() {
		return encoding;
	}
}
