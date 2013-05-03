package free.solnRss.fragment;

import android.app.Activity;
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
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.LinearLayout;
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
import free.solnRss.service.PublicationsFinderService;
import free.solnRss.task.PublicationsByCategoryReloaderTask;
import free.solnRss.task.PublicationsReloaderTask;

public class PublicationsFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<Cursor>, PublicationsFragmentListener {

	//Integer index = null;
	//Integer top   = null;
	
	private ResultReceiver resultReceiver = new ResultReceiver(new Handler()) {
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			/*
			Log.e(PublicationsFragment.class.getName(), "Receive some change");
			getListView().setScrollbarFadingEnabled(false);
			index = getListView().getFirstVisiblePosition()
					+ resultData.getInt("newPublicationsNumber");
			View v = getListView().getChildAt(0);
			top = (v == null) ? 0 : v.getTop();
			refreshPublications(getActivity());
			*/
		};
	};
		
	private PublicationAdapter publicationAdapter;
	
	final private String[] from = { 
			"pub_title", 
			"pub_link" 
		};
	
	final private int[] to = { 
			android.R.id.text1, 
			android.R.id.text2 
		};
	
	private void initAdapter() {		
		publicationAdapter = new PublicationAdapter(getActivity(),R.layout.publications, null, from, to, 0);
		setListAdapter(publicationAdapter);
		publicationAdapter.setSelectedCategoryId(selectedCategorieID);
		publicationAdapter.setSelectedSyndicationId(selectedSyndicationID);
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {		
		
		Uri uri = PublicationsProvider.URI;
		if(bundle != null){
			if ( bundle.getInt("selectedSyndicationID") != 0) {
				selectedSyndicationID = bundle.getInt("selectedSyndicationID");
				uri = Uri.parse(PublicationsProvider.URI + "/" + selectedSyndicationID);
				
			} 
			else if (bundle.getInt("selectedCategorieID") != 0) {
				selectedCategorieID = bundle.getInt("selectedCategorieID");
				uri = Uri.parse(PublicationsProvider.URI + "/categoryId/" + selectedCategorieID);
			}
		}

		String selection = null;
		String[] args = null;
		if (!TextUtils.isEmpty(filterText)) {
			selection = PublicationTable.COLUMN_TITLE + " like ? ";
			args = new String[1];
			args[0] = "%" + filterText + "%";
		}
		
		CursorLoader cursorLoader = new CursorLoader(getActivity(), uri,
				PublicationsProvider.projection, selection, args, null);
		
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		
		if(publicationAdapter == null){
			initAdapter();
		}
		
		// The list should now be shown.
		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}
		
		publicationAdapter.swapCursor(arg1);
		
		if (getListAdapter().isEmpty()) {
			displayEmptyMessage();
		}
		else{
			 hideEmptyMessage();
		}
		
		/*if(index != null){
			getListView().setSelectionFromTop(index, top);
			index = null;
		}*/
	}

	private void displayEmptyMessage() {
		LinearLayout l = (LinearLayout) getActivity().findViewById(R.id.emptyPublicationsLayout);
		l.setVisibility(View.VISIBLE);
	}

	private void hideEmptyMessage() {
		LinearLayout l = (LinearLayout) getActivity().findViewById(R.id.emptyPublicationsLayout);
		l.setVisibility(View.INVISIBLE);
	}
	
	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		if(publicationAdapter == null){
			initAdapter();
		}
		publicationAdapter.swapCursor(null);
	}
	
	private void clickOnPublicationItem(Cursor cursor, ListView l, View v,
			int position, long id) {
		
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

		} else if (this.selectedCategorieID != null) {
			PublicationsByCategoryReloaderTask task = new PublicationsByCategoryReloaderTask(
					this, context);
			task.execute(this.selectedCategorieID);

		} else {
			PublicationsReloaderTask task = new PublicationsReloaderTask(
					this, context);
			task.execute(selectedSyndicationID);
		}
	}
	
	private PublicationRepository publicationRepository;
	private Integer selectedSyndicationID;
	private Integer nextSelectedSyndicationID; // selected by context menu
	private Integer selectedCategorieID;
	private String filterText;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle save) {
		View fragment     = inflater.inflate(R.layout.fragment_publications, vg, false);
		listContainer     = fragment.findViewById(R.id.listContainer);
		progressContainer = fragment.findViewById(R.id.progressContainer);
		listShown = true;
		return fragment;
	}

	public void restoreOrFirstDisplay(Bundle save) {
		Log.e(PublicationsFragment.this.getClass().getName(), "BEGIN TO CREATE OR RESTORE ");

		if (save != null) {
			selectedSyndicationID = save.getInt("selectedSyndicationID") == 0 
					? null : save.getInt("selectedSyndicationID");

			selectedCategorieID = save.getInt("selectedCategorieID") == 0 
					? null : save.getInt("selectedCategorieID");
			
			filterText = TextUtils.isEmpty(save.getString("filterText")) 
					? null : save.getString("filterText");
		}

		Log.e(PublicationsFragment.this.getClass().getName(),"Restore with Sid-> " + selectedSyndicationID + " cId-> " + selectedCategorieID);

		if (selectedSyndicationID != null) {
			loadPublicationsBySyndication(selectedSyndicationID);
		} else if (selectedCategorieID != null) {
			loadPublicationsByCategorie(selectedCategorieID);
		} else {
			loadPublications(getActivity());
		}
		
		if (((SolnRss) getActivity()).getFilterText() != null) {
			getListView().setFilterText(((SolnRss) getActivity()).getFilterText());
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
		if (filterText != null) {
			outState.putString("filterText", filterText);
		}
		
		Log.e(PublicationsFragment.this.getClass().getName(),"Save sId-> " + selectedSyndicationID	+ " cId-> " + selectedCategorieID);
	}
	
	@Override
	public void onViewCreated(View view, Bundle save) {
		restoreOrFirstDisplay(save);
	    publicationRepository = new PublicationRepository(getActivity());
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registerForContextMenu(getListView());
		
		getListView().setTextFilterEnabled(true);
		((SolnRss)getActivity()).setPublicationsFragmentListener(this);
		
		setListShown(false);
		
		// Start the service for retrieve new publications
		Intent service = new Intent(getActivity(), PublicationsFinderService.class);
		service.setAction("REGISTER_RECEIVER");
		service.putExtra("ResultReceiver", resultReceiver);
		service.putExtra("ResultReceiver_ID", hashCode());
		getActivity().startService(service);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
	
		Cursor cursor = ((PublicationAdapter) l.getAdapter()).getCursor();
		clickOnPublicationItem(cursor, l, v, position, id);
		
		String link = getClickedUrl(cursor);
		
		String description = hasDescriptionValue(cursor);
		
	    if (description != null && description.trim().length() > 0) {
			// TODO improve this
	    	if(displayPublicationOnApp()){
	    		openReaderActivity(description, link);
	    	}else{
	    		openBroswser(link);
	    	}			
		} else {
			openBroswser(link);
		}
	    
		//openBroswser(link);
	}
	
	private boolean displayPublicationOnApp() {
		return PreferenceManager.getDefaultSharedPreferences(getActivity())
					.getBoolean("pref_display_publication", true);
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
			
		}
		if (selectedSyndicationID != null) {
			menu.getItem(1).setTitle(
					getResources().getString(R.string.mark_selected_syndication_read));

		} else if (selectedCategorieID != null) {
			menu.getItem(1).setTitle(
					getResources().getString(R.string.mark_selected_category_read));
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_see_only:
			// Make a switch between display a the selected syndication and all
			// syndication.
			if (!isSyndicationOrCategorieSelected()) {
				selectedSyndicationID = nextSelectedSyndicationID;
				reLoadPublicationsBySyndication(getActivity(),
						this.selectedSyndicationID);
				this.moveListViewToTop();
			} else {
				reloadPublications(getActivity());
				this.moveListViewToTop();
			}
			break;

		case R.id.menu_mark_read:

			if (selectedSyndicationID != null && selectedCategorieID == null) {
				markSyndicationPublicationsAsRead(selectedSyndicationID);
			} else if (selectedCategorieID != null
					&& selectedSyndicationID == null) {
				markCategoryPublicationsAsRead(selectedCategorieID);
			} else {
				markAllPublicationsAsRead();
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
			protected void onPostExecute(Void result) {
				reloadPublications(getActivity());
			};
		}.execute();		
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
			protected void onPostExecute(Void result) {
				refreshPublications(getActivity());
			};
		}.execute();
	}
	
	private void markCategoryPublicationsAsRead(final Integer id) {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				// TODO must work with all syndication in a categories
				publicationRepository.markCategoryPublicationsAsRead(id);
				return null;
			}
			protected void onPostExecute(Void result) {
				refreshPublications(getActivity());
			};
		}.execute();
		
	}
	
	/**
	 * 
	 * @param url
	 */
	private void openBroswser(String url) {
		// Start a browser
		Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		try {
			startActivity(openUrlIntent);
		} catch (Exception e) {
			Toast.makeText(getActivity(), getActivity().getResources().getString(
					R.string.open_browser_bad_url), Toast.LENGTH_LONG).show();
		}
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
	 * 
	 * @param cursor
	 * @return
	 */
	public String hasDescriptionValue(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndex(PublicationTable.COLUMN_PUBLICATION));
	}

	@Override
	public void loadPublications(Context context) {
		this.selectedSyndicationID = null;
		this.selectedCategorieID = null;
		getLoaderManager().initLoader(0, null, this);
	}
	
	@Override
	public void reloadPublications(Context context) {
		this.selectedSyndicationID = null;
		this.selectedCategorieID = null;
		if (isAdded()) {
			getLoaderManager().restartLoader(0, null, this);
		}
	}
	
	private void loadPublicationsByCategorie(Integer selectedCategoryId) {
		this.selectedSyndicationID = null;		
		this.selectedCategorieID = selectedCategoryId;
		Bundle bundle = new Bundle();
		bundle.putInt("selectedCategorieID",selectedCategoryId);
		getLoaderManager().restartLoader(0, bundle, this);
	}
	
	@Override
	public void reLoadPublicationsByCategorie(Context context,	Integer selectedCategoryId) {
		this.selectedSyndicationID = null;
		this.selectedCategorieID = selectedCategoryId;
		Bundle bundle = new Bundle();
		bundle.putInt("selectedCategorieID",this.selectedCategorieID);
		getLoaderManager().restartLoader(0, bundle, this);
	}

	private void loadPublicationsBySyndication(Integer selectedSyndicationID) {
		this.selectedCategorieID = null;	
		this.selectedSyndicationID = selectedSyndicationID;
		Bundle bundle = new Bundle();
		bundle.putInt("selectedSyndicationID", selectedSyndicationID);
		getLoaderManager().restartLoader(0, bundle, this);
	}
	
	@Override
	public void reLoadPublicationsBySyndication(Context context, Integer selectedSyndicationID) {
		this.selectedSyndicationID = selectedSyndicationID;
		this.selectedCategorieID = null;
		Bundle bundle = new Bundle();
		bundle.putInt("selectedSyndicationID", this.selectedSyndicationID);
		if (isAdded()) {
			getLoaderManager().restartLoader(0, bundle, this);
		}
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void refreshPublications(Context context) {
		if (this.selectedSyndicationID != null) {
			reLoadPublicationsBySyndication(context, this.selectedSyndicationID);
		} else if (this.selectedCategorieID != null) {
			reLoadPublicationsByCategorie(context, this.selectedCategorieID);
		} else {
			reloadPublications(context);
		}
	}
	
	@Override
	public void moveListViewToTop() {
		getListView().setSelection(0);
	}
	
	@Override
	public void filterPublications(String text) {
		if (this.getListView() != null) {
			if (TextUtils.isEmpty(text)) {
				setFilterText(null);
				this.getListView().clearTextFilter();
			} else {
				setFilterText(text); 
				this.getListView().setFilterText(text);
			}
		}
	}

	public String getFilterText() {
		return filterText;
	}

	public void setFilterText(String filterText) {
		this.filterText = filterText;
	}
	
	private boolean listShown;
	private View progressContainer;
 	private View listContainer;
 	
	public void setListShown(boolean shown, boolean animate){		
	    if (listShown == shown) {
	        return;
	    }
	    listShown = shown;
	    if (shown) {
	        if (animate) {
	            progressContainer.startAnimation(AnimationUtils.loadAnimation(
	                    getActivity(), android.R.anim.fade_out));
	            listContainer.startAnimation(AnimationUtils.loadAnimation(
	                    getActivity(), android.R.anim.fade_in));
	        }
	        progressContainer.setVisibility(View.GONE);
	        listContainer.setVisibility(View.VISIBLE);
	    } else {
	        if (animate) {
	            progressContainer.startAnimation(AnimationUtils.loadAnimation(
	                    getActivity(), android.R.anim.fade_in));
	            listContainer.startAnimation(AnimationUtils.loadAnimation(
	                    getActivity(), android.R.anim.fade_out));
	        }
	        progressContainer.setVisibility(View.VISIBLE);
	        listContainer.setVisibility(View.INVISIBLE);
	    }
	}
	public void setListShown(boolean shown){
	    setListShown(shown, true);
	}
	public void setListShownNoAnimation(boolean shown) {
	    setListShown(shown, false);
	}
}
