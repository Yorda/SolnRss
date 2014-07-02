package free.solnRss.state;

import android.content.Loader;
import android.database.Cursor;
import free.solnRss.R;

public class PublicationsByLastSearchListState extends
		AbstractPublicationListState {

	private Integer lastFound;

	@Override
	public Loader<Cursor> displayList() {
		return repository.loadPublicationsBySyndication(filterText, lastFound);
	}

	@Override
	public String getActionBarTitle() {
		return context.getString(R.string.last_publications);
	}

	public void setLastFound(Integer lastFound) {
		this.lastFound = lastFound;
	}
}
