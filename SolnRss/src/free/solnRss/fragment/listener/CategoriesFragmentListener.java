package free.solnRss.fragment.listener;

import android.content.Context;

public interface CategoriesFragmentListener {
	/**
	 * 
	 * @param context
	 */
	public void loadCategories(Context context);

	/**
	 * 
	 * @param context
	 */
	public void reloadCategories(Context context);
	
	/**
	 * 
	 * @param contex
	 * @param newCatgorie
	 */
	public void addCategorie(Context contex, String newCatgorie);

	/**
	 * 
	 * @param text
	 */
	public void filterCategories(String text);
}
