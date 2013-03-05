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

public class SyndicationsReloaderTask extends AsyncTask<Integer, Void, Cursor> {
	final int emptyMessageID = R.id.emptySyndicationsMessage;
	private SyndicationRepository repository;
	private Context context;
	private ListFragment fragment;

	public SyndicationsReloaderTask(Context context, ListFragment fragment) {
		this.context = context;
		this.fragment = fragment;
	}

	@Override
	protected Cursor doInBackground(Integer... ids) {
		repository = new SyndicationRepository(context);
		return repository.fetchAllSite();
	}

	@Override
	protected void onPostExecute(Cursor result) {
		super.onPostExecute(result);
		((SyndicationAdapter) fragment.getListAdapter()).changeCursor(result);
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
