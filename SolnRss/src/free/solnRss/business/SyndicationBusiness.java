package free.solnRss.business;

import java.util.List;

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntry;

import free.solnRss.exception.ExtractFeedException;
import free.solnRss.model.Syndication;

public interface SyndicationBusiness {

	/**
	 * Try to get the url for RSS feed in a web page by the url given in
	 * parameter.
	 * 
	 * @param url
	 * @return
	 * @throws ExtractFeedException
	 */
	public Syndication searchSyndication(String url)
			throws ExtractFeedException;

	/**
	 * Get a list of last publications published for a syndication
	 * 
	 * @param url
	 * @return
	 */
	public Syndication getLastPublications(Syndication syndication)
			throws ExtractFeedException;

	/**
	 * 
	 * @param html
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public Syndication retrieveSyndicationContent(String html, String url)
			throws Exception;

	/**
	 * 
	 * @param url
	 * @return
	 * @throws ExtractFeedException
	 */
	List<SyndEntry> newRssPublished(String url) throws ExtractFeedException;

}
