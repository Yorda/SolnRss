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

public class PublicationsLoaderTask extends
		AsyncTask<Object, Void, PublicationAdapter> {

	final int emptyMessageID = R.id.emptyPublicationsMessage;

	final private String[] from = { "pub_title", "pub_link" };
	final private int[] to = { android.R.id.text1, android.R.id.text2 };

	private PublicationRepository repository;
	private ListFragment fragment;
	private Context context;
	private String filter;


	public PublicationsLoaderTask(ListFragment fragment, Context context) {
		this.context = context;
		this.fragment = fragment;
	}

	@Override
	protected PublicationAdapter doInBackground(Object... params) {
		
		Integer id = (Integer) params[0];
		filter = params.length > 1 && params[1] != null ? (String) params[1] : null;

		repository = new PublicationRepository(context);
		Cursor c = repository.fetchFilteredPublication(id, null, displayUnread());

		c.moveToFirst();
		PublicationAdapter publicationAdapter = 
			new PublicationAdapter(context,	R.layout.publications, c, from, to, 0);

		// Keep the syndication id in adapter for filter
		publicationAdapter.setSelectedSyndicationId((Integer)id);

		return publicationAdapter;
	}

	@Override
	protected void onPostExecute(PublicationAdapter result) {
		super.onPostExecute(result);

		fragment.setListAdapter(result);
		if (fragment.getListAdapter().isEmpty()) {
			displayEmptyMessage();
			
		} else {
			hideEmptyMessage();
			if (!TextUtils.isEmpty(filter)) {
				fragment.getListView().setFilterText(filter);
			} else {
				fragment.getListView().clearTextFilter();
			}
		}

		result.notifyDataSetChanged();
		repository.close();
	}

	private void hideEmptyMessage() {
		TextView tv = ((TextView) ((SolnRss) context)
				.findViewById(emptyMessageID));
		if (tv != null) {
			tv.setVisibility(View.INVISIBLE);
		}

	}

	private void displayEmptyMessage() {
		TextView tv = ((TextView) ((SolnRss) context)
				.findViewById(emptyMessageID));
		if (tv != null) {
			tv.setVisibility(View.VISIBLE);
		}
	}

	public boolean displayUnread() {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean("pref_display_unread", true);
	}
}
