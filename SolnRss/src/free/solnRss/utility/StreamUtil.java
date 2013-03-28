package free.solnRss.utility;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * @author jf.tomasi@gmail.com
 * 
 */
public class StreamUtil {

	/**
	 * Get an input stream and return a String object
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static String readInputStreamAsString(InputStream in)
			throws IOException {

		BufferedInputStream bis = new BufferedInputStream(in);
		ByteArrayOutputStream buf = new ByteArrayOutputStream();

		int result = bis.read();
		while (result != -1) {
			byte b = (byte) result;
			buf.write(b);
			result = bis.read();
		}

		return buf.toString();
	}

	
	public static File stringToFile(String text, String path) {

		File file = new File(path);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			fos.write(text.getBytes());
			fos.close();

		} catch (IOException e) {
			return null;
		}
		return file;
	}
	
}
