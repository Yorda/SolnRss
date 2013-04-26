package free.solnRss.utility;

import java.io.IOException;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class WebSiteUtil {

	final static String[] feedsType = { 
		"link[type$=rss+xml]", 
		"link[type$=atom+xml]" , 
		"a[href*=http://feeds.feedburner.com]"
	};
	
	/**
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static String searchSyndicate(String url) throws IOException {
		Document doc = Jsoup.connect(url).get();		
		String feeds = null;

		int i = 0;
		while (i < feedsType.length) {
			Elements elements = doc.select(feedsType[i]);
			if (feeds == null && elements != null && elements.hasAttr("href")) {
				feeds = getSyndicate(url, elements);
			}
			i++;
		}
		return feeds;
	}
	
	public static String searchSyndicate(String html, String url) throws IOException {
		Document doc = Jsoup.parse(html);
		return searchSyndicate(doc, url);
	}

	public static String searchSyndicate(Document doc, String url) throws IOException {
		
		String feeds = null;

		int i = 0;
		while ( i < feedsType.length) {
			Elements elements = doc.select(feedsType[i]);
			if (feeds == null && elements != null && elements.hasAttr("href")) {
				feeds = getSyndicate(url, elements);
			}
			i++;
		}
		return feeds;
	}
	
	private static String getSyndicate(String url, Elements elements) {
		String href = elements.attr("href");
		if (href.toLowerCase(Locale.getDefault()).indexOf("http") == -1) {
			return concatUrl(url, href);
		} else {
			return href;
		}
	}

	private static String concatUrl(String url, String feeds) {
		String begin = url;
		if (begin.endsWith("/")) {
			begin = url.substring(0, url.length() - 1);
		}
		String end = feeds.substring(1, feeds.length());
		if (end.startsWith("/")) {
			end = feeds.substring(1, feeds.length());
		}
		return begin.concat("/").concat(end);
	}
}
