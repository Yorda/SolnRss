package free.solnRss.task;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.app.ListFragment;
import android.view.View;
import free.solnRss.R;
import free.solnRss.adapter.CategorieAdapter;
import free.solnRss.repository.CategoryRepository;

/**
 * Task for load the syndication list.
 * 
 * @author jf.tomasi
 * 
 */
@Deprecated
public class CategoriesLoaderTask extends
		AsyncTask<Void, Void, CategorieAdapter> {

	CategoryRepository repository;
	ListFragment fragment;
	Activity activity;

	private final String[] from = { "cat_name" };
	private final int[] to = { android.R.id.text1 };

	public CategoriesLoaderTask(ListFragment fragment, Activity activity) {
		this.activity = activity;
		this.fragment = fragment;
	}

	@Override
	protected CategorieAdapter doInBackground(Void... params) {
		repository = new CategoryRepository(activity);
		Cursor c = repository.fetchAllCategorie();
		c.moveToFirst();
		CategorieAdapter adapter = new CategorieAdapter(activity,
				R.layout.categories, c, from, to, 0);
		return adapter;
	}

	@Override
	protected void onPostExecute(CategorieAdapter result) {
		super.onPostExecute(result);

		fragment.setListAdapter(result);

		if (fragment.getListAdapter() == null
				|| fragment.getListAdapter().isEmpty()) {
			displayEmptyMessage();
		} else {
			hideyEmptyMessage();
		}
		repository.close();
	}

	private void displayEmptyMessage() {
		if (activity.findViewById(R.id.emptycategoriesLayout) != null) {
			((View) activity.findViewById(R.id.emptycategoriesLayout))
					.setVisibility(View.VISIBLE);
		}
	}

	private void hideyEmptyMessage() {
		if (activity.findViewById(R.id.emptycategoriesLayout) != null) {
			((View) activity.findViewById(R.id.emptycategoriesLayout))
					.setVisibility(View.INVISIBLE);
		}
	}
}
