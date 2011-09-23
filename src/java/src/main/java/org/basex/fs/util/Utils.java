package org.basex.fs.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public final class Utils {

	/**
	 * Copy file. If file exists it will be overwritten.
	 * 
	 * @param source
	 *            data to copy to target
	 * @param target
	 *            new file to be written
	 */
	public static void copyFile(final File source, final File target) {
		try {
			FileInputStream in = new FileInputStream(source);
			FileOutputStream out = new FileOutputStream(target);
			byte[] buf = new byte[4096];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
