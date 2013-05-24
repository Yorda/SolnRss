package free.solnRss.fragment.listener;


public interface PublicationsFragmentListener2 {
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
	 * @param text
	 */
	public void filterPublications(String text);

}
