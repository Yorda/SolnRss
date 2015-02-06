package free.solnRss.state;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Loader;
import android.database.Cursor;

public class PublicationsByCategoryListState extends
		AbstractPublicationListState {

	private Integer categoryId;

	@Override
	public Loader<Cursor> displayList() {
		return repository.loadPublicationsByCategory(filterText, categoryId);
	}

	@Override
	public String getActionBarTitle() {
		return repository.categoryName(categoryId);
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	@Override
	public void restore(JSONObject jsonObject) throws JSONException {
		restoreAbstractPublicationListState(jsonObject);
		this.categoryId = jsonObject.getInt("categoryId");
	}
	
	@Override
	public String save() throws JSONException {
		JSONObject jsonObject = saveAbstractPublicationListState();
		jsonObject.put("categoryId", categoryId);
		return jsonObject.toString();
	}
}
