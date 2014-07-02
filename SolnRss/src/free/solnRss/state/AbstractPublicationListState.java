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

	public void init(Context context) {
		this.context = context;
		this.repository = PublicationRepositorySingleton.getInstance(context)
				.getPublicationRepository();
	}

	@Override
	public void setFilterText(String filterText) {
		this.filterText = filterText;
	}

	@Override
	public String getFilterText() {
		return filterText;
	}

	protected void setPositionOnList(Integer position, Integer index) {
		this.position = position;
		this.index = index;
	}

	protected void resetPositionOnList() {
		this.position = 0;
		this.index = 0;
	}
	
	public void saveListState() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("name", this.getClass().getName());
		jsonObject.put("filterText", this.filterText);
		jsonObject.put("position", position == null ? 0 : position);
		jsonObject.put("index", index == null ? 0 : index);

	}

	public void restoreListState(String s) throws JSONException,
			ClassNotFoundException, IllegalAccessException, InstantiationException {
		JSONObject jsonObject = new JSONObject(s);
		PublicationsListState state = (PublicationsListState) Class.forName(jsonObject.getString("name")).newInstance();
		state.setFilterText(jsonObject.getString("filterText"));
		position = jsonObject.getInt("position");
		index = jsonObject.getInt("index");
	}
	
	
}
