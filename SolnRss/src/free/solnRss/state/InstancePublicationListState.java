package free.solnRss.state;

import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class InstancePublicationListState {

	public PublicationsListState restorePublicationListState(Context context,
			SharedPreferences sharedPreferences) {
		
		String jsonString = sharedPreferences.getString("publication list state", null);
		PublicationsListState publicationListState = null;

		if (jsonString == null) {
			publicationListState = emptyPublicationListState(context);
		} else {
			publicationListState = deserializePublicationListState(context, jsonString);
		}
		return publicationListState;
	}
	
	public void savePublicationListState(SharedPreferences sharedPreferences,
			PublicationsListState publicationsListState) {
		
		String jsonString = null;
		try {
			jsonString = publicationsListState.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Editor editor = sharedPreferences.edit();
		editor.putString("publication list state", jsonString);
		editor.commit();
	}
	
	private PublicationsListState deserializePublicationListState(Context context, String jsonString) {
		PublicationsListState publicationListState = null;
		try {
			JSONObject jsonObject = new JSONObject(jsonString);
			publicationListState = (PublicationsListState) Class.forName(
					jsonObject.getString("name")).newInstance();
			publicationListState.init(context);
			publicationListState.restore(jsonObject);
			
		} catch (Exception e) {
			publicationListState = emptyPublicationListState(context);
			e.printStackTrace();
		}
		return publicationListState;
	}
	
	private PublicationsListState emptyPublicationListState(Context context) {
		PublicationsListState publicationListState = new AllPublicationsListState();
		publicationListState.init(context);
		return publicationListState;
	}
}
