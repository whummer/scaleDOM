package at.ac.tuwien.dsg.scaledom.test;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import at.ac.tuwien.dsg.scaledom.ScaleDom;
import at.ac.tuwien.dsg.scaledom.ScaleDomDocumentBuilder;
import at.ac.tuwien.dsg.scaledom.ScaleDomDocumentBuilderFactory;
import at.ac.tuwien.dsg.scaledom.io.impl.FileDocumentSource;
import at.ac.tuwien.dsg.scaledom.lazy.LazyLoadingStrategy;
import at.ac.tuwien.dsg.scaledom.test.util.MeasuredTask;

public abstract class TestBase {

	// Only true is supported until Apache Xerces is repackaged in ScaleDOM!
	public static boolean USE_SCALEDOM = true;

	protected static Document parseDocument(final File file, final String fileEncoding) throws Exception {
		return parseDocument(file, fileEncoding, null);
	}

	protected static Document parseDocument(final File file, final String fileEncoding,
			final Class<? extends LazyLoadingStrategy> strategy) throws Exception {
		if(!USE_SCALEDOM) {
			System.err.println("Only ScaleDOM is supported until Apache Xerces is repackaged in ScaleDOM! Currently, you may not use Apache Xerces directly!");
			System.err.println("Please set USE_SCALEDOM = true!");
		}
		
		// Create ScaleDOM document builder factory
		final DocumentBuilderFactory dbf;

		if (USE_SCALEDOM) {
			dbf = DocumentBuilderFactory.newInstance(ScaleDomDocumentBuilderFactory.class.getName(),
					ScaleDomDocumentBuilderFactory.class.getClassLoader());
		} else {
			dbf = DocumentBuilderFactory.newInstance();
		}

		// Configure document builder factory
		dbf.setNamespaceAware(true);
		dbf.setValidating(true);
		if (strategy != null) {
			dbf.setAttribute(ScaleDom.ATTRIBUTE_LAZYLOADINGSTRATEGY_IMPLEMENTATION, strategy);
		}

		// Create ScaleDOM document builder
		final DocumentBuilder db = dbf.newDocumentBuilder();

		// Parse document
		final Document doc = new MeasuredTask<Document>("Initial parsing") {
			@Override
			protected Document runTask() throws Exception {
				if(USE_SCALEDOM) {
					// Use internal API instead of JAXP to be able to set encoding
					final FileDocumentSource fds = new FileDocumentSource(file, fileEncoding);
					return ((ScaleDomDocumentBuilder) db).parse(fds);
				} else {
					return db.parse(file);
				}
			}
		}.run();

		return doc;
	}
}
