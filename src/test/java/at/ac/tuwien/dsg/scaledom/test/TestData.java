package at.ac.tuwien.dsg.scaledom.test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class TestData {

	// Default files
	public final static int FILE_BOOKS = 100; // 5 KB, no NS, no DTD - default file
	public final static int FILE_BOOKS_NS = 101; // ? KB, has NS, no DTD - default file for namespace awareness

	// Test XML files
	public final static int FILE_SIMPLE = 200; // 1 KB, no NS, no DTD - very basic example
	public final static int FILE_VARWIDTH = 201; // 1 KB, no NS, no DTD - contains UTF-8 variable-width characters
	public final static int FILE_DTD = 202; // 1 KB, no NS, internal DTD - very basic example, contains an internal DTD
	public final static int FILE_DEEP = 203; // 1 KB, no NS, no DTD - XML with six levels

	// Big XML files
	public final static int FILE_ACTORS = 300; // 14 MB, no NS, no DTD - IMDb actors
	public final static int FILE_DBLP = 301; // 127 MB, no NS, external DTD - bibliography of major computer science
												// magazines
	public final static int FILE_NASA = 302; // 23 MB, no NS, www-external DTD - publicly available datasets
	public final static int FILE_PSD = 303; // 683 MB, no NS, external DTD - integrated collection of functionally
											// annotated protein sequences
	public final static int FILE_TREEBANK = 304; // 82 MB, no NS, no DTD - Wall Street Jorunal speeches (data encrypted
													// tue to copyright)

	// XPaths for default files
	public final static int XPATH_BOOKS = 100;

	public static class TestFile {
		public final File file;
		public final String encoding;

		public TestFile(final File file, final String encoding) {
			this.file = file;
			this.encoding = encoding;
		}
	}

	private final static Map<Integer, TestFile> FILES;
	static {
		FILES = new HashMap<>();
		FILES.put(FILE_BOOKS, new TestFile(new File("xml/books.xml"), StandardCharsets.ISO_8859_1.name()));
		FILES.put(FILE_BOOKS_NS, new TestFile(new File("xml/booksns.xml"), StandardCharsets.ISO_8859_1.name()));
		FILES.put(FILE_SIMPLE, new TestFile(new File("xml/simple.xml"), StandardCharsets.ISO_8859_1.name()));
		FILES.put(FILE_VARWIDTH, new TestFile(new File("xml/varwidth.xml"), StandardCharsets.UTF_8.name()));
		FILES.put(FILE_DTD, new TestFile(new File("xml/dtd.xml"), StandardCharsets.ISO_8859_1.name()));
		FILES.put(FILE_DEEP, new TestFile(new File("xml/deep.xml"), StandardCharsets.ISO_8859_1.name()));
		FILES.put(FILE_ACTORS, new TestFile(new File("xml/big/actors20000.xml"), StandardCharsets.ISO_8859_1.name()));
		FILES.put(FILE_DBLP, new TestFile(new File("xml/big/dblp.xml"), StandardCharsets.ISO_8859_1.name()));
		FILES.put(FILE_NASA, new TestFile(new File("xml/big/nasa.xml"), StandardCharsets.ISO_8859_1.name()));
		FILES.put(FILE_PSD, new TestFile(new File("xml/big/psd7003.xml"), StandardCharsets.ISO_8859_1.name()));
		FILES.put(FILE_TREEBANK, new TestFile(new File("xml/big/treebank_e.xml"), StandardCharsets.ISO_8859_1.name()));
	}

	private final static Map<Integer, String> XPATHS;
	static {
		XPATHS = new HashMap<>();
		XPATHS.put(XPATH_BOOKS, "/catalog");
	}

	public static TestFile getFile(final int fileId) {
		return FILES.get(fileId);
	}

	public static String getXPath(final int xPathId) {
		return XPATHS.get(xPathId);
	}
}
