package free.solnRss.state;

import org.json.JSONException;
import org.json.JSONObject;

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

	@Override
	public void restore(JSONObject jsonObject) throws JSONException {
		restoreAbstractPublicationListState(jsonObject);
		this.lastFound = jsonObject.getInt("lastFound");
	}
	
	@Override
	public String save() throws JSONException {
		JSONObject jsonObject = saveAbstractPublicationListState();
		jsonObject.put("lastFound", lastFound);
		return jsonObject.toString();
	}
}
