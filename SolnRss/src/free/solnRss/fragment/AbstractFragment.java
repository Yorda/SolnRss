package free.solnRss.fragment;


import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SimpleCursorAdapter;
import free.solnRss.R;
import free.solnRss.provider.SolnRssProvider;
import free.solnRss.repository.CategoryTable;
import free.solnRss.repository.SyndicationTable;


public abstract class AbstractFragment extends ListFragment implements OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor> {

	protected SimpleCursorAdapter	simpleCursorAdapter;

	private String					filterText;

	protected int					emptyLayoutId;

	protected abstract void initAdapter();

	protected abstract void queryTheTextChange();

	@Override
	public abstract Loader<Cursor> onCreateLoader(int id, Bundle bundle);

	protected abstract void setListPositionOnScreen();

	protected String syndicationName(final Integer id) {
		final String[] projection = { SyndicationTable.COLUMN_NAME };
		final Uri uri = Uri.parse(SolnRssProvider.URI + "/syndication");
		final Cursor c = getActivity().getContentResolver().query(uri, projection, " _id = ? ", new String[] { id.toString() }, null);
		c.moveToFirst();
		final String name = c.getCount() > 0 && c.getString(0) != null ? c.getString(0) : null;
		c.close();
		return name;
	}

	protected String categoryName(final Integer id) {
		final Uri uri = Uri.parse(SolnRssProvider.URI + "/category_name");
		final String[] projection = { CategoryTable.COLUMN_NAME };
		final Cursor c = getActivity().getContentResolver().query(uri, projection, " _id = ? ", new String[] { id.toString() }, null);
		c.moveToFirst();
		final String name = c.getCount() > 0 && c.getString(0) != null ? c.getString(0) : null;
		c.close();
		return name;
	}

	protected boolean isLoaderManagerAlreadyStarted() {
		return getLoaderManager() != null && getLoaderManager().getLoader(0) != null && getLoaderManager().getLoader(0).isStarted();
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> arg0, final Cursor arg1) {

		if (simpleCursorAdapter == null) {
			initAdapter();
		}

		// The list should now be shown.
		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}

		simpleCursorAdapter.swapCursor(arg1);

		if (getListAdapter().isEmpty()) {
			displayEmptyMessage();
		} else {
			hideEmptyMessage();

			simpleCursorAdapter.notifyDataSetChanged();

			setListPositionOnScreen();
		}
		getActivity().setProgressBarIndeterminateVisibility(false);
	}

	protected void displayEmptyMessage() {
		final LinearLayout l = (LinearLayout) getActivity().findViewById(emptyLayoutId);
		l.setVisibility(View.VISIBLE);
	}

	protected void hideEmptyMessage() {
		final LinearLayout l = (LinearLayout) getActivity().findViewById(emptyLayoutId);
		l.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> arg0) {
		if (simpleCursorAdapter == null) {
			initAdapter();
		}
		simpleCursorAdapter.swapCursor(null);
	}

	@Override
	public void onPrepareOptionsMenu(final Menu menu) {
		onQueryTextChange("");
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {

		inflater.inflate(R.menu.activity_soln_rss, menu);

		final MenuItem item = menu.add("Search");
		item.setIcon(R.drawable.ic_abar_search);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

		final SearchView sv = new SearchView(getActivity());
		sv.setOnQueryTextListener(this);
		item.setActionView(sv);
	}

	//private Menu menu;

	/*
	 * public void addNumberOfLastFoundInMenu(Integer muberOfLastFound) { MenuItem item = menu.add("count");
	 * item.setIcon(writeOnDrawable(getActivity(),R.drawable.ic_abar_search, String.valueOf(muberOfLastFound)));
	 * item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW); } private BitmapDrawable
	 * writeOnDrawable(Context mContext,int drawableId, String text){ Bitmap bm = BitmapFactory.decodeResource(getResources(),
	 * drawableId).copy(Bitmap.Config.ARGB_8888, true); Paint paint = new Paint(); paint.setStyle(Style.FILL); paint.setColor(Color.RED);
	 * paint.setTextSize(22); paint.setTypeface(Typeface.create("",Typeface.BOLD)); paint.setFlags(Paint.ANTI_ALIAS_FLAG); Canvas canvas = new
	 * Canvas(bm); canvas.drawText(text, 0, bm.getHeight()/2, paint); return new BitmapDrawable(mContext.getResources(),bm); }
	 */

	@Override
	public boolean onQueryTextChange(final String newText) {
		final String newFilter = !TextUtils.isEmpty(newText) ? newText : null;
		if (getFilterText() == null && newFilter == null) {
			getListView().clearTextFilter();
			return true;
		}
		if (getFilterText() != null && getFilterText().equals(newFilter)) {
			return true;
		}
		setFilterText(newFilter);
		queryTheTextChange();
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(final String query) {
		return true;
	}

	public String getFilterText() {
		return filterText;
	}

	public void setFilterText(final String filterText) {
		this.filterText = filterText;
	}

	protected boolean	listShown;
	protected View		progressContainer;
	protected View		listContainer;

	public void setListShown(final boolean shown, final boolean animate) {
		final int fadeOut = android.R.anim.fade_out;
		final int fadeIn = android.R.anim.fade_in;

		if (listShown == shown) {
			return;
		}
		listShown = shown;
		if (shown) {
			if (animate) {
				progressContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(), fadeOut));
				listContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(), fadeIn));
			}
			progressContainer.setVisibility(View.GONE);
			listContainer.setVisibility(View.VISIBLE);
		} else {
			if (animate) {
				progressContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(), fadeIn));
				listContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(), fadeOut));
			}
			progressContainer.setVisibility(View.VISIBLE);
			listContainer.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void setListShown(final boolean shown) {
		setListShown(shown, true);
	}

	@Override
	public void setListShownNoAnimation(final boolean shown) {
		setListShown(shown, false);
	}

}
