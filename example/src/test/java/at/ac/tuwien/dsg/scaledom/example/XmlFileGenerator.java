package at.ac.tuwien.dsg.scaledom.example;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generate XML test files.
 * 
 * @author Waldemar Hummer
 */
public class XmlFileGenerator {

	public static void generate(int branchFactor, int maxLevel, OutputStream out) throws IOException {
		AtomicLong nodeCount = new AtomicLong();
		generate(branchFactor, maxLevel, 0, new BufferedWriter(new OutputStreamWriter(out)), nodeCount);
		System.out.println("INFO: XML document created with " + nodeCount.get() + " nodes");
	}

	private static void generate(int branchFactor, int maxLevel, 
			int level, BufferedWriter out, AtomicLong nodeCount) throws IOException {
		if(level > maxLevel) {
			return;
		}
		out.write("<l" + level + ">");
		nodeCount.incrementAndGet();
		for(int i = 0; i < branchFactor; i ++) {
			generate(branchFactor, maxLevel, level + 1, out, nodeCount);
		}
		out.write("</l" + level + ">");
		out.flush();
	}

}
