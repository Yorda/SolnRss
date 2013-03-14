package free.solnRss.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
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
import free.solnRss.activity.ReaderActivity;
import free.solnRss.adapter.PublicationAdapter;
import free.solnRss.repository.PublicationRepository;
import free.solnRss.task.PublicationsByCategoryLoaderTask;
import free.solnRss.task.PublicationsByCategoryReloaderTask;
import free.solnRss.task.PublicationsLoaderTask;
import free.solnRss.task.PublicationsReloaderTask;

public class PublicationsFragment extends ListFragment {

	final int layoutID = R.layout.fragment_publications;
	private PublicationRepository publicationRepository;
	private Integer selectedSyndicationID;
	private Integer nextSelectedSyndicationID; // selected by context menu
	private Integer selectedCategorieID;
	

	public Integer getSelectedSyndicationID(){
		return selectedSyndicationID;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle save) {
		publicationRepository = new PublicationRepository(getActivity());
		View fragment = inflater.inflate(layoutID, vg, false);
		restoreOrFirstDisplay(save);
		return fragment;
	}

	public void restoreOrFirstDisplay(Bundle save) {
		Log.e(PublicationsFragment.this.getClass().getName(), "BEGIN TO CREATE OR RESTORE ");

		if (save != null) {
			selectedSyndicationID = save.getInt("selectedSyndicationID") == 0 ? null
					: save.getInt("selectedSyndicationID");

			selectedCategorieID = save.getInt("selectedCategorieID") == 0 ? null
					: save.getInt("selectedCategorieID");
		}

		Log.e(PublicationsFragment.this.getClass().getName(),"Restore with Sid-> " + selectedSyndicationID + " cId-> " + selectedCategorieID);

		if (selectedSyndicationID != null) {
			loadPublicationsBySyndication(selectedSyndicationID);
		} else if (selectedCategorieID != null) {
			loadPublicationsByCategorie(selectedCategorieID);
		} else {
			loadAllPublications(getActivity());
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (selectedSyndicationID != null) {
			outState.putInt("selectedSyndicationID", selectedSyndicationID);
		}
		if (selectedCategorieID != null) {
			outState.putInt("selectedCategorieID", selectedCategorieID);
		}
		
		Log.e(PublicationsFragment.this.getClass().getName(),"Save sId-> " + selectedSyndicationID	+ " cId-> " + selectedCategorieID);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registerForContextMenu(getListView());
		getListView().setTextFilterEnabled(true);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Cursor cursor = ((PublicationAdapter) l.getAdapter()).getCursor();
		String description = hasDescriptionValue(cursor);
		String link = getClickedUrl(cursor);
		
		if (description != null && description.trim().length() > 0) {
			// TODO improve this
			// openReaderActivity(description, link);
			openBroswser(link);
		} else {
			openBroswser(link);
		}
		markPublicationRead(cursor);
	}
	
	private boolean isSyndicationOrCategorieSelected() {
		if (selectedSyndicationID != null || selectedCategorieID != null) {
			return true;
		}
		return false;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,	ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		nextSelectedSyndicationID = null;
		
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		Cursor c = ((PublicationAdapter) getListAdapter()).getCursor();
		c.moveToPosition(info.position);

		menu.setHeaderTitle(c.getString(c.getColumnIndex("syn_name")));
		nextSelectedSyndicationID = c.getInt(c.getColumnIndex("syn_syndication_id"));
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.publications_context, menu);

		if (isSyndicationOrCategorieSelected()) {
			menu.getItem(0).setTitle(
					getResources().getString(R.string.display_all_publication));
			menu.getItem(1).setTitle(
					getResources().getString(R.string.mark_selected_read));
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_see_only:
			// Make a switch between display a the selected syndication and all
			// syndication.
			if(!isSyndicationOrCategorieSelected()){
				selectedSyndicationID = nextSelectedSyndicationID;
				reLoadPublicationsBySyndication(this.selectedSyndicationID, getActivity());
				this.moveListViewToTop();
			} else {
				reLoadAllPublications(getActivity());
				this.moveListViewToTop();
			}
			
			break;

		case R.id.menu_mark_read:

			if (selectedSyndicationID != null && selectedCategorieID == null) {
				// Log.e(PublicationsFragment.this.getClass().getName(),
				// "Mark syndication as read");
				markSyndicationPublicationsAsRead(selectedSyndicationID);
			} else if (selectedCategorieID != null
					&& selectedSyndicationID == null) {
				// Log.e(PublicationsFragment.this.getClass().getName(),
				// "Mark category as read");
				markCategoryPublicationsAsRead(selectedCategorieID);
			} else {
				markAllPublicationsAsRead();
				// Log.e(PublicationsFragment.this.getClass().getName(),
				// "Mark all as read");
			}

			break;

		default:
			break;
		}
		return super.onContextItemSelected(item);
	}
	
	/**
	 * 
	 * @param text
	 * @param link
	 */
	public void openReaderActivity(String text, String link) {
		Intent i = new Intent(getActivity(), ReaderActivity.class);
		i.putExtra("read", text);
		i.putExtra("link", link);
		startActivity(i);
	}
	
	/**
	 * All unread publication set to read
	 */
	private void markAllPublicationsAsRead() {

		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				publicationRepository.markAllPublicationsAsRead();
				return null;
			}
		}.execute();
		
		reLoadAllPublications(getActivity());
	}

	/**
	 * 
	 */
	private void markSyndicationPublicationsAsRead(final Integer id) {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				// TODO must work with all syndication in a categories
				publicationRepository.markSyndicationPublicationsAsRead(id);
				return null;
				
			}
		}.execute();
		refreshPublications();
	}
	
	private void markCategoryPublicationsAsRead(final Integer id) {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				// TODO must work with all syndication in a categories
				publicationRepository.markCategoryPublicationsAsRead(id);
				return null;
				
			}
		}.execute();
		refreshPublications();
	}
	
	
	/**
	 * 
	 * @param url
	 */
	private void openBroswser(String url) {
		// Start a browser
		Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(openUrlIntent);
	}

	/**
	 * 
	 * @param l
	 * @param position
	 */
	public void moveListToPosition(ListView l, int position) {
		Cursor cursor = ((PublicationAdapter) l.getAdapter()).getCursor();
		cursor.moveToPosition(position);
	}

	/**
	 * 
	 * @param l
	 * @return
	 */
	public String getClickedUrl(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndex("pub_link"));
	}
	
	/**
	 * Set the clicked publication to read
	 * 
	 * @param cursor
	 * @param v
	 */
	public void markPublicationRead(Cursor cursor) {
		
		if (selectedSyndicationID != null) {
			PublicationsReloaderTask task = new PublicationsReloaderTask(this, getActivity());
			task.execute(selectedSyndicationID,	cursor.getInt(cursor.getColumnIndex("_id")));
		}
		else if (selectedCategorieID != null) {
			PublicationsByCategoryReloaderTask task = new PublicationsByCategoryReloaderTask(	this, getActivity());
			task.execute(selectedCategorieID, cursor.getInt(cursor.getColumnIndex("_id")));
		}
		else {
			PublicationsReloaderTask task = new PublicationsReloaderTask(this, getActivity());
			task.execute(null, cursor.getInt(cursor.getColumnIndex("_id")));
		}
	}
	
	/**
	 * 
	 * @param cursor
	 * @return
	 */
	public String hasDescriptionValue(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndex("pub_publication"));
	}
		
	private void refreshPublications() {
		if (this.selectedSyndicationID != null) {
			reLoadPublicationsBySyndication(selectedSyndicationID, getActivity());
		} else if (this.selectedCategorieID != null) {
			reLoadPublicationsByCategorie(this.selectedCategorieID,
					getActivity());
		} else {
			reLoadAllPublications(getActivity());
		}
	}
	
	public void loadAllPublications(Context context) {
		this.selectedSyndicationID = null;
		this.selectedCategorieID = null;
		PublicationsLoaderTask task = new PublicationsLoaderTask(this, context);
		// TODO add task without parameter value
		task.execute(selectedSyndicationID);
	}

	private void loadPublicationsBySyndication(Integer selectedSyndicationId) {
		this.selectedCategorieID = null;
		PublicationsLoaderTask task = new PublicationsLoaderTask(this, getActivity());
		task.execute(selectedSyndicationId);
	}

	private void loadPublicationsByCategorie(Integer selectedCategorieId) {
		this.selectedSyndicationID = null;
		PublicationsByCategoryLoaderTask task = new PublicationsByCategoryLoaderTask(this, getActivity());
		task.execute(selectedCategorieId);
	}

	public void reLoadAllPublications(Context context) {
		this.selectedSyndicationID = null;
		this.selectedCategorieID = null;
		PublicationsReloaderTask task = new PublicationsReloaderTask(this, context);
		// TODO add task without parameter value
		task.execute(selectedSyndicationID);
	}
	
	public void reLoadPublicationsBySyndication(Integer selectedSyndicationID, Context context) {
		this.selectedSyndicationID = selectedSyndicationID;
		this.selectedCategorieID = null;
		PublicationsReloaderTask task = new PublicationsReloaderTask(this, getActivity());
		task.execute(selectedSyndicationID);
	}

	public void reLoadPublicationsByCategorie(Integer selectedCategorieId, Context context) {
		this.selectedSyndicationID = null;
		this.selectedCategorieID = selectedCategorieId;
		PublicationsByCategoryReloaderTask task = new PublicationsByCategoryReloaderTask(this, getActivity());
		task.execute(this.selectedCategorieID);
	}

	public void moveListViewToTop() {
		getListView().setSelection(0);
	}
}
