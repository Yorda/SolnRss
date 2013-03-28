package free.solnRss.utility;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class HttpUtil {
	

	public static String htmlFromSite(String url) throws Exception{
		
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
			if (code != 200) {
				//TODO throw error or redirect...
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
}
