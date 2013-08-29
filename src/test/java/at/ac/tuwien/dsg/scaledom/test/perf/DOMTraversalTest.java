package at.ac.tuwien.dsg.scaledom.test.perf;

import java.io.File;

import org.w3c.dom.Document;

import at.ac.tuwien.dsg.scaledom.test.perf.TestData.TestFile;
import at.ac.tuwien.dsg.scaledom.test.perf.util.MeasuredTask;
import at.ac.tuwien.dsg.scaledom.test.perf.util.dom.DOMTraverser;
import at.ac.tuwien.dsg.scaledom.test.perf.util.dom.FirstOnLevelCallback;
import at.ac.tuwien.dsg.scaledom.test.perf.util.dom.FullOutputCallback;
import at.ac.tuwien.dsg.scaledom.test.perf.util.dom.MinimalOutputCallback;
import at.ac.tuwien.dsg.scaledom.test.perf.util.dom.NoOpCallback;

public class DOMTraversalTest extends TestBase {

	// <Configuration>
	private final static TestFile TESTFILE = TestData.getFile(TestData.FILE_TREEBANK);
	private final static File FILE = TESTFILE.file;
	private final static String FILE_ENCODING = TESTFILE.encoding;
	private final static boolean DEEP = true;
	// </Configuration>

	@SuppressWarnings("unused")
	public static void main(final String[] args) {
		try {
			final Document doc = parseDocument(FILE, FILE_ENCODING);

			// Traverse document
			new MeasuredTask<Void>("DOM traversal") {
				@Override
				protected Void runTask() throws Exception {
					final DOMTraverser noOpTraverser = new DOMTraverser(DEEP, new NoOpCallback());
					final DOMTraverser minimalOutputTraverser = new DOMTraverser(DEEP, new MinimalOutputCallback(3));
					final DOMTraverser firstOnLeveltraverser = new DOMTraverser(DEEP, new FirstOnLevelCallback(2, 2));
					final DOMTraverser fullOutputTraverser = new DOMTraverser(DEEP, new FullOutputCallback());

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
