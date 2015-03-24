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
	 * @param numberOfClick
	 * 
	 */
	public void addOneReadToSyndication(Integer syndicationId, Integer numberOfClick);

	/**
	 * Move list view to top
	 */
	public void moveListViewToTop();
}
