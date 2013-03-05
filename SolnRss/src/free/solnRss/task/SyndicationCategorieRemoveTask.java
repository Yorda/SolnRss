package free.solnRss.task;

import android.content.Context;
import android.os.AsyncTask;
import free.solnRss.activity.SyndicationsCategorieActivity;
import free.solnRss.repository.CategorieRepository;

public class SyndicationCategorieRemoveTask extends
		AsyncTask<Integer, Void, Void> {

	private CategorieRepository repository;
	private Context context;

	public SyndicationCategorieRemoveTask(Context context) {
		this.context = context;
	}

	@Override
	protected Void doInBackground(Integer... args) {
		repository = new CategorieRepository(context);
		repository.removeSyndicationToCategorie(args[0], args[1]);
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		((SyndicationsCategorieActivity) context).reload();
	}
}
