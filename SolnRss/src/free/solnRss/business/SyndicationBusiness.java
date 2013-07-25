package free.solnRss.business;

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

}
