package free.solnRss.state;

import android.content.Loader;
import android.database.Cursor;
import free.solnRss.activity.SolnRss;

public class AllPublicationsListState extends
		AbstractPublicationListState {

	@Override
	public Loader<Cursor> displayList() {
		return repository.loadAllPublications(filterText);
	}

	@Override
	public String getActionBarTitle() {		
		return ((SolnRss) context).getTitle().toString();
	}
}
