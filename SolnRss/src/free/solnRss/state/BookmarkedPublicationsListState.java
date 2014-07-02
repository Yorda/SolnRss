package free.solnRss.state;

import android.content.Loader;
import android.database.Cursor;
import free.solnRss.R;

public class BookmarkedPublicationsListState extends
		AbstractPublicationListState {

	@Override
	public Loader<Cursor> displayList() {
		return repository.loadBookmarkedPublications(filterText);
	}

	@Override
	public String getActionBarTitle() {
		return context.getResources().getString(R.string.title_favorite);
	}
}
