package free.solnRss.fragment.listener;

import android.content.Context;

/**
 * 
 * @author jftomasi
 *
 */
public interface PublicationsFragmentListener {
	/**
	 * 
	 * @param context
	 */
	public void loadPublications(Context context);

	/**
	 * 
	 * @param context
	 */
	public void reloadPublications(Context context);
	
	
	/**
	 * Call after change on global settings
	 * 
	 * @param context
	 */
	public void refreshPublications(Context context);
	
	/**
	 * 
	 * @param context
	 * @param categorieID
	 */
	public void reLoadPublicationsByCategorie(Context context, Integer categorieID);
	
	/**
	 * 
	 * @param context
	 * @param syndicationID
	 */
	public void reLoadPublicationsBySyndication(Context context, Integer syndicationID);
	
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