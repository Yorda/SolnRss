package free.solnRss.task;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
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
public class PublicationsReloaderTask extends AsyncTask<Integer, Void, Cursor> {
	final int emptyMessageID = R.id.emptyPublicationsMessage;
	final int emptyPublicationsLayoutID = R.id.emptyPublicationsLayout;
	final int displayAllButtonID = R.id.displayAllButton;
	
	private PublicationRepository repository;
	private Context context;
	private ListFragment fragment;

	public PublicationsReloaderTask(ListFragment fragment, Context context) {
		this.context = context;
		this.fragment = fragment;
	}

	@Override
	protected Cursor doInBackground(Integer... ids) {
		// Set this publication as read and reload cursor for adapter
		repository = new PublicationRepository(context);
		if (ids.length > 1) {
			repository.markClickedPublicationRead(ids[1]);
		}

		// Keep the syndication id in adapter for filter
		((PublicationAdapter) fragment.getListAdapter()).setSelectedSyndicationId(ids[0]);

		return repository.fetchFilteredPublication(ids[0], null, mustDisplayUnread());
	}

	@Override
	protected void onPostExecute(Cursor result) {
		super.onPostExecute(result);
		((PublicationAdapter) fragment.getListAdapter()).changeCursor(result);

		if (fragment.getListAdapter().isEmpty()) {
			displayEmptyPublicationsMessage();
		} else {
			hideEmptyPublicationsMessage();
		}
		repository.close();
	}

	public boolean mustDisplayUnread() {
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
