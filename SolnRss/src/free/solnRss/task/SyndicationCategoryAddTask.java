package free.solnRss.task;


import android.content.Context;
import android.os.AsyncTask;
import free.solnRss.activity.SyndicationsCategoriesActivity;
import free.solnRss.repository.CategoryRepository;


public class SyndicationCategoryAddTask extends AsyncTask<Integer, Void, Void> {

	private CategoryRepository	repository;
	private Context				context;

	public SyndicationCategoryAddTask(final Context context) {
		this.context = context;
	}

	@Override
	protected Void doInBackground(final Integer... args) {
		repository = new CategoryRepository(context);
		repository.addSyndicationToCategorie(args[0], args[1]);
		return null;
	}

	@Override
	protected void onPostExecute(final Void result) {
		super.onPostExecute(result);
		((SyndicationsCategoriesActivity) context).reload();
	}
}
