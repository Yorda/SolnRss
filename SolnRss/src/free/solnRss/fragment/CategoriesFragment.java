package free.solnRss.fragment;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import free.solnRss.R;
import free.solnRss.activity.SolnRss;
import free.solnRss.activity.SyndicationsCategoriesActivity;
import free.solnRss.adapter.CategorieAdapter;
import free.solnRss.fragment.listener.CategoriesFragmentListener;
import free.solnRss.provider.CategoryProvider;
import free.solnRss.repository.CategoryTable;
import free.solnRss.task.CategoriesAddAndReloaderTask;
import free.solnRss.task.CategoriesDeleteAndReloaderTask;

public class CategoriesFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<Cursor>, CategoriesFragmentListener {
	
	private CategorieAdapter categorieAdapter;
	final private int layoutID = R.layout.fragment_categories;
	private Integer selectedCategorieID;
	
	private void provideCategories() {
		getLoaderManager().initLoader(0, null, this);
	}
	
	private final String[] from = { 
			"cat_name"
		};
	private final int[] to = {
			android.R.id.text1
		};
	
	private void initAdapter() {
		categorieAdapter = new CategorieAdapter(getActivity(),
				R.layout.categories, null, from, to, 0);
		setListAdapter(categorieAdapter);
	}

	private final String categoryTable = CategoryTable.CATEGORY_TABLE;
	private final String columns[] = new String[] {
			categoryTable + "." + CategoryTable.COLUMN_ID,
			categoryTable + "." + CategoryTable.COLUMN_NAME
		};
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {	
		CursorLoader cursorLoader = new CursorLoader(getActivity(),
				CategoryProvider.URI, columns, null, null, null);
		
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		if(categorieAdapter == null){
			initAdapter();
		}
		categorieAdapter.swapCursor(arg1);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		if(categorieAdapter == null){
			initAdapter();
		}
		categorieAdapter.swapCursor(null);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup vg,
			Bundle savedInstanceState) {
		selectedCategorieID = null;
		View fragment = inflater.inflate(layoutID, vg, false);
		return fragment;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registerForContextMenu(getListView());
		getListView().setTextFilterEnabled(true);
		((SolnRss)getActivity()).setCategoriesFragmentListener(this);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,	ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		Cursor c = ((CategorieAdapter) getListAdapter()).getCursor();
		c.moveToPosition(info.position);
		
		selectedCategorieID = c.getInt(c.getColumnIndex("_id"));
		menu.setHeaderTitle(c.getString(c.getColumnIndex("cat_name")));
		
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.categories_context, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		
		case R.id.menu_delete_categorie:
			deleteCategorie(getActivity(), selectedCategorieID);
			break;

		case R.id.menu_add_to_categorie:
			startActivityForAddSyndication();
			break;
			
		default:
			break;
		}
		return super.onContextItemSelected(item);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Cursor cursor = ((CategorieAdapter) l.getAdapter()).getCursor();
		selectedCategorieID = cursor.getInt(cursor.getColumnIndex("_id"));
		((SolnRss) getActivity()).reLoadPublicationsByCategorie(selectedCategorieID);
		
		clickOnCategoryItem(l, v, position, id);
	}
	
	public void startActivityForAddSyndication() {
		Intent i = new Intent(getActivity(), SyndicationsCategoriesActivity.class);
		i.putExtra("selectedCategorieID", selectedCategorieID);
		startActivityForResult(i, 0);
	}
	
	@Override
	public void loadCategories(Context context) {
		if (getListAdapter() == null) {
			provideCategories();
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		getLoaderManager().restartLoader(0, null, this);
	}

	@Override
	public void reloadCategories(Context context) {
		if (isAdded()) {
			getLoaderManager().restartLoader(0, null, this);
		}
	}
	
	public void addCategorie(Context context, String newCatgorie) {
		CategoriesAddAndReloaderTask task = new CategoriesAddAndReloaderTask(this, context);
		task.execute(newCatgorie);
	}
	
	public void deleteCategorie(Context context, Integer categorieId) {
		CategoriesDeleteAndReloaderTask task = new CategoriesDeleteAndReloaderTask(this, context);
		task.execute(categorieId);
	}
	
	private void clickOnCategoryItem(ListView l, View v, int position, long id) {
	}


	private String filterText;

	@Override
	public void filterCategories(String text) {
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
	
}

