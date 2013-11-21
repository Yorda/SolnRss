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


public abstract class AbstractFragment extends ListFragment implements	OnQueryTextListener, 
		LoaderManager.LoaderCallbacks<Cursor> {
	
	protected SimpleCursorAdapter simpleCursorAdapter;
	
	private String filterText;
	
	protected int emptyLayoutId;
	
	protected abstract void initAdapter();
	
	protected abstract void queryTheTextChange();
	
	@Override
	public abstract Loader<Cursor> onCreateLoader(int id, Bundle bundle);
	
	protected abstract void setListPositionOnScreen();

	protected String syndicationName(Integer id) {
		String[] projection = { SyndicationTable.COLUMN_NAME };
		Uri uri = Uri.parse(SolnRssProvider.URI + "/syndication");
		Cursor c = getActivity().getContentResolver().query(uri, projection,
				" _id = ? ", new String[] { id.toString() }, null);
		c.moveToFirst();
		String name = c.getCount() > 0 && c.getString(0) != null ? c
				.getString(0) : null;
		c.close();
		return name;
	}
	
	protected String categoryName(Integer id) {
		//Uri uri = Uri.parse(CategoryProvider.URI + "/" + id);
		Uri uri = Uri.parse(SolnRssProvider.URI + "/category_name");
		String[] projection = { CategoryTable.COLUMN_NAME };
		Cursor c = getActivity().getContentResolver().query(uri, projection,
				" _id = ? ",  new String[] { id.toString() }, null);
		c.moveToFirst();
		String name =  c.getCount() > 0 && c.getString(0) != null ? c.getString(0) : null;
		c.close();
		return name;
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		
		if(simpleCursorAdapter == null){
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
			setListPositionOnScreen();
			simpleCursorAdapter.notifyDataSetChanged();
		}
	}
	
	protected void displayEmptyMessage() {
		LinearLayout l = (LinearLayout) getActivity().findViewById(emptyLayoutId);
		l.setVisibility(View.VISIBLE);
	}

	protected void hideEmptyMessage() {
		LinearLayout l = (LinearLayout) getActivity().findViewById(emptyLayoutId);
		l.setVisibility(View.INVISIBLE);
	}
	
	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		if (simpleCursorAdapter == null) {
			initAdapter();
		}
		simpleCursorAdapter.swapCursor(null);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		onQueryTextChange("");
		super.onPrepareOptionsMenu(menu);
	}
	
	@Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		inflater.inflate(R.menu.activity_soln_rss, menu);
		
	    MenuItem item = menu.add("Search");
	    
	    item.setIcon(R.drawable.ic_abar_search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
               // | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        
        SearchView sv = new SearchView(getActivity());
        sv.setOnQueryTextListener(this);
        item.setActionView(sv);
    }

	@Override public boolean onQueryTextChange(String newText) {
        String newFilter = !TextUtils.isEmpty(newText) ? newText : null;
        if (getFilterText() == null && newFilter == null) {
        	this.getListView().clearTextFilter();
            return true;
        }
        if (getFilterText() != null && getFilterText().equals(newFilter)) {
            return true;
        }
        setFilterText(newFilter);
        queryTheTextChange();
        return true;
    }
	
    @Override public boolean onQueryTextSubmit(String query) {
        return true;
    }
    
	public String getFilterText() {
		return filterText;
	}

	public void setFilterText(String filterText) {
		this.filterText = filterText;
	}

	protected boolean listShown;
	protected View progressContainer;
	protected View listContainer;

	public void setListShown(boolean shown, boolean animate) {
		if (listShown == shown) {
			return;
		}
		listShown = shown;
		if (shown) {
			if (animate) {
				progressContainer.startAnimation(AnimationUtils.loadAnimation(
						getActivity(), android.R.anim.fade_out));
				listContainer.startAnimation(AnimationUtils.loadAnimation(
						getActivity(), android.R.anim.fade_in));
			}
			progressContainer.setVisibility(View.GONE);
			listContainer.setVisibility(View.VISIBLE);
		} else {
			if (animate) {
				progressContainer.startAnimation(AnimationUtils.loadAnimation(
						getActivity(), android.R.anim.fade_in));
				listContainer.startAnimation(AnimationUtils.loadAnimation(
						getActivity(), android.R.anim.fade_out));
			}
			progressContainer.setVisibility(View.VISIBLE);
			listContainer.setVisibility(View.INVISIBLE);
		}
	}
	
	public void setListShown(boolean shown) {
		setListShown(shown, true);
	}

	public void setListShownNoAnimation(boolean shown) {
		setListShown(shown, false);
	}
}
