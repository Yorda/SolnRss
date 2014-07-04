package free.solnRss.state;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import free.solnRss.repository.PublicationRepository;
import free.solnRss.singleton.PublicationRepositorySingleton;

public abstract class AbstractPublicationListState implements
		PublicationsListState {

	protected Context context;
	protected String filterText;
	protected PublicationRepository repository;
	protected Integer position, index;

	@Override
	public void init(Context context) {
		this.position = 0;
		this.index = 0;
		this.filterText = "";
		this.context = context;
		this.repository = PublicationRepositorySingleton.getInstance(context).getPublicationRepository();
	}

	@Override
	public void setFilterText(String filterText) {
		this.filterText = filterText;
	}

	@Override
	public String getFilterText() {
		return filterText;
	}

	@Override
	public void setPositionOnList(Integer position, Integer index) {
		this.position = position;
		this.index = index;
	}

	@Override
	public void resetPositionOnList() {
		this.position = 0;
		this.index = 0;
	}

	public JSONObject saveAbstractPublicationListState() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("name", this.getClass().getName());
		jsonObject.put("filterText", getFilterText());
		jsonObject.put("position", position);
		jsonObject.put("index", index);
		return jsonObject;
	}

	public void restoreAbstractPublicationListState(JSONObject jsonObject)
			throws JSONException {
		setFilterText(jsonObject.getString("filterText"));
		this.position = jsonObject.getInt("position");
		this.index = jsonObject.getInt("index");
	}
}
