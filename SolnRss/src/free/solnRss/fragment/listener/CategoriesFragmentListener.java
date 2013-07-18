package free.solnRss.fragment.listener;

import android.content.Context;

public interface CategoriesFragmentListener {
	/**
	 * 
	 * @param context
	 */
	public void loadCategories();

	/**
	 * 
	 * @param context
	 */
	public void reloadCategories();
	
	/**
	 * 
	 * @param contex
	 * @param newCatgorie
	 */
	public void addCategorie(Context contex, String newCatgorie);

	/**
	 * 
	 */
	public void reLoadCategoriesAfterSyndicationDeleted();
}
