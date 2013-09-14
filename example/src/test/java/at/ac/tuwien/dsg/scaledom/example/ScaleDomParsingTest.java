package at.ac.tuwien.dsg.scaledom.example;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import at.ac.tuwien.dsg.scaledom.ScaleDomDocumentBuilderFactory;
import at.ac.tuwien.dsg.scaledom.io.impl.DelegatorReaderFactory;
import at.ac.tuwien.dsg.scaledom.io.impl.HttpDocumentSource;
import at.ac.tuwien.dsg.scaledom.util.DOMTraverser;
import at.ac.tuwien.dsg.scaledom.util.DOMTraverserCallback;


/**
 * A simple example which illustrates XML parsing using scaleDOM.
 * This class can be run either as JUnit test or via the main method.
 * 
 * @author Waldemar Hummer
 */
public class ScaleDomParsingTest {

	String tmpXmlFile = System.getProperty("user.dir") + 
			File.separator + UUID.randomUUID().toString() + ".xml";

	@Before
	public void setup() throws Exception {
		System.out.println("INFO: Generating big XML file, please be patient: " + tmpXmlFile);
		XmlFileGenerator.generate(10, 7, new FileOutputStream(tmpXmlFile));
	}

	@Test
	public void parseFile() throws Exception {
		// Parse and traverse XML file with ScaleDOM
		System.setProperty("javax.xml.parsers.DocumentBuilderFactory", 
				ScaleDomDocumentBuilderFactory.class.getName());
		System.out.println("INFO: Parsing with ScaleDOM, please be patient (may take several minutes)...");
		doParseFile();

		// Parse and traverse XML file with standard XML parser (Xerces)
		System.setProperty("javax.xml.parsers.DocumentBuilderFactory", 
				DocumentBuilderFactoryImpl.class.getName());
		System.out.println("INFO: Parsing with Xerces, please be patient (may take several minutes)...");
		try {
			System.gc();
			doParseFile();
		} catch (OutOfMemoryError t) {
			System.out.println("INFO: Xerces was unable to parse the file due to OutOfMemoryError. (This is EXPECTED!)");
		}

	}

	@Ignore
	private void doParseFile() throws Exception {
		// Create document builder factory
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		// Configure document builder factory
		dbf.setNamespaceAware(true);
		// Create document builder
		final DocumentBuilder db = dbf.newDocumentBuilder();
		// Parse document
		final Document doc = db.parse("file://" + tmpXmlFile);
		// Traverse the entire DOM
		new DOMTraverser(new DOMTraverserCallback() {
			// empty traverser (ignore all traversal events)
			public void nodeTraversed(Document doc, Node node, int level) {}
		}).traverse(doc);
	}

	@Test
	public void testParseHttpFile() throws Exception {
		new DelegatorReaderFactory(new HttpDocumentSource(
				new URL("http://www.w3.org/2001/XMLSchema.xsd"), 
				StandardCharsets.ISO_8859_1.name()));
		// Parse and traverse XML file with ScaleDOM
		System.setProperty("javax.xml.parsers.DocumentBuilderFactory", 
				ScaleDomDocumentBuilderFactory.class.getName());
		System.out.println("INFO: Parsing XML file from HTTP connection, please be patient...");
		// Create document builder factory
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		// Configure document builder factory
		dbf.setNamespaceAware(true);
		// Create document builder
		final DocumentBuilder db = dbf.newDocumentBuilder();
		// Parse document
		final Document doc = db.parse("http://www.w3.org/2001/XMLSchema.xsd");
		// Traverse the entire DOM
		new DOMTraverser(new DOMTraverserCallback() {
			// empty traverser (ignore all traversal events)
			public void nodeTraversed(Document doc, Node node, int level) {
				//System.out.println(node.getLocalName());
			}
		}).traverse(doc);
		System.out.println("INFO: Done parsing XML file from HTTP connection.");
	}

	@After
	public void tearDown() {
		System.out.println("INFO: Deleting XML file: " + tmpXmlFile);
		new File(tmpXmlFile).delete();
	}

	public static void main(String[] args) throws Exception {
		ScaleDomParsingTest t = new ScaleDomParsingTest();
		//t.setup();
		//t.parseFile();
		t.testParseHttpFile();
		t.tearDown();
	}
}
