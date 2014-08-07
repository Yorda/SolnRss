package free.solnRss.utility;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class HttpUtil {
	

	public enum HTTP_FAILURE {
		
	}

	public static String retrieveHtml(String url)
			throws ClientProtocolException, IOException {

		final HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 6000);
		HttpConnectionParams.setSoTimeout(httpParams, 6000);
		HttpClient httpclient = new DefaultHttpClient(httpParams);

		HttpGet httpget = null;
		HttpResponse response = null;
		String html = null;
		try {

			httpget = new HttpGet(url.trim());
			response = httpclient.execute(httpget);

			int code = response.getStatusLine().getStatusCode();

			if (code < 200 || code > 300) {
				throw new IOException("Response code from site not OK");
			}

			HttpEntity entity = response.getEntity();

			if (entity != null) {
				html = StreamUtil.readInputStreamAsString(entity.getContent());
			}
		} finally {
			if (httpget != null) {
				httpget.abort();
			}
			httpclient.getConnectionManager().shutdown();
		}
		return html;
	}

	public static Boolean isValidUrl(String url) {
		try {
			new URL(url);
		} catch (MalformedURLException e) {
			return false;
		}
		return true;
	}
	
	public static Bitmap downloadBitmap(String url) throws IOException {
		
		HttpUriRequest request = new HttpGet(url.toString());
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(request);

		StatusLine statusLine = response.getStatusLine();
		int statusCode = statusLine.getStatusCode();
		if (statusCode == 200) {
			HttpEntity entity = response.getEntity();
			byte[] bytes = EntityUtils.toByteArray(entity);

			Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0,	bytes.length);
			return bitmap;
		} else {
			throw new IOException("Download failed, HTTP response code "
					+ statusCode + " - " + statusLine.getReasonPhrase());
		}
	}

	public static String ImageSrcToBinary(String webSiteUrl, String imageTag) {

		String imageTagWithBinary = new String();
		try {
			// Get the image url
			String url = "";
			
			// Download the remote file
			Bitmap bitmap = downloadBitmap(url);

			// Transform file to binary string
			String raw = StreamUtil.encode(bitmap);

			// Replace source in image tag
			final String regex = "src=(['\"])" // the ' or the " is in group 1
					+ "(.*?)" // match any character in a non-greedy fashion
					+ "\\1"; // closes with the quote that is in group 1

			imageTagWithBinary = imageTag.replaceAll(regex,
					"src=\"data:image/gif;base64," + raw + "\"");

		} catch (Exception e) {
			return imageTag;
		}
		return imageTagWithBinary;
	}
	
	protected static String getImageUrl(String webSiteUrl, String imageTag){
		return null;
	}
	
	
	/*final HttpParams httpParams = new BasicHttpParams();
	HttpConnectionParams.setConnectionTimeout(httpParams, 6000);
	HttpConnectionParams.setSoTimeout(httpParams, 6000);
	HttpClient httpclient = new DefaultHttpClient(httpParams);

	HttpGet httpget = null;
	String response = null;

	try {

		httpget = new HttpGet(url.trim());
		
		// Create a custom response handler
		ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

			public String handleResponse(final HttpResponse response)
					throws ClientProtocolException, IOException {

				int status = response.getStatusLine().getStatusCode();

				if (status >= 200 && status < 300) {
					HttpEntity entity = response.getEntity();
					return entity != null ? StreamUtil.readInputStreamAsString(entity.getContent()) : null;
				} else {
					throw new ClientProtocolException("Unexpected response from site: " + status);
				}
			}
		};

		response = httpclient.execute(httpget, responseHandler);

	} finally {
		
		if (httpget != null) {
			httpget.abort();
		}
		httpclient.getConnectionManager().shutdown();
	}
	return response;*/
}
