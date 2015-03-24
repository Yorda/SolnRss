package free.solnRss.activity;


import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
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
import free.solnRss.repository.SyndicationsByCategoryRepository;
import free.solnRss.task.SyndicationCategoryAddTask;
import free.solnRss.task.SyndicationCategoryRemoveTask;


public class SyndicationsCategoriesActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {

	final int									layoutID	= R.layout.activity_syndications_categorie;
	private Integer								selectedCategorieID;
	private String								selectedCategoryName;

	private final String[]						from		= { "syn_name" };
	private final int[]							to			= { android.R.id.text1 };

	private SyndicationsCategorieAdapter		adapter;

	private SyndicationsByCategoryRepository	syndicationsByCategoryRepository;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(layoutID);

		getActionBar().setBackgroundDrawable(new ColorDrawable(0xeeeeee));

		selectedCategorieID = getIntent().getIntExtra("selectedCategorieID", -1);
		selectedCategoryName = getIntent().getStringExtra("selectedCategoryName");

		if (!TextUtils.isEmpty(selectedCategoryName)) {
			getActionBar().setTitle(Html.fromHtml("<b><u>" + selectedCategoryName + "</u></b>"));
		}

		if (selectedCategorieID == -1) {
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.syndications_categories_menu, menu);
		final MenuItem item = menu.add("Search");
		item.setIcon(R.drawable.ic_abar_search);
		final SearchView searchView = new SearchView(this);
		setupSearchView(item, searchView);
		item.setActionView(searchView);
		return true;
	}

	@Override
	protected void onStart() {
		super.onStart();
		syndicationsByCategoryRepository = new SyndicationsByCategoryRepository(this);
		getLoaderManager().initLoader(0, null, this);
		getListView().setTextFilterEnabled(true);
	}

	public void addSyndicationToCategorie(final Integer syndicationId) {
		final SyndicationCategoryAddTask addTask = new SyndicationCategoryAddTask(this);
		addTask.execute(syndicationId, selectedCategorieID);
	}

	public void removeSyndicationToCategorie(final Integer syndicationId) {
		final SyndicationCategoryRemoveTask removeTask = new SyndicationCategoryRemoveTask(this);
		removeTask.execute(syndicationId, selectedCategorieID);
	}

	public void checkBoxHandler(final View v) {
		final CheckBox cb = (CheckBox) v;
		final Integer syndicationId = (Integer) cb.getTag();
		if (cb.isChecked()) {
			addSyndicationToCategorie(syndicationId);
		} else {
			removeSyndicationToCategorie(syndicationId);
		}
	}

	public void reload() {
		getLoaderManager().restartLoader(0, null, this);
	}

	private void setupSearchView(final MenuItem searchItem, final SearchView searchView) {
		final boolean isAlwaysExpanded = false;
		if (isAlwaysExpanded) {
			searchView.setIconifiedByDefault(false);
		} else {
			searchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		}
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(final String query) {
				filterCategories(query);
				return false;
			}

			@Override
			public boolean onQueryTextChange(final String newText) {
				filterCategories(newText);
				return true;
			}
		});
	}

	private String	filterText	= null;

	private void filterCategories(final String newText) {
		if (getListView() != null) {
			if (TextUtils.isEmpty(newText)) {
				getListView().clearTextFilter();
				filterText = null;
			} else {
				getListView().setFilterText(newText);
				filterText = newText;
			}
		}
	}

	private void initAdapter() {
		adapter = new SyndicationsCategorieAdapter(this, R.layout.syndications_categorie, null, from, to, 0);
		setListAdapter(adapter);
		adapter.setSelectedCategoryId(selectedCategorieID);
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int arg0, final Bundle arg1) {
		return syndicationsByCategoryRepository.loadSyndicationsByCategory(selectedCategorieID, filterText);
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
		if (adapter == null) {
			initAdapter();
		}
		if (cursor.getCount() < 1) {
			findViewById(R.id.emptySyndicationCategoryLayout).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.emptySyndicationCategoryLayout).setVisibility(View.INVISIBLE);
		}

		adapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader) {
		if (adapter == null) {
			initAdapter();
		}
		adapter.swapCursor(null);
	}
}
