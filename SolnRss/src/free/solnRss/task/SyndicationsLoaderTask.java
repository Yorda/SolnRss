package free.solnRss.task;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.TextView;
import free.solnRss.R;
import free.solnRss.activity.SolnRss;
import free.solnRss.adapter.SyndicationAdapter;
import free.solnRss.repository.SyndicationRepository;

/**
 * Task for load the syndication list.
 * 
 * @author jf.tomasi
 * 
 */
public class SyndicationsLoaderTask extends
		AsyncTask<Void, Void, SyndicationAdapter> {
	final int emptyMessageID = R.id.emptySyndicationsMessage;
	SyndicationRepository repository;
	ListFragment fragment;
	Context context;

	private final String[] from = { "syn_name", "syn_number_click" };
	private final int[] to = { android.R.id.text1, android.R.id.text2 };

	public SyndicationsLoaderTask(ListFragment fragment, Context context) {
		this.context = context;
		this.fragment = fragment;
	}

	@Override
	protected SyndicationAdapter doInBackground(Void... params) {
		repository = new SyndicationRepository(context);
		Cursor c = repository.fetchAllSite();
		c.moveToFirst();
		SyndicationAdapter adapter = new SyndicationAdapter(context,
				R.layout.syndications, c, from, to, 0);
		return adapter;
	}

	@Override
	protected void onPostExecute(SyndicationAdapter result) {
		super.onPostExecute(result);

		fragment.setListAdapter(result);
		if (fragment.getListAdapter().isEmpty()) {
			displayEmptySyndicationsMessage();
		} else {
			hideEmptySyndicationsMessage();
		}
		repository.close();
	}

	private void displayEmptySyndicationsMessage() {
		((TextView) ((SolnRss) context).findViewById(emptyMessageID))
				.setVisibility(View.VISIBLE);
	}

	private void hideEmptySyndicationsMessage() {
		((TextView) ((SolnRss) context).findViewById(emptyMessageID))
				.setVisibility(View.INVISIBLE);
	}
}
