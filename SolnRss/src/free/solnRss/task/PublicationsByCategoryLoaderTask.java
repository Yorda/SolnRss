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

public class PublicationsByCategoryLoaderTask extends AsyncTask<Integer, Void, PublicationAdapter> {

	final int emptyMessageID = R.id.emptyPublicationsMessage;

	final private String[] from = { "pub_title", "pub_link" };
	final private int[] to = { android.R.id.text1, android.R.id.text2 };

	private PublicationRepository repository;
	private ListFragment fragment;
	private Context context;

	public PublicationsByCategoryLoaderTask(ListFragment fragment, Context context) {
		this.context = context;
		this.fragment = fragment;
	}

	@Override
	protected PublicationAdapter doInBackground(Integer... ids) {
		repository = new PublicationRepository(context);
		Cursor c = repository.fetchPublicationByCategorie(ids[0], null,
				mustDisplayUnread());
		c.moveToFirst();
		
		// Keep the category id in adapter for filter
		//((PublicationAdapter) fragment.getListAdapter()).setSelectedCategoryId(ids[0]);
		return new PublicationAdapter(context, R.layout.publications, c, from,	to, 0);
	}

	@Override
	protected void onPostExecute(PublicationAdapter result) {
		super.onPostExecute(result);

		fragment.setListAdapter(result);
		if (fragment.getListAdapter().isEmpty()) {
			displayEmptyMessage();
		} else {
			hideEmptyMessage();
		}

		result.notifyDataSetChanged();
		repository.close();
	}

	private void hideEmptyMessage() {
		((TextView) ((SolnRss) context).findViewById(emptyMessageID))
				.setVisibility(View.INVISIBLE);
	}

	private void displayEmptyMessage() {
		((TextView) ((SolnRss) context).findViewById(emptyMessageID))
				.setVisibility(View.VISIBLE);
	}

	public boolean mustDisplayUnread() {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean("pref_display_unread", true);
	}
}
