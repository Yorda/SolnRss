package free.solnRss.fragment;


import java.util.Arrays;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import free.solnRss.R;
import free.solnRss.activity.SolnRss;
import free.solnRss.activity.SyndicationsCategoriesActivity;
import free.solnRss.adapter.CategoryAdapter;
import free.solnRss.dialog.OneEditTextDialogBox;
import free.solnRss.fragment.listener.CategoriesFragmentListener;
import free.solnRss.repository.CategoryRepository;


public class CategoriesFragment extends AbstractFragment implements CategoriesFragmentListener, SharedPreferences.OnSharedPreferenceChangeListener {

	private Integer				selectedCategoryID;
	private String				selectedCategoryName;
	private CategoryRepository	categoryRepository;

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup vg, final Bundle savedInstanceState) {
		selectedCategoryID = null;
		selectedCategoryName = null;
		final View fragment = inflater.inflate(R.layout.fragment_categories, vg, false);

		emptyLayoutId = R.id.emptycategoriesLayout;

		listContainer = fragment.findViewById(R.id.categoriesListContainer);
		progressContainer = fragment.findViewById(R.id.categoriesProgressContainer);
		listShown = true;

		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		return fragment;
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		categoryRepository = new CategoryRepository(getActivity());
		registerForContextMenu(getListView());
		((SolnRss) getActivity()).setCategoriesFragmentListener(this);

		setHasOptionsMenu(true);
		setListShown(false);
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		final Cursor c = ((CategoryAdapter) getListAdapter()).getCursor();
		c.moveToPosition(info.position);

		selectedCategoryID = c.getInt(c.getColumnIndex("_id"));
		selectedCategoryName = c.getString(c.getColumnIndex("cat_name"));
		menu.setHeaderTitle(selectedCategoryName);

		final MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.categories_context, menu);
	}

	@Override
	protected void initAdapter() {
		final String[] from = { "cat_name" };
		final int[] to = { android.R.id.text1 };
		simpleCursorAdapter = new CategoryAdapter(getActivity(), R.layout.categories, null, from, to, 0);

		setListAdapter(simpleCursorAdapter);
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle bundle) {
		return categoryRepository.loadCategories(getFilterText());
	}

	@Override
	protected void queryTheTextChange() {
		getLoaderManager().restartLoader(0, null, this);
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item) {
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

			case R.id.menu_rename_category:
				rename();
				break;

			default:
				break;
		}
		return super.onContextItemSelected(item);
	}

	private void markCategoryPublicationsAsRead() {

		final OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				((SolnRss) getActivity()).getPublicationsFragmentListener().markCategoryPublicationsAsRead(selectedCategoryID);
			}
		};

		final Resources r = getResources();
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(r.getString(R.string.confirm_mark_as_read, categoryName(selectedCategoryID)))
				.setNegativeButton(r.getString(android.R.string.cancel), null).setPositiveButton(r.getString(android.R.string.ok), listener).create()
				.show();
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {

		final Cursor cursor = ((CategoryAdapter) l.getAdapter()).getCursor();
		selectedCategoryID = cursor.getInt(cursor.getColumnIndex("_id"));
		((SolnRss) getActivity()).reLoadPublicationsByCategorie(selectedCategoryID);
	}

	public void startActivityForAddSyndication() {
		final Intent i = new Intent(getActivity(), SyndicationsCategoriesActivity.class);
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
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
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

	@Override
	public void addCategory(final Context context, final String newCatgorieName) {
		categoryRepository.addCategory(newCatgorieName);
	}

	private void rename() {
		OneEditTextDialogBox oneEditTextDialogBox;
		oneEditTextDialogBox = new OneEditTextDialogBox(getActivity(), categoryName(selectedCategoryID), getResources().getString(
				R.string.input_new_name), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				final EditText e = (EditText) ((AlertDialog) dialog).findViewById(R.id.one_edit_text_dialog);
				renameCategory(e.getText().toString());
			}
		});
		oneEditTextDialogBox.displayDialogBox();
	}

	private void renameCategory(final String newName) {
		categoryRepository.renameCategory(selectedCategoryID, newName);
		((SolnRss) getActivity()).refreshPublications();
	}

	public void deleteCategorie(final Context context, final Integer deletedCategoryId) {

		final OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {

				categoryRepository.deleteCategory(deletedCategoryId);

				//getLoaderManager().restartLoader(0, null, this);
				reLoadCategoriesAfterSyndicationDeleted();
				// Must warn publications time line to reload all publications if this deleted category
				((SolnRss) getActivity()).reLoadPublicationsAfterCatgoryDeleted(deletedCategoryId);
			}
		};

		final Resources r = getResources();
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(r.getString(R.string.confirm_delete, categoryName(deletedCategoryId)))
				.setNegativeButton(r.getString(android.R.string.cancel), null).setPositiveButton(r.getString(android.R.string.ok), listener).create()
				.show();

	}

	@Override
	public void reLoadCategoriesAfterSyndicationDeleted() {
		getLoaderManager().restartLoader(0, null, this);
	}

	@Override
	protected void setListPositionOnScreen() {

	}

	@Override
	public void moveListViewToTop() {
		getListView().setSelection(0);
	}

	final List<String>	preferenceKeys	= Arrays.asList(new String[] { "pref_sort_categories", "pref_user_font_face", "pref_user_font_size" });

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
		if (preferenceKeys.contains(key)) {
			reloadCategories();
		}
	}

}
