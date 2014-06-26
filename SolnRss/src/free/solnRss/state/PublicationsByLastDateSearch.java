package free.solnRss.state;

import android.content.Loader;
import android.database.Cursor;

public class PublicationsByLastDateSearch implements PublicationsListState {

	@Override
	public Loader<Cursor> displayList() {
		return null;
	}

	@Override
	public String getActionBarTitle() {
		return null;
	}

	@Override
	public void setFilterText(String filterText) {
		
	}

	@Override
	public String getFilterText() {
		return null;
	}

}
