package free.solnRss.activity;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import free.solnRss.R;
import free.solnRss.adapter.SyndicationsCategorieAdapter;
import free.solnRss.task.SyndicationCategoryAddTask;
import free.solnRss.task.SyndicationCategoryLoaderTask;
import free.solnRss.task.SyndicationCategoryReloaderTask;
import free.solnRss.task.SyndicationCategoryRemoveTask;

public class SyndicationsCategoriesActivity extends ListActivity {
	final int layoutID = R.layout.activity_syndications_categorie;
	private Integer selectedCategorieID;
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
	protected void onStart() {
		super.onStart();
		loadSyndicationCategorie();
	}

	void loadSyndicationCategorie() {
		SyndicationCategoryLoaderTask task = new SyndicationCategoryLoaderTask(this);
		task.execute(selectedCategorieID);
	}

	public void addSyndicationToCategorie(Integer syndicationId) {
		SyndicationCategoryAddTask addTask = new SyndicationCategoryAddTask(
				this);
		addTask.execute(syndicationId, selectedCategorieID);
	}

	public void removeSyndicationToCategorie(Integer syndicationId) {
		SyndicationCategoryRemoveTask removeTask = new SyndicationCategoryRemoveTask(
				this);
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

}
