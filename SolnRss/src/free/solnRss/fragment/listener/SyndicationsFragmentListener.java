package free.solnRss.fragment.listener;


/**
 * 
 */
public interface SyndicationsFragmentListener {
	/**
	 * 
	 * @param context
	 */
	public void loadSyndications();

	/**
	 * 
	 * @param context
	 */
	public void reloadSyndications();

	/**
	 * 
	 * @param text
	 */
	public void filterSyndications(String text);
}
