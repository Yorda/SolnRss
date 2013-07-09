package free.solnRss.fragment;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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

public class CategoriesFragment extends AbstractFragment implements
		CategoriesFragmentListener {
	
	private Integer selectedCategoryID;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup vg,
			Bundle savedInstanceState) {
		selectedCategoryID = null;
		View fragment = inflater.inflate(R.layout.fragment_categories, vg, false);
		
		emptyLayoutId = R.id.emptycategoriesLayout;	
		
		listContainer = fragment.findViewById(R.id.categoriesListContainer);
		progressContainer = fragment.findViewById(R.id.categoriesProgressContainer);
		listShown = true;
		
		return fragment;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registerForContextMenu(getListView());
		((SolnRss)getActivity()).setCategoriesFragmentListener(this);
		
		setListShown(false);
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,	ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		Cursor c = ((CategorieAdapter) getListAdapter()).getCursor();
		c.moveToPosition(info.position);
		
		selectedCategoryID = c.getInt(c.getColumnIndex("_id"));
		menu.setHeaderTitle(c.getString(c.getColumnIndex("cat_name")));
		
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.categories_context, menu);
	}
	

	@Override
	protected void initAdapter() {
		final String[] from = { "cat_name" };
		final int[] to = { android.R.id.text1 };
		simpleCursorAdapter = new CategorieAdapter(getActivity(),
				R.layout.categories, null, from, to, 0);
		
		setListAdapter(simpleCursorAdapter);
	}

	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		final String categoryTable = CategoryTable.CATEGORY_TABLE;
		final String columns[] = new String[] {
				categoryTable + "." + CategoryTable.COLUMN_ID,
				categoryTable + "." + CategoryTable.COLUMN_NAME };

		String selection = null;
		String[] args = null;
		
		if (!TextUtils.isEmpty(getFilterText())) {
			selection = CategoryTable.COLUMN_NAME + " like ? ";
			args = new String[1];
			args[0] = "%" + getFilterText().toString() + "%";
		}

		CursorLoader cursorLoader = new CursorLoader(getActivity(),
				CategoryProvider.URI, columns, selection, args, null);

		return cursorLoader;
	}
	
	@Override
	protected void queryTheTextChange() {
		getLoaderManager().restartLoader(0, null, this);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		
		case R.id.menu_delete_categorie:
			deleteCategorie(getActivity(), selectedCategoryID);
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
		selectedCategoryID = cursor.getInt(cursor.getColumnIndex("_id"));
		((SolnRss) getActivity()).reLoadPublicationsByCategorie(selectedCategoryID);
	}
	
	public void startActivityForAddSyndication() {
		Intent i = new Intent(getActivity(), SyndicationsCategoriesActivity.class);
		i.putExtra("selectedCategorieID", selectedCategoryID);
		startActivityForResult(i, 0);
	}
	
	@Override
	public void loadCategories() {
		if (getListAdapter() == null && isAdded()) {
			getLoaderManager().initLoader(0, null, this);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (isAdded()) {
			getLoaderManager().restartLoader(0, null, this);
		}
	}

	@Override
	public void reloadCategories() {
		if (isAdded()) {
			getLoaderManager().restartLoader(0, null, this);
		}
	}
	
	public void addCategorie(Context context, String newCatgorie) {
		ContentValues values = new ContentValues();
		values.put(CategoryTable.COLUMN_NAME, newCatgorie);
		getActivity().getContentResolver().insert(CategoryProvider.URI , values);
		getLoaderManager().restartLoader(0, null, this);
	}

	public void deleteCategorie(Context context, Integer categorieId) {
		
		ContentValues values = new ContentValues();
		values.put(CategoryTable.COLUMN_ID, categorieId);
		getActivity().getContentResolver().delete(CategoryProvider.URI,
				CategoryTable.COLUMN_ID + " = ? ",
				new String[] { categorieId.toString() });
		getLoaderManager().restartLoader(0, null, this);
		
		// Must warn publications time line to reload all publications if this deleted category
		((SolnRss)getActivity()).reLoadAllPublications();
	}

	@Override
	protected void setListPositionOnScreen() {
	}
	
}

