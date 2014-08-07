package free.solnRss.business;

import android.util.SparseArray;

public interface PublicationFinderBusiness {

	/**
	 * 
	 */
	public void searchNewPublications();

	/**
	 * 
	 * @param syndications
	 */
	public void searchNewPublications(SparseArray<String> syndications);

	/**
	 * 
	 * @return
	 */
	public int getNewPublicationsRecorded();
}
