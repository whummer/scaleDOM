package at.ac.tuwien.dsg.scaledom.test.perf;

import java.io.File;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import at.ac.tuwien.dsg.scaledom.test.perf.TestData.TestFile;
import at.ac.tuwien.dsg.scaledom.test.perf.util.MeasuredTask;
import at.ac.tuwien.dsg.scaledom.test.perf.util.dom.FullOutputCallback;
import at.ac.tuwien.dsg.scaledom.util.DOMTraverser;

public class XPathTest extends TestBase {

	// <Configuration>
	private final static TestFile TESTFILE = TestData.getFile(TestData.FILE_BOOKS);
	private final static File FILE = TESTFILE.file;
	private final static String FILE_ENCODING = TESTFILE.encoding;
	private final static String XPATH = TestData.getXPath(TestData.XPATH_BOOKS);
	private final static boolean DEEP = false;
	private final static boolean EAGER = false;
	// </Configuration>

	public static void main(final String[] args) {
		try {
			final Document doc = parseDocument(FILE, FILE_ENCODING);

			// Execute XPath
			final Node result = new MeasuredTask<Node>("XPath execution") {
				@Override
				protected Node runTask() throws Exception {
					final XPathFactory xpf = XPathFactory.newInstance();
					final XPath xp = xpf.newXPath();
					final XPathExpression xpe = xp.compile(XPATH);
					return (Node) xpe.evaluate(doc, XPathConstants.NODE);
				}
			}.run();

			// Output results
			if (result != null) {
				final DOMTraverser traverser = new DOMTraverser(DEEP, EAGER, new FullOutputCallback());
				traverser.traverse(result);
			} else {
				System.out.println("XPath yielded no results.");
			}
		} catch (final Exception ex) {
			ex.printStackTrace(System.err);
		}
	}
}
