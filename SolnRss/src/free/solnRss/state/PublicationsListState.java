package free.solnRss.state;

import android.content.Loader;
import android.database.Cursor;

public interface PublicationsListState {
	
	public Loader<Cursor> displayList();

	public String getActionBarTitle();
	
	public void setFilterText(String filterText);
	
	public String getFilterText();
	
}
