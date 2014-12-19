package free.solnRss.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpUtil {

	public static String retrieveHtml(String surl) throws IOException {

		String response = new String();
		try {
			URL url = new URL(surl);
			HttpURLConnection httpURLConnection = (HttpURLConnection) url
					.openConnection();
			int code = httpURLConnection.getResponseCode();
			if (code < 200 || code > 300) {
				throw new IOException("Response code from site not OK " + code);
			}
			response = readStream(httpURLConnection.getInputStream());

		} catch (Exception e) {
			e.printStackTrace(System.err);
			throw new IOException("Response code from site not OK ");
		}

		return response;
	}

	private static String readStream(InputStream in) {
		BufferedReader reader = null;
		StringBuilder sb = new StringBuilder();
		try {
			reader = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace(System.err);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace(System.err);
				}
			}
		}
		return sb.toString();
	}

	public static Boolean isValidUrl(String url) {
		try {
			new URL(url);
		} catch (MalformedURLException e) {
			return false;
		}
		return true;
	}
}
