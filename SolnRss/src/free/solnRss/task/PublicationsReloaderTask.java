package free.solnRss.task;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import free.solnRss.R;
import free.solnRss.activity.SolnRss;
import free.solnRss.adapter.PublicationAdapter;
import free.solnRss.repository.PublicationRepository;

/**
 * Reload publication list after a click on a item
 * 
 * @author jftomasi
 * 
 */
public class PublicationsReloaderTask extends AsyncTask<Object, Void, Cursor> {
	
	final int emptyMessageID = R.id.emptyPublicationsMessage;
	final int emptyPublicationsLayoutID = R.id.emptyPublicationsLayout;
	final int displayAllButtonID = R.id.displayAllButton;
	
	private PublicationRepository repository;
	private Context context;
	private ListFragment fragment;
	private String filter;
	
	public PublicationsReloaderTask(ListFragment fragment, Context context) {
		this.context = context;
		this.fragment = fragment;
	}

	@Override
	protected Cursor doInBackground(Object... params) {

		Integer syndicationId = (Integer) params[0];
		filter = params.length > 2 && params[2] != null ? (String) params[2] : null;
		repository = new PublicationRepository(context);
		
		// Set this publication as read and reload cursor for adapter
		//Integer publicationId = params.length > 1 && params[1] != null ? (Integer) params[1] : null;
		//if (publicationId != null) {
		//	repository.markClickedPublicationRead(publicationId);
		//}

		// Keep the syndication id in adapter for filter
		((PublicationAdapter) fragment.getListAdapter()).setSelectedSyndicationId(syndicationId);
		return repository.fetchFilteredPublication(syndicationId, null, displayUnread());
	}

	@Override
	protected void onPostExecute(Cursor result) {
		((PublicationAdapter) fragment.getListAdapter()).swapCursor(result);
		
		if (fragment.getListAdapter().isEmpty()) {
			displayEmptyPublicationsMessage();
		} else {
			hideEmptyPublicationsMessage();
			if (!TextUtils.isEmpty(filter)) {
				fragment.getListView().setFilterText(filter);
			} else {
				fragment.getListView().clearTextFilter();
			}
		}
		
		((PublicationAdapter) fragment.getListAdapter()).notifyDataSetChanged();
		repository.close();
	}

	public boolean displayUnread() {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean("pref_display_unread", true);
	}

	private void displayEmptyPublicationsMessage() {
		((TextView) ((SolnRss) context).findViewById(emptyMessageID))
				.setVisibility(View.VISIBLE);
		
		((View) ((SolnRss) context).findViewById(emptyPublicationsLayoutID))
				.setVisibility(View.VISIBLE);
	}

	private void hideEmptyPublicationsMessage() {
		((TextView) ((SolnRss) context).findViewById(emptyMessageID))
				.setVisibility(View.INVISIBLE);

		((View) ((SolnRss) context).findViewById(emptyPublicationsLayoutID))
				.setVisibility(View.INVISIBLE);
	}
}
