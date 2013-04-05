package free.solnRss.activity;

import android.app.ListActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SearchView;
import free.solnRss.R;
import free.solnRss.adapter.SyndicationsCategorieAdapter;
import free.solnRss.task.SyndicationCategoryAddTask;
import free.solnRss.task.SyndicationCategoryLoaderTask;
import free.solnRss.task.SyndicationCategoryReloaderTask;
import free.solnRss.task.SyndicationCategoryRemoveTask;

public class SyndicationsCategoriesActivity extends ListActivity {
	final int layoutID = R.layout.activity_syndications_categorie;
	private Integer selectedCategorieID;
	private SearchView searchView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(layoutID);
		selectedCategorieID = getIntent()
				.getIntExtra("selectedCategorieID", -1);
		if (selectedCategorieID == -1) {
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_soln_rss, menu);
		MenuItem searchItem = menu.findItem(R.id.action_search);
		searchView = (SearchView) searchItem.getActionView();
		setupSearchView(searchItem);
		return true;
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		loadSyndicationCategorie();
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
		SyndicationCategoryReloaderTask task = new SyndicationCategoryReloaderTask(
				this, (SyndicationsCategorieAdapter) getListAdapter());
		task.execute(selectedCategorieID);
	}

	private void setupSearchView(MenuItem searchItem) {
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
				filterCategorie(query);
				return false;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
				filterCategorie(newText);
				return true;
			}
		});
	}
	
	private void filterCategorie(String newText) {
		if (this.getListView() != null) {
			if (TextUtils.isEmpty(newText)) {
				this.getListView().clearTextFilter();
			} else {
				this.getListView().setFilterText(newText);
			}
		}
	}
}
