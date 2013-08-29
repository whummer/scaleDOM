package at.ac.tuwien.dsg.scaledom.test.perf;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import at.ac.tuwien.dsg.scaledom.test.perf.util.MeasuredTask;

public abstract class TestBase {

	protected static Document parseDocument(final File file, final String fileEncoding) throws Exception {	
		// Create document builder factory
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		// Configure document builder factory
		dbf.setNamespaceAware(true);
		// dbf.setValidating(true);

		// Create document builder
		final DocumentBuilder db = dbf.newDocumentBuilder();

		// Parse document
		final Document doc = new MeasuredTask<Document>("Initial parsing") {
			@Override
			protected Document runTask() throws Exception {
				return db.parse(file);
			}
		}.run();

		return doc;
	}
}
