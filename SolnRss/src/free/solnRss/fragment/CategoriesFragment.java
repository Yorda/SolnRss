package free.solnRss.fragment;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
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
import free.solnRss.repository.CategoryRepository;
import free.solnRss.repository.CategoryTable;

public class CategoriesFragment extends AbstractFragment implements
		CategoriesFragmentListener {
	
	private Integer selectedCategoryID;
	private String selectedCategoryName;
	private CategoryRepository categoryRepository;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup vg,
			Bundle savedInstanceState) {
		selectedCategoryID = null;
		selectedCategoryName = null;
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
		categoryRepository = new CategoryRepository(getActivity());
		registerForContextMenu(getListView());
		((SolnRss)getActivity()).setCategoriesFragmentListener(this);

		setHasOptionsMenu(true);
		setListShown(false);
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
		selectedCategoryName = c.getString(c.getColumnIndex("cat_name"));
		menu.setHeaderTitle(selectedCategoryName);
		
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
		return categoryRepository.loadCategories(getFilterText());
	}
	
	@Override
	protected void queryTheTextChange() {
		getLoaderManager().restartLoader(0, null, this);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_mark_category_read:
			markCategoryPublicationsAsRead();
			break;

		case R.id.menu_add_to_categorie:
			startActivityForAddSyndication();
			break;
			
		case R.id.menu_delete_categorie:
			deleteCategorie(getActivity(), selectedCategoryID);
			break;

		default:
			break;
		}
		return super.onContextItemSelected(item);
	}

	private void markCategoryPublicationsAsRead() {
		
		OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				((SolnRss)getActivity()).getPublicationsFragmentListener().markCategoryPublicationsAsRead(selectedCategoryID);
			}
		};
		
		Resources r = getResources();
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(r.getString(R.string.confirm_mark_as_read, categoryName(selectedCategoryID)))
			.setNegativeButton(r.getString(android.R.string.cancel), null)
			.setPositiveButton(r.getString(android.R.string.ok), listener)
			.create().show();
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
		i.putExtra("selectedCategoryName", selectedCategoryName);
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

	public void deleteCategorie(Context context, final Integer deletedCategoryId) {
		
		OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				ContentValues values = new ContentValues();
				values.put(CategoryTable.COLUMN_ID, deletedCategoryId);
				getActivity().getContentResolver().delete(CategoryProvider.URI,
						CategoryTable.COLUMN_ID + " = ? ",
						new String[] { deletedCategoryId.toString() 
				});
				//getLoaderManager().restartLoader(0, null, this);
				reLoadCategoriesAfterSyndicationDeleted();
				// Must warn publications time line to reload all publications if this deleted category
				((SolnRss)getActivity()).reLoadPublicationsAfterCatgoryDeleted(deletedCategoryId);
			}
		};
		
		Resources r = getResources();
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(r.getString(R.string.confirm_delete, categoryName(deletedCategoryId)))
				.setNegativeButton(r.getString(android.R.string.cancel), null)
				.setPositiveButton(r.getString(android.R.string.ok), listener)
				.create().show();
		
		
	}

	@Override
	public void reLoadCategoriesAfterSyndicationDeleted() {
		getLoaderManager().restartLoader(0, null, this);
	}
	
	@Override
	protected void setListPositionOnScreen() {
		
	}
	
}

