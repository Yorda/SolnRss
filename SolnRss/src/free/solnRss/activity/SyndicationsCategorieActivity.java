package free.solnRss.activity;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import free.solnRss.R;
import free.solnRss.adapter.SyndicationsCategorieAdapter;
import free.solnRss.task.SyndicationCategorieAddTask;
import free.solnRss.task.SyndicationCategorieLoaderTask;
import free.solnRss.task.SyndicationCategorieReloaderTask;
import free.solnRss.task.SyndicationCategorieRemoveTask;

public class SyndicationsCategorieActivity extends ListActivity {
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
		SyndicationCategorieLoaderTask task = new SyndicationCategorieLoaderTask(this);
		task.execute(selectedCategorieID);
	}

	public void addSyndicationToCategorie(Integer syndicationId) {
		SyndicationCategorieAddTask addTask = new SyndicationCategorieAddTask(
				this);
		addTask.execute(syndicationId, selectedCategorieID);
	}

	public void removeSyndicationToCategorie(Integer syndicationId) {
		SyndicationCategorieRemoveTask removeTask = new SyndicationCategorieRemoveTask(
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
		SyndicationCategorieReloaderTask task = new SyndicationCategorieReloaderTask(
				this, (SyndicationsCategorieAdapter) getListAdapter());
		task.execute(selectedCategorieID);
	}

}
