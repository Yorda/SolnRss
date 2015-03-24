package free.solnRss.utility;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;


public class ImageToDataUtil {

	private final Pattern	pImg	= Pattern.compile("<img(.*?)>");
	private final Pattern	pSrc	= Pattern.compile("src=(['\"])(.*?)\\1");

	//private final String onePixelBase64 = "R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7";

	public String replaceImageByBase64Data(String html) {

		Matcher m = pImg.matcher(html);

		String htmlWithBase64Image = new String(html);
		String httpImage = null;
		String base64Image = null;

		while (m.find()) {
			httpImage = html.substring(m.start(), m.end());
			base64Image = replaceByBase64Data(httpImage);
			htmlWithBase64Image = htmlWithBase64Image.replaceAll(httpImage, base64Image);
		}
		return htmlWithBase64Image;
	}

	private String replaceByBase64Data(String imageTag) {

		Matcher m = pSrc.matcher(imageTag);

		Bitmap bitmap = null;
		String url = null;
		String raw64ImageTag = new String(imageTag);

		String raw64 = null;
		while (m.find()) {
			url = imageTag.substring(m.start() + 5, m.end() - 1);
			try {

				bitmap = downloadBitmap(url);
				raw64 = encode(bitmap);
				raw64ImageTag = raw64ImageTag.replaceAll(url, "data:image/gif;base64," + raw64);

			} catch (Exception e) {
				/*
				 * raw64ImageTag = raw64ImageTag.replaceAll(url, "data:image/gif;base64," + onePixelBase64);
				 */
			}
		}
		return raw64ImageTag;
	}

	private String encode(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		byte[] bytes = baos.toByteArray();
		return Base64.encodeToString(bytes, Base64.DEFAULT);
	}

	private Bitmap downloadBitmap(String url) throws IOException {

		HttpUriRequest request = new HttpGet(url.toString());
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(request);

		StatusLine statusLine = response.getStatusLine();
		int statusCode = statusLine.getStatusCode();

		if (statusCode == 200) {
			HttpEntity entity = response.getEntity();
			byte[] bytes = EntityUtils.toByteArray(entity);

			Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

			/*
			 * File f = new File(""); FileOutputStream fos = new FileOutputStream(f); bitmap.compress(CompressFormat.PNG, 0, fos);
			 */

			return bitmap;
		} else {
			throw new IOException("Download failed, HTTP response code " + statusCode + " - " + statusLine.getReasonPhrase());
		}
	}
}
