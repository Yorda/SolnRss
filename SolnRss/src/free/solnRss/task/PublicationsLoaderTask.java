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

public class PublicationsLoaderTask extends AsyncTask<Integer, Void, PublicationAdapter> {

	final int emptyMessageID = R.id.emptyPublicationsMessage;

	final private String[] from = { "pub_title", "pub_link" };
	final private int[] to = { android.R.id.text1, android.R.id.text2 };

	private PublicationRepository repository;
	private ListFragment fragment;
	private Context context;
	private String filter = null;

	public PublicationsLoaderTask(ListFragment fragment, Context context) {
		this.context = context;
		this.fragment = fragment;
	}

	@Override
	protected PublicationAdapter doInBackground(Integer... ids) {
		repository = new PublicationRepository(context);
		Cursor c = repository.fetchFilteredPublication(ids[0], filter,
				mustDisplayUnread());
		c.moveToFirst();

		PublicationAdapter publications = new PublicationAdapter(context,
				R.layout.publications, c, from, to, 0);
		
		return publications;
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
