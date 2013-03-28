package free.solnRss.business;

import java.util.List;

import free.solnRss.exception.ExtractFeedException;
import free.solnRss.model.Publication;
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
	public Syndication searchSyndication(String url) throws ExtractFeedException;
	
	/**
	 * Get a list of last publications published for a syndication
	 * 
	 * @param url
	 * @return
	 */
	public List<Publication> getLastPublications(String url) throws ExtractFeedException ;

}
