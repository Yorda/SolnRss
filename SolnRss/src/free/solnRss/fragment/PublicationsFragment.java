package free.solnRss.fragment;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
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
import android.widget.Toast;
import free.solnRss.R;
import free.solnRss.activity.ReaderActivity;
import free.solnRss.activity.SolnRss;
import free.solnRss.adapter.PublicationAdapter;
import free.solnRss.fragment.listener.PublicationsFragmentListener;
import free.solnRss.provider.PublicationsProvider;
import free.solnRss.provider.SyndicationsProvider;
import free.solnRss.repository.PublicationRepository;
import free.solnRss.repository.PublicationTable;
import free.solnRss.repository.SyndicationTable;
import free.solnRss.service.PublicationsFinderService;
import free.solnRss.task.PublicationsByCategoryReloaderTask;
import free.solnRss.task.PublicationsReloaderTask;

public class PublicationsFragment extends AbstractFragment implements
		PublicationsFragmentListener {

	private PublicationRepository publicationRepository;
	private Integer selectedSyndicationID;
	private Integer nextSelectedSyndicationID; // selected by context menu
	private Integer selectedCategoryID;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle save) {
		View fragment = inflater.inflate(R.layout.fragment_publications, vg,
				false);
		listContainer = fragment.findViewById(R.id.listContainer);
		progressContainer = fragment.findViewById(R.id.progressContainer);
		emptyLayoutId = R.id.emptyPublicationsLayout;
		listShown = true;
		return fragment;
	}
	
	@Override
	public void onViewCreated(View view, Bundle save) {
		
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		
		super.onActivityCreated(savedInstanceState);
		
		restoreOrFirstDisplay(savedInstanceState);
	    publicationRepository = new PublicationRepository(getActivity());
	    
		registerForContextMenu(getListView());
		
		getListView().setTextFilterEnabled(true);
		
		((SolnRss)getActivity()).setPublicationsFragmentListener2(this);
		
		setListShown(false);
		
		// Start the service for retrieve new publications
		Intent service = new Intent(getActivity(), PublicationsFinderService.class);
		service.setAction("REGISTER_RECEIVER");
		service.putExtra("ResultReceiver", resultReceiver);
		service.putExtra("ResultReceiver_ID", hashCode());
		getActivity().startService(service);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (selectedSyndicationID != null) {
			outState.putInt("selectedSyndicationID", selectedSyndicationID);
		}
		if (selectedCategoryID != null) {
			outState.putInt("selectedCategoryID", selectedCategoryID);
		}
		if (getFilterText() != null) {
			outState.putString("filterText", getFilterText());
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
	
		Cursor cursor = ((PublicationAdapter) l.getAdapter()).getCursor();
		clickOnPublicationItem(cursor, l, v, position, id);
		
		String link = getPublicationUrl(cursor);
		String description = hasPublicationContentToDisplay(cursor);
		
	    if (description != null && description.trim().length() > 0) {

	    	if(isPreferenceToDisplayOnAppReader()){
	    		displayOnApplicationReader(description, link);
	    	}else{
	    		displayOnSystemBrowser(link);
	    	}			
		} else {
			displayOnSystemBrowser(link);
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,	ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		nextSelectedSyndicationID = null;
		
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		Cursor c = ((PublicationAdapter) getListAdapter()).getCursor();
		c.moveToPosition(info.position);
		
		menu.setHeaderTitle(c.getString(c.getColumnIndex(SyndicationTable.COLUMN_NAME)));
		
		nextSelectedSyndicationID = c.getInt(c.getColumnIndex(PublicationTable.COLUMN_SYNDICATION_ID));
		
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.publications_context, menu);

		// If a category or a syndication already selected change menu label
		if (isSyndicationOrCategorySelected()) {
			// Display all publications instead of only the syndication
			menu.getItem(0).setTitle(
					getResources().getString(R.string.display_all_publication));
			
			// If user is on a category the menu display a "mark as read" for all
			// syndication in category
			if (selectedCategoryID != null) {
				menu.getItem(1).setTitle(
						getResources().getString(
								R.string.mark_selected_category_read));
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_see_only:
			// Make a switch between display a the selected syndication and all
			// syndication.
			if (!isSyndicationOrCategorySelected()) {
				reLoadPublicationsBySyndication(nextSelectedSyndicationID);
				this.moveListViewToTop();
				
			} else {
				reloadPublications();
				this.moveListViewToTop();
			}
			break;

		case R.id.menu_mark_read:
			if (selectedCategoryID != null) {
				markCategoryPublicationsAsRead(selectedCategoryID);
			} else {
				markSyndicationPublicationsAsRead(nextSelectedSyndicationID);
			}

			break;

		default:
			break;
		}
		return super.onContextItemSelected(item);
	}
	
	private void restoreOrFirstDisplay(Bundle save) {
		
		if (save != null) {
			if(save.getInt("selectedSyndicationID") != 0 ){
				selectedSyndicationID = save.getInt("selectedSyndicationID");
			}
			else {
				selectedSyndicationID = null;
			}

			if(save.getInt("selectedCategoryID") != 0 ){
				selectedCategoryID = save.getInt("selectedCategoryID");
			}
			else {
				selectedCategoryID = null;
			}
			
			if(!TextUtils.isEmpty(save.getString("filterText"))){
				setFilterText( save.getString("filterText"));
			}else{
				setFilterText(null);
			}
		}

		if (selectedSyndicationID != null) {
			loadPublicationsBySyndication();
		} else if (selectedCategoryID != null) {
			loadPublicationsByCategory();
		} else {
			loadPublications();
		}
		
	}
	
	@Override
	protected void initAdapter() {
		final String[] from = { PublicationTable.COLUMN_TITLE,  PublicationTable.COLUMN_LINK };
		final int[] to = { android.R.id.text1, android.R.id.text2 };

		simpleCursorAdapter = new PublicationAdapter(getActivity(),R.layout.publications, null, from, to, 0);
		setListAdapter((PublicationAdapter)simpleCursorAdapter);
		((PublicationAdapter)simpleCursorAdapter).setSelectedCategoryId(selectedCategoryID);
		((PublicationAdapter)simpleCursorAdapter).setSelectedSyndicationId(selectedSyndicationID);
		
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		Uri uri = PublicationsProvider.URI;
		
		if (bundle != null) {
			if (bundle.getInt("selectedSyndicationID") != 0) {
				selectedSyndicationID = bundle.getInt("selectedSyndicationID");
				uri = Uri.parse(uri + "/" + selectedSyndicationID);
				
			} else if (bundle.getInt("selectedCategoryID") != 0) {
				selectedCategoryID = bundle.getInt("selectedCategoryID");
				uri = Uri.parse(uri + "/categoryId/" + selectedCategoryID);
			}
		}

		String selection = null;
		String[] args = null;
		if (!TextUtils.isEmpty(getFilterText())) {
			selection = PublicationTable.COLUMN_TITLE + " like ? ";
			args = new String[1];
			args[0] = "%" + getFilterText() + "%";
		}
		
		CursorLoader cursorLoader = new CursorLoader(getActivity(), uri,
				PublicationsProvider.projection, selection, args, null);
		
		return cursorLoader;
	}
	
	/*
	 * Click on a publication item. It's mark this publication as read and add
	 * one click to the syndication
	 */
	private void clickOnPublicationItem(Cursor cursor, ListView l, View v,	int position, long id) {
		
		// Set this publication as already read
		int publicationId = cursor.getInt(cursor
				.getColumnIndex(PublicationTable.COLUMN_ID));

		markPublicationAsRead(publicationId);

		// Add a click to the syndication
		int syndicationId = cursor.getInt(cursor
				.getColumnIndex(PublicationTable.COLUMN_SYNDICATION_ID));
		addOneClickToSyndication(syndicationId);
	}
	
	private void markPublicationAsRead(int publicationId) {
		ContentValues values = new ContentValues();
		values.put(PublicationTable.COLUMN_ALREADY_READ, "1");
		Uri uri = Uri.parse(PublicationsProvider.URI + "/publicationId/" + publicationId);
		getActivity().getContentResolver().update(uri, values, null, null);
		
		refreshPublicationsAfterMarkAsRead(getActivity());
	}
	
	private void addOneClickToSyndication(int syndicationId){
		Uri uri = Uri.parse(SyndicationsProvider.URI + "/click/" + syndicationId);
		ContentValues values = new ContentValues();
		getActivity().getContentResolver().update(uri, values, null, null);
	}
	
	/*
	 * Refresh publication after click on item list for hide it.
	 * Use async task instead of content provider, because when calling intent for open browser
	 * the loader manger don't refresh list view. why ????
	 */
	private void refreshPublicationsAfterMarkAsRead(Context context) {

		if (this.selectedSyndicationID != null) {
			PublicationsReloaderTask task = new PublicationsReloaderTask(
					this, context);
			task.execute(this.selectedSyndicationID);

		} else if (this.selectedCategoryID != null) {
			PublicationsByCategoryReloaderTask task = new PublicationsByCategoryReloaderTask(
					this, context);
			task.execute(this.selectedCategoryID);

		} else {
			PublicationsReloaderTask task = new PublicationsReloaderTask(
					this, context);
			task.execute(selectedSyndicationID);
		}
	}
	
	
	public void moveListViewToTop() {
		getListView().setSelection(0);
	}

	public void filterPublications(String text) {
		makeFilterInListView(text);
	}
	
	public void loadPublications() {
		getLoaderManager().initLoader(0, null, this);
	}

	public void reloadPublications() {
		this.selectedSyndicationID = null;
		this.selectedCategoryID = null;
		if (isAdded()) {
			getLoaderManager().restartLoader(0, null, this);
		}
	}

	public void refreshPublications() {
		if (this.selectedSyndicationID != null) {
			reLoadPublicationsBySyndication(this.selectedSyndicationID);
		} else if (this.selectedCategoryID != null) {
			reLoadPublicationsByCategory(this.selectedCategoryID);
		} else {
			reloadPublications();
		}
	}

	private void loadPublicationsByCategory() {
		Bundle bundle = new Bundle();
		bundle.putInt("selectedCategoryID",selectedCategoryID);
		getLoaderManager().initLoader(0, bundle, this);
	}
	
	public void reLoadPublicationsByCategory(Integer categoryID) {
		this.selectedSyndicationID = null;
		this.selectedCategoryID = categoryID;
		Bundle bundle = new Bundle();
		bundle.putInt("selectedCategoryID",this.selectedCategoryID);
		if(isAdded()){
			getLoaderManager().restartLoader(0, bundle, this);
		}
	}

	private void loadPublicationsBySyndication() {
		Bundle bundle = new Bundle();
		bundle.putInt("selectedSyndicationID", selectedSyndicationID);
		getLoaderManager().initLoader(0, bundle, this);
	}
	
	public void reLoadPublicationsBySyndication(Integer syndicationID) {
		this.selectedSyndicationID = syndicationID;
		this.selectedCategoryID = null;
		Bundle bundle = new Bundle();
		bundle.putInt("selectedSyndicationID", this.selectedSyndicationID);
		if (isAdded()) {
			getLoaderManager().restartLoader(0, bundle, this);
		}
	}
	
	private ResultReceiver resultReceiver = new ResultReceiver(new Handler()) {
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			
		};
	};
	
	public String getPublicationUrl(Cursor cursor) {
		return cursor.getString(cursor
				.getColumnIndex(PublicationTable.COLUMN_LINK));
	}

	public String hasPublicationContentToDisplay(Cursor cursor) {
		return cursor.getString(cursor
				.getColumnIndex(PublicationTable.COLUMN_PUBLICATION));
	}

	private boolean isPreferenceToDisplayOnAppReader() {
		return PreferenceManager.getDefaultSharedPreferences(getActivity())
				.getBoolean("pref_display_publication", true);
	}

	private void displayOnApplicationReader(String text, String link) {
		Intent i = new Intent(getActivity(), ReaderActivity.class);
		i.putExtra("read", text);
		i.putExtra("link", link);
		startActivity(i);
	}

	private void displayOnSystemBrowser(String url) {
		// Start a browser
		Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		try {
			startActivity(openUrlIntent);
		} catch (Exception e) {
			Toast.makeText(
					getActivity(),
					getActivity().getResources().getString(
							R.string.open_browser_bad_url), Toast.LENGTH_LONG)
					.show();
		}
	}

	private boolean isSyndicationOrCategorySelected() {
		if (selectedSyndicationID != null || selectedCategoryID != null) {
			return true;
		}
		return false;
	}
	
	private void markSyndicationPublicationsAsRead(final Integer id) {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				// TODO USE PROVIDER
				publicationRepository.markSyndicationPublicationsAsRead(id);
				return null;
			}
			protected void onPostExecute(Void result) {
				refreshPublications();
			};
		}.execute();
	}
	
	private void markCategoryPublicationsAsRead(final Integer id) {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				// TODO USE PROVIDER
				publicationRepository.markCategoryPublicationsAsRead(id);
				return null;
			}
			protected void onPostExecute(Void result) {
				refreshPublications();
			};
		}.execute();
		
	}

}
