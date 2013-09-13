package at.ac.tuwien.dsg.scaledom.test.perf;

import java.io.File;

import org.w3c.dom.Document;

import at.ac.tuwien.dsg.scaledom.ScaleDomDocumentBuilderFactory;
import at.ac.tuwien.dsg.scaledom.test.perf.TestData.TestFile;
import at.ac.tuwien.dsg.scaledom.test.perf.util.MeasuredTask;
import at.ac.tuwien.dsg.scaledom.test.perf.util.dom.FirstOnLevelCallback;
import at.ac.tuwien.dsg.scaledom.test.perf.util.dom.FullOutputCallback;
import at.ac.tuwien.dsg.scaledom.test.perf.util.dom.MinimalOutputCallback;
import at.ac.tuwien.dsg.scaledom.test.perf.util.dom.NoOpCallback;
import at.ac.tuwien.dsg.scaledom.util.DOMTraverser;

public class DOMTraversalTest extends TestBase {

	// <Configuration>
	private final static TestFile TESTFILE = TestData.getFile(TestData.FILE_TREEBANK);
	private final static File FILE = TESTFILE.file;
	private final static String FILE_ENCODING = TESTFILE.encoding;
	private final static boolean DEEP = true;
	private final static boolean EAGER = true;
	// </Configuration>

	@SuppressWarnings("unused")
	public static void main(final String[] args) {
		try {
			System.setProperty("javax.xml.parsers.DocumentBuilderFactory", 
					ScaleDomDocumentBuilderFactory.class.getName());
			final Document doc = parseDocument(FILE, FILE_ENCODING);

			// Traverse document
			new MeasuredTask<Void>("DOM traversal") {
				@Override
				protected Void runTask() throws Exception {
					final DOMTraverser noOpTraverser = new DOMTraverser(DEEP, EAGER, new NoOpCallback());
					final DOMTraverser minimalOutputTraverser = new DOMTraverser(DEEP, EAGER, new MinimalOutputCallback(3));
					final DOMTraverser firstOnLeveltraverser = new DOMTraverser(DEEP, EAGER, new FirstOnLevelCallback(2, 2));
					final DOMTraverser fullOutputTraverser = new DOMTraverser(DEEP, EAGER, new FullOutputCallback());

					noOpTraverser.traverse(doc);
					System.out.println();
					return null;
				}
			}.run();

		} catch (final Exception ex) {
			ex.printStackTrace(System.err);
		}
	}
}
