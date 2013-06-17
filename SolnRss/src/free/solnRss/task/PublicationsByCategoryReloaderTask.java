package free.solnRss.task;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.LinearLayout;
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
@Deprecated
public class PublicationsByCategoryReloaderTask extends AsyncTask<Integer, Void, Cursor> {
	final int emptyMessageID = R.id.emptyPublicationsMessage;
	private PublicationRepository repository;
	private Context context;
	private ListFragment fragment;

	public PublicationsByCategoryReloaderTask(ListFragment fragment,
			Context context) {
		this.context = context;
		this.fragment = fragment;
	}

	@Override
	protected Cursor doInBackground(Integer... ids) {
		repository = new PublicationRepository(context);
		
		if (ids.length > 1) {
			repository.markClickedPublicationRead(ids[1]);
		}
		
		// Keep the category id in adapter for filter
		//((PublicationAdapter) fragment.getListAdapter()).setSelectedCategoryId(ids[0]);
		
		return repository.fetchPublicationByCategorie(ids[0], null,
				mustDisplayUnread());
		
	}

	@Override
	protected void onPostExecute(Cursor result) {
		
		((PublicationAdapter) fragment.getListAdapter()).swapCursor(result);
		if (fragment.getListAdapter().isEmpty()) {
			displayEmptyMessage();
		} else {
			hideEmptyMessage();
		}
		
		repository.close();
	}

	public boolean mustDisplayUnread() {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean("pref_display_unread", true);
	}

	private void displayEmptyMessage() {
		LinearLayout l = (LinearLayout) ((SolnRss) context).findViewById(R.id.emptyPublicationsLayout);
		l.setVisibility(View.VISIBLE);
	}

	private void hideEmptyMessage() {
		LinearLayout l = (LinearLayout) ((SolnRss) context).findViewById(R.id.emptyPublicationsLayout);
		l.setVisibility(View.INVISIBLE);
	}
	
	/*private void displayEmptyMessage() {
		((TextView) ((SolnRss) context).findViewById(emptyMessageID))
				.setVisibility(View.VISIBLE);
	}

	private void hideEmptyMessage() {
		((TextView) ((SolnRss) context).findViewById(emptyMessageID))
				.setVisibility(View.INVISIBLE);
	}*/
}
