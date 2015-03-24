package free.solnRss.utility;


import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.List;

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntry;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndFeed;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.FeedException;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.SyndFeedInput;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.XmlReader;


public class SyndicateUtil {

	private String			url;
	private SyndFeedInput	syndFeedInput;
	private SyndFeed		syndFeed;

	public SyndicateUtil() {

	}

	public SyndicateUtil(String url) {
		this.url = url;
	}

	/**
	 * Init with the url
	 * 
	 * @throws Exception
	 */
	public void init() throws Exception {
		URL u = new URL(this.url);
		syndFeedInput = new SyndFeedInput();
		syndFeed = syndFeedInput.build(new XmlReader(u));
	}

	/**
	 * The data received from web site is still a xml feed
	 * 
	 * @param html
	 * @throws Exception
	 */
	public void init(String xml) throws Exception {
		StringReader sr = new StringReader(xml);
		syndFeedInput = new SyndFeedInput();
		syndFeed = syndFeedInput.build(sr);
	}

	/**
	 * 
	 * @param u
	 * @throws Exception
	 */
	public void init(URL u) throws Exception {
		syndFeedInput = new SyndFeedInput();
		syndFeed = syndFeedInput.build(new XmlReader(u));
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 * @throws FeedException
	 */
	public String syndicationName() throws IOException, FeedException {
		return syndFeed.getTitle();
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 * @throws FeedException
	 */
	@SuppressWarnings("unchecked")
	public List<SyndEntry> lastEntries() throws IOException, FeedException {
		return syndFeed.getEntries();
	}

	/**
	 * 
	 * @param text
	 * @return
	 */
	public boolean isFeed(String text) {
		SyndFeedInput sfi = new SyndFeedInput();
		try {
			StringReader sr = new StringReader(text);
			sfi.build(sr);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 * @throws FeedException
	 */
	@SuppressWarnings("unchecked")
	public static List<SyndEntry> lastEntries(URL url) throws IOException, FeedException {
		SyndFeedInput sfi = new SyndFeedInput();
		return sfi.build(new XmlReader(url)).getEntries();
	}

	/**
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws FeedException
	 */
	@SuppressWarnings("unchecked")
	public static List<SyndEntry> lastEntries(File file) throws IOException, FeedException {
		SyndFeedInput sfi = new SyndFeedInput();
		return sfi.build(new XmlReader(file)).getEntries();
	}

	/**
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 * @throws FeedException
	 */
	public static String syndicationName(URL url) throws IOException, FeedException {
		SyndFeedInput sfi = new SyndFeedInput();
		return sfi.build(new XmlReader(url)).getTitle();
	}

}
