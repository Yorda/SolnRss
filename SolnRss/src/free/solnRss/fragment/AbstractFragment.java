package free.solnRss.fragment;

import android.support.v4.app.ListFragment;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;


public abstract class AbstractFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {
	
	protected abstract void initAdapter();
	
	protected SimpleCursorAdapter simpleCursorAdapter;
	
	@Override
	public abstract Loader<Cursor> onCreateLoader(int id, Bundle bundle);

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
		}
	}
	
	protected int emptyLayoutId;
	
	private void displayEmptyMessage() {
		LinearLayout l = (LinearLayout) getActivity().findViewById(emptyLayoutId);
		l.setVisibility(View.VISIBLE);
	}

	private void hideEmptyMessage() {
		LinearLayout l = (LinearLayout) getActivity().findViewById(emptyLayoutId);
		l.setVisibility(View.INVISIBLE);
	}
	
	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		if(simpleCursorAdapter == null){
			initAdapter();
		}
		simpleCursorAdapter.swapCursor(null);
	}
	
	private String filterText;

	public void makeFilterInListView(String text) {
		if (this.getListView() != null) {
			if (TextUtils.isEmpty(text)) {
				setFilterText(null);
				this.getListView().clearTextFilter();
			} else {
				setFilterText(text);
				this.getListView().setFilterText(text);
			}
		}
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
