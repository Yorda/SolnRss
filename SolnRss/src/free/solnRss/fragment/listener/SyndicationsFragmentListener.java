package free.solnRss.fragment.listener;

import android.content.Context;

/**
 * 
 */
public interface SyndicationsFragmentListener {
	/**
	 * 
	 * @param context
	 */
	public void loadSyndications(Context context);

	/**
	 * 
	 * @param context
	 */
	public void reloadSyndications(Context context);

	/**
	 * 
	 * @param text
	 */
	public void filterSyndications(String text);
}
