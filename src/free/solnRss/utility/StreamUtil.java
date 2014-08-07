package free.solnRss.utility;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

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

	public static String encode(String path) {

		Bitmap bm = BitmapFactory.decodeFile(path);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);

		byte[] bytes = baos.toByteArray();

		return Base64.encodeToString(bytes, Base64.DEFAULT);
	}

	public static String encode(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);

		byte[] bytes = baos.toByteArray();

		return Base64.encodeToString(bytes, Base64.DEFAULT);
	}

}
