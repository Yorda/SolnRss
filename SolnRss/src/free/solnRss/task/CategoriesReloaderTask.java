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
public class CategoriesReloaderTask extends AsyncTask<Integer, Void, Cursor> {
	final int emptyMessageID = R.id.emptyCategoriesMessage;
	private CategoryRepository repository;
	private Context context;
	private ListFragment fragment;

	public CategoriesReloaderTask(ListFragment fragment, Context context) {
		this.context = context;
		this.fragment = fragment;
	}

	@Override
	protected Cursor doInBackground(Integer... ids) {
		repository = new CategoryRepository(context);
		return repository.fetchAllCategorie();
	}

	@Override
	protected void onPostExecute(Cursor result) {
		((CategorieAdapter) fragment.getListAdapter()).swapCursor(result);
		if (fragment.getListAdapter().isEmpty()) {
			displayEmptyMessage();
		} else {
			hideEmptyMessage();
		}
		repository.close();
	}
	
	private void displayEmptyMessage() {
		((TextView) ((SolnRss) context).findViewById(emptyMessageID))
				.setVisibility(View.VISIBLE);
	}

	private void hideEmptyMessage() {
		((TextView) ((SolnRss) context).findViewById(emptyMessageID))
				.setVisibility(View.INVISIBLE);
	}
	
}
