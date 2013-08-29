package at.ac.tuwien.dsg.scaledom.test.mmap;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import at.ac.tuwien.dsg.scaledom.test.util.MeasuredTask;

/**
 * Compares stream read to memory map read performance for huge files.
 * 
 * @author Dominik Rauch
 */
public class MemoryMapTest {

	public static void main(String[] args) throws Exception {

		final File file = new File("xml/test.xml");
		final int[] positions = new int[] { 1, 16 * 1024, 17 * 1024 * 1024, 34 * 1024 * 1024, 2 };
		final char[] chars = new char[positions.length];

		final byte[] bytes = new byte[1024 * 1024];

		new MeasuredTask<Void>("Stream read") {
			@Override
			protected Void runTask() throws Exception {
				final RandomAccessFile raf = new RandomAccessFile(file, "r");

				for (int j = 0; j < 1000; ++j) {
					for (int i = 0; i < positions.length; ++i) {
						raf.seek(positions[i]);
						raf.read(bytes);
						chars[i] = (char) bytes[1024];
					}
				}

				raf.close();
				return null;
			}
		}.run();

		System.out.println("");

		new MeasuredTask<Void>("Map read") {
			@Override
			protected Void runTask() throws Exception {
				final RandomAccessFile raf = new RandomAccessFile(file, "r");
				final FileChannel channel = raf.getChannel();
				final MappedByteBuffer mmap = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());

				for (int j = 0; j < 1000; ++j) {
					for (int i = 0; i < positions.length; ++i) {
						mmap.position(positions[i]);
						mmap.get(bytes);
						chars[i] = (char) bytes[1024];
					}
				}

				raf.close();
				return null;
			}
		}.run();
	}

}
