package free.solnRss.state;

import org.json.JSONException;
import org.json.JSONObject;

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
