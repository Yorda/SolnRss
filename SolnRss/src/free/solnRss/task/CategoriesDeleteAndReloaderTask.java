package free.solnRss.task;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.app.ListFragment;
import android.view.View;
import free.solnRss.R;
import free.solnRss.activity.SolnRss;
import free.solnRss.adapter.CategorieAdapter;
import free.solnRss.repository.CategoryRepository;

@Deprecated
public class CategoriesDeleteAndReloaderTask extends
		AsyncTask<Integer, Void, Cursor> {

	private CategoryRepository repository;
	private Context context;
	private ListFragment fragment;

	public CategoriesDeleteAndReloaderTask(ListFragment fragment,
			Context context) {
		this.context = context;
		this.fragment = fragment;
	}

	@Override
	protected Cursor doInBackground(Integer... ids) {
		repository = new CategoryRepository(context);
		repository.delete(ids[0]);
		return repository.fetchAllCategorie();
	}

	@Override
	protected void onPostExecute(Cursor result) {
		if (fragment.getListAdapter() != null) {
			((CategorieAdapter) fragment.getListAdapter()).swapCursor(result);
		}

		if (fragment.getListAdapter() == null
				|| fragment.getListAdapter().isEmpty()) {
			displayEmptyMessage();
		} else {
			hideEmptyMessage();
		}
	}

	private void displayEmptyMessage() {
		if (isViewDisplayed()) {
			((View) ((SolnRss) context)
					.findViewById(R.id.emptycategoriesLayout))
					.setVisibility(View.VISIBLE);
		}
	}

	private void hideEmptyMessage() {
		if (isViewDisplayed()) {
			((View) ((SolnRss) context)
					.findViewById(R.id.emptycategoriesLayout))
					.setVisibility(View.INVISIBLE);
		}
	}

	private boolean isViewDisplayed() {
		return ((View) ((SolnRss) context)
				.findViewById(R.id.emptycategoriesLayout)) != null;
	}
}