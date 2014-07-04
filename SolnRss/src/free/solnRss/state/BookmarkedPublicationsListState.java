package free.solnRss.state;

import org.json.JSONException;
import org.json.JSONObject;

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

	@Override
	public void restore(JSONObject jsonObject) throws JSONException {
		restoreAbstractPublicationListState(jsonObject);
	}
	
	@Override
	public String save() throws JSONException {
		JSONObject jsonObject = saveAbstractPublicationListState();
		return jsonObject.toString();
	}
}
