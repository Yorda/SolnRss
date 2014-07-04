package free.solnRss.state;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Loader;
import android.database.Cursor;

public interface PublicationsListState {
	
	void init(Context context);
	
	public Loader<Cursor> displayList();

	public String getActionBarTitle();
	
	public void setFilterText(String filterText);
	
	public String getFilterText();

	void setPositionOnList(Integer position, Integer index);

	void resetPositionOnList();

	void restore(JSONObject jsonObject) throws JSONException;

	String save() throws JSONException;	
}
