package free.solnRss.task;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import free.solnRss.R;
import free.solnRss.activity.SyndicationsCategoriesActivity;
import free.solnRss.adapter.SyndicationsCategorieAdapter;
import free.solnRss.repository.SyndicationRepository;

public class SyndicationCategoryLoaderTask extends
		AsyncTask<Integer, Void, SyndicationsCategorieAdapter> {

	private SyndicationRepository repository;
	private Context context;

	private final String[] from = { "syn_name" };
	private final int[] to = { android.R.id.text1, };

	public SyndicationCategoryLoaderTask(Context context) {
		this.context = context;
	}

	@Override
	protected SyndicationsCategorieAdapter doInBackground(Integer... arg0) {
		repository = new SyndicationRepository(context);
		Cursor c = repository.syndicationCategorie(arg0[0]);
		c.moveToFirst();
		SyndicationsCategorieAdapter adapter = new SyndicationsCategorieAdapter(
				context, R.layout.syndications_categorie, c, from, to, 0);
		return adapter;

	}

	@Override
	protected void onPostExecute(SyndicationsCategorieAdapter result) {
		((SyndicationsCategoriesActivity) context).setListAdapter(result);
		super.onPostExecute(result);
	}
}
