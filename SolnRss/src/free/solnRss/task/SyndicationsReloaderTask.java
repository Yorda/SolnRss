package free.solnRss.task;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.app.ListFragment;
import android.view.View;
import free.solnRss.R;
import free.solnRss.activity.SolnRss;
import free.solnRss.adapter.SyndicationAdapter;
import free.solnRss.repository.SyndicationRepository;

public class SyndicationsReloaderTask extends AsyncTask<Integer, Void, Cursor> {

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
		((SyndicationAdapter) fragment.getListAdapter()).swapCursor(result);
		if (fragment.getListAdapter().isEmpty()) {
			displayEmptySyndicationsMessage();
		} else {
			hideEmptySyndicationsMessage();
		}
		repository.close();
	}

	private void displayEmptySyndicationsMessage() {
		((View) ((SolnRss) context).findViewById( R.id.emptySyndicationsLayout))
				.setVisibility(View.VISIBLE);
	}

	private void hideEmptySyndicationsMessage() {
		((View) ((SolnRss) context).findViewById( R.id.emptySyndicationsLayout))
				.setVisibility(View.INVISIBLE);
	}
}
