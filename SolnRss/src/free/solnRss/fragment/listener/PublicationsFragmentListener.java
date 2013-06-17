package free.solnRss.fragment.listener;


public interface PublicationsFragmentListener {
	/**
	 * 
	 * @param context
	 */
	public void loadPublications();

	/**
	 * 
	 * @param context
	 */
	public void reloadPublications();
	
	
	/**
	 * Call after change on global settings
	 * 
	 * @param context
	 */
	public void refreshPublications();
	
	/**
	 * 
	 * @param context
	 * @param categorieID
	 */
	public void reLoadPublicationsByCategory(Integer categorieID);
	
	/**
	 * 
	 * @param context
	 * @param syndicationID
	 */
	public void reLoadPublicationsBySyndication(Integer syndicationID);
	
	/**
	 * 
	 */
	public void moveListViewToTop();

	/**
	 * 
	 */
	public void markAllPublicationsAsRead();

	/**
	 * When list view is empty user can reload publication's list with publications already read.
	 */
	public void reloadPublicationsWithAlreadyRead();

}
