package free.solnRss.task;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import free.solnRss.adapter.SyndicationsCategorieAdapter;
import free.solnRss.repository.SyndicationRepository;

public class SyndicationCategorieReloaderTask extends
		AsyncTask<Integer, Void, Cursor> {

	private SyndicationRepository repository;
	private Context context;
	private SyndicationsCategorieAdapter adapter;

	public SyndicationCategorieReloaderTask(Context context, SyndicationsCategorieAdapter adapter) {
		this.context = context;
		this.adapter = adapter;
	}

	@Override
	protected Cursor doInBackground(Integer... arg0) {
		repository = new SyndicationRepository(context);
		return repository.syndicationCategorie(arg0[0]);

	}

	@Override
	protected void onPostExecute(Cursor result) {
		adapter.changeCursor(result);
		adapter.notifyDataSetChanged();
		super.onPostExecute(result);
	}
}
