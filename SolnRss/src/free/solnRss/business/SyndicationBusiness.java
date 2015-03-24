package free.solnRss.business;


import java.util.List;

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntry;

import free.solnRss.exception.ExtractFeedException;
import free.solnRss.model.Syndication;


public interface SyndicationBusiness {

	/**
	 * 
	 * @param html
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public Syndication retrieveSyndicationContent(String html, String url) throws Exception;

	/**
	 * 
	 * @param url
	 * @return
	 * @throws ExtractFeedException
	 */
	List<SyndEntry> newRssPublished(String url) throws ExtractFeedException;

}
