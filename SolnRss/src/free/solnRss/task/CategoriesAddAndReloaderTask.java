package free.solnRss.task;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.TextView;
import free.solnRss.R;
import free.solnRss.activity.SolnRss;
import free.solnRss.adapter.CategorieAdapter;
import free.solnRss.repository.CategoryRepository;

/**
 * @author jftomasi
 */
public class CategoriesAddAndReloaderTask extends AsyncTask<String, Void, Cursor> {
	final int emptyMessageID = R.id.emptyCategoriesMessage;
	private CategoryRepository repository;
	private Context context;
	private ListFragment fragment;

	public CategoriesAddAndReloaderTask(ListFragment fragment, Context context) {
		this.context = context;
		this.fragment = fragment;
	}

	@Override
	protected Cursor doInBackground(String... label) {
		repository = new CategoryRepository(context);
		repository.insert(label[0]);
		return repository.fetchAllCategorie();
	}

	@Override
	protected void onPostExecute(Cursor result) {
		//super.onPostExecute(result);
		if (fragment.getListAdapter() != null) {
			((CategorieAdapter) fragment.getListAdapter()).swapCursor(result);
		}
		if (fragment.getListAdapter() == null
				|| fragment.getListAdapter().isEmpty()) {
			displayEmptyMessage();
		} else {
			hideEmptyMessage();
		}
		repository.close();
	}

	private void displayEmptyMessage() {
		if (isViewDisplayed()) {
			((TextView) ((SolnRss) context).findViewById(emptyMessageID))
				.setVisibility(View.VISIBLE);
		}
	}

	private void hideEmptyMessage() {
		if (isViewDisplayed()) {
			((TextView) ((SolnRss) context).findViewById(emptyMessageID))
				.setVisibility(View.INVISIBLE);
		}
	}
	
	private boolean isViewDisplayed() {
		return ((TextView) ((SolnRss) context).findViewById(emptyMessageID)) != null;
	}
}
