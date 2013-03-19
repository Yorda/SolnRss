package free.solnRss.fragment;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
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
import free.solnRss.task.CategoriesAddAndReloaderTask;
import free.solnRss.task.CategoriesDeleteAndReloaderTask;
import free.solnRss.task.CategoriesLoaderTask;
import free.solnRss.task.CategoriesReloaderTask;

public class CategoriesFragment extends ListFragment implements
		CategoriesFragmentListener {

	final private int layoutID = R.layout.fragment_categories;
	private Integer selectedCategorieID;
	
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
	}
	
	public void startActivityForAddSyndication() {
		Intent i = new Intent(getActivity(), SyndicationsCategoriesActivity.class);
		i.putExtra("selectedCategorieID", selectedCategorieID);
		startActivityForResult(i, 0);
	}
	
	@Override
	public void loadCategories(Context context) {
		CategoriesLoaderTask task = new CategoriesLoaderTask(this, (SolnRss) context);
		task.execute();
	}

	@Override
	public void reloadCategories(Context context) {
		CategoriesReloaderTask task = new CategoriesReloaderTask(this, context);
		task.execute();
	}
	
	public void addCategorie(Context context, String newCatgorie) {
		CategoriesAddAndReloaderTask task = new CategoriesAddAndReloaderTask(this, context);
		task.execute(newCatgorie);
	}
	
	public void deleteCategorie(Context context, Integer categorieId) {
		CategoriesDeleteAndReloaderTask task = new CategoriesDeleteAndReloaderTask(this, context);
		task.execute(categorieId);
	}
}
