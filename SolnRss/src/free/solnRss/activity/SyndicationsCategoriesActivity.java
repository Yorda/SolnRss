package free.solnRss.activity;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SearchView;
import free.solnRss.R;
import free.solnRss.adapter.SyndicationsCategorieAdapter;
import free.solnRss.provider.SyndicationsProvider;
import free.solnRss.repository.SyndicationTable;
import free.solnRss.task.SyndicationCategoryAddTask;
import free.solnRss.task.SyndicationCategoryLoaderTask;
import free.solnRss.task.SyndicationCategoryRemoveTask;

public class SyndicationsCategoriesActivity extends ListActivity implements
		LoaderManager.LoaderCallbacks<Cursor> {
	
	final int layoutID = R.layout.activity_syndications_categorie;
	private Integer selectedCategorieID;
	private String selectedCategoryName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(layoutID);
		
		getActionBar().setBackgroundDrawable(new ColorDrawable(0xeeeeee));
		
		selectedCategorieID = getIntent().getIntExtra("selectedCategorieID", -1);
		selectedCategoryName = getIntent().getStringExtra("selectedCategoryName");
		
		if (!TextUtils.isEmpty(selectedCategoryName)) {
			getActionBar().setTitle(Html.fromHtml("<b><u>" + selectedCategoryName + "</u><b>"));
		}
		
		if (selectedCategorieID == -1) {
			finish();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.syndications_categories_menu, menu);
		MenuItem item =  menu.add("Search"); 
		item.setIcon(R.drawable.ic_abar_search);
		SearchView searchView =  new SearchView(this);
		setupSearchView(item, searchView);
		item.setActionView(searchView);
		return true;
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		//loadSyndicationCategorie();
		getLoaderManager().initLoader(0, null, this);
		getListView().setTextFilterEnabled(true);
	}

	void loadSyndicationCategorie() {
		SyndicationCategoryLoaderTask task = new SyndicationCategoryLoaderTask(this);
		task.execute(selectedCategorieID);
	}

	public void addSyndicationToCategorie(Integer syndicationId) {
		SyndicationCategoryAddTask addTask = new SyndicationCategoryAddTask(this);
		addTask.execute(syndicationId, selectedCategorieID);
	}

	public void removeSyndicationToCategorie(Integer syndicationId) {
		SyndicationCategoryRemoveTask removeTask = new SyndicationCategoryRemoveTask(this);
		removeTask.execute(syndicationId, selectedCategorieID);
	}

	public void checkBoxHandler(View v) {
		CheckBox cb = (CheckBox) v;
		Integer syndicationId = (Integer) cb.getTag();
		if (cb.isChecked()) {
			addSyndicationToCategorie(syndicationId);
		} else {
			removeSyndicationToCategorie(syndicationId);
		}
	}

	public void reload() {
		getLoaderManager().restartLoader(0, null, this);
	}

	private void setupSearchView(MenuItem searchItem, SearchView searchView) {
		boolean isAlwaysExpanded = false;
		if (isAlwaysExpanded) {
			searchView.setIconifiedByDefault(false);
		} else {
			searchItem.setShowAsAction(
					MenuItem.SHOW_AS_ACTION_IF_ROOM
					| MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW
				);
		}
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				filterCategories(query);
				return false;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
				filterCategories(newText);
				return true;
			}
		});
	}
	private String filterText = null;
	private void filterCategories(String newText) {
		if (this.getListView() != null) {
			if (TextUtils.isEmpty(newText)) {
				this.getListView().clearTextFilter();
				filterText = null;
			} else {
				this.getListView().setFilterText(newText);
				filterText = newText;
			}
		}
	}
	
	private final String[] from = { "syn_name" };
	private final int[] to = { android.R.id.text1 };
	
	SyndicationsCategorieAdapter adapter;
	private void initAdapter() {		
		adapter = new SyndicationsCategorieAdapter(this,R.layout.syndications_categorie, null, from, to, 0);
		setListAdapter(adapter);
		adapter.setSelectedCategoryId(selectedCategorieID);
	}
	
	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		
		Uri uri = Uri.parse(SyndicationsProvider.URI 
				+ "/selectedCategoryId/"
				+ selectedCategorieID);

		String selection = null;
		String[] args    = null;

		if (!TextUtils.isEmpty(filterText)) {
			selection = SyndicationTable.COLUMN_NAME + " like ? ";
			args = new String[1];
			args[0] = "%" + filterText + "%";
		}
		
		CursorLoader cursorLoader = new CursorLoader(this, uri,
				SyndicationsProvider.syndicationByCategoryProjection, selection, args, null);
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if(adapter == null){
			initAdapter();
		}
		if (cursor.getCount() < 1) {
			findViewById(R.id.emptySyndicationCategoryLayout)
					.setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.emptySyndicationCategoryLayout)
					.setVisibility(View.INVISIBLE);
		}
		
		adapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if(adapter == null){
			initAdapter();
		}
		adapter.swapCursor(null);
	}
}
