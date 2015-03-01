package free.solnRss.state;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Loader;
import android.database.Cursor;

public class PublicationsBySyndicationListState extends
		AbstractPublicationListState {

	private Integer syndicationId;

	@Override
	public Loader<Cursor> displayList() {
		return repository.loadPublicationsBySyndication(filterText,
				syndicationId);
	}

	@Override
	public String getActionBarTitle() {
		return repository.syndicationName(syndicationId);
	}

	public void setSyndicationId(Integer syndicationId) {
		this.syndicationId = syndicationId;
	}

	@Override
	public void restore(JSONObject jsonObject) throws JSONException {
		restoreAbstractPublicationListState(jsonObject);
		this.syndicationId = jsonObject.getInt("syndicationId");
	}

	@Override
	public String save() throws JSONException {
		JSONObject jsonObject = saveAbstractPublicationListState();
		jsonObject.put("syndicationId", syndicationId);
		return jsonObject.toString();
	}
}
