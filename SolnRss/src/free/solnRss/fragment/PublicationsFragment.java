package free.solnRss.fragment;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import de.greenrobot.event.EventBus;
import free.solnRss.R;
import free.solnRss.activity.ReaderActivity;
import free.solnRss.activity.SolnRss;
import free.solnRss.adapter.PublicationAdapter;
import free.solnRss.event.ChangePublicationListStateEvent;
import free.solnRss.fragment.listener.PublicationsFragmentListener;
import free.solnRss.repository.PublicationContentRepository;
import free.solnRss.repository.PublicationRepository;
import free.solnRss.repository.PublicationTable;
import free.solnRss.repository.SyndicationTable;
import free.solnRss.service.CleanOlderDataService;
import free.solnRss.singleton.TypeFaceSingleton;
import free.solnRss.state.InstancePublicationListState;
import free.solnRss.state.PublicationsListState;
import free.solnRss.utility.Constants;

public class PublicationsFragment extends AbstractFragment implements
		PublicationsFragmentListener, SharedPreferences.OnSharedPreferenceChangeListener {
	
	private PublicationRepository publicationRepository;
	private PublicationContentRepository publicationContentRepository;
	private String dateNewPublicationsFound;  
	private Integer selectedSyndicationID;
	private Integer nextSelectedSyndicationID; // Selected by context menu
	private Integer selectedCategoryID;
	private Boolean selectFavoritePublications = false;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle save) {
		View fragment = inflater.inflate(R.layout.fragment_publications, vg, false);
		listContainer = fragment.findViewById(R.id.listContainer);
		progressContainer = fragment.findViewById(R.id.progressContainer);
		emptyLayoutId = R.id.emptyPublicationsLayout;
		listShown = true;
		
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		
		return fragment;
	}

	PublicationsListState publicationsListState;
	
	public void onEvent(ChangePublicationListStateEvent event) {
		// Log.e("EVENT", "A change list state event received");
		this.publicationsListState = event.getState();
		/*if (getLoaderManager() != null && getLoaderManager().getLoader(0) != null 
				&& getLoaderManager().getLoader(0).isStarted()) {
			getLoaderManager().restartLoader(0, null, this);
			updateActionBarTitle();
		}
		else {
			getLoaderManager().initLoader(0, null, this);
			updateActionBarTitle();
		}*/
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		publicationRepository = new PublicationRepository(getActivity());
		publicationContentRepository = new PublicationContentRepository(getActivity());
		displayList(savedInstanceState);
		
		registerForContextMenu(getListView());
		
		getListView().setTextFilterEnabled(true);
		
		((SolnRss)getActivity()).setPublicationsFragmentListener(this);
		
		setListShown(false);
		
		setHasOptionsMenu(true);
	
		//PublicationFinderBusinessImpl finder = new PublicationFinderBusinessImpl(getActivity());
		//finder.searchNewPublications();
		
		// NewPublicationsNotification notify = new NewPublicationsNotification(getActivity());
		// DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRENCH);
		// notify.notificationForNewPublications(25, "2014-01-31 09:30:00");
		
		//NewPublicationsNotification notify = new NewPublicationsNotification(getActivity());
		//notify.notificationForNewPublications(25, "2014-05-21 14:45:00");
		
		// addNumberOfLastFoundInMenu(25);	
		
		//ImageLoaderUtil imageLoaderUtil = new ImageLoaderUtil(getActivity());
		//imageLoaderUtil.saveImages(description);
		
	}
/*
	String description = "<p>Si vous avez un PC Windows que vous utilisez pour tout faire, y compris écouter de la musique, voici une petite application qui va vous changer la vie. Il s'agit de <strong>Stream What You Hear</strong>, un freeware disponible ici et développé par le français Sébastien Warin, qui permet de streamer le son de son PC sur les appareils compatibles Upnp / Dlna.</p>\r\n" + 
		"<p>Vous allez pouvoir balancer le son de Spotify, de vos playlists Youtube ou de votre Soundcloud sur les appareils XBMC, PS3, Xbox 360, Freebox, Bbox mais aussi toutes les TV et appareils connectés et supportant le DLNA et l'Upnp. Pratique non ?</p>\r\n" + 
		"<p style=\"text-align: center;\"><a href=\"http://korben.info/wp-content/uploads/2014/08/swyh-1.21.png\"><img class=\"aligncenter size-full wp-image-54262\" src=\"http://korben.info/wp-content/uploads/2014/08/swyh-1.21.png\" alt=\"swyh 1.21 Un logiciel pour balancer le son de votre ordinateur sur vos appareils compatibles DLNA / UPNP\" width=\"391\" height=\"185\" title=\"Un logiciel pour balancer le son de votre ordinateur sur vos appareils compatibles DLNA / UPNP\" /></a></p>\r\n" + 
		"<p style=\"text-align: left;\">En plus de ça, comme SWYH est capable de récupérer le flux audio sortant de votre ordinateur, vous pouvez même réaliser des enregistrements MP3 du son.</p>\r\n" + 
		"<p style=\"text-align: left;\">Si ça vous branche, et que vous voulez faire péter les watts dans toute la baraque, <a href=\"http://www.streamwhatyouhear.com/download/\">c'est par ici que ça se passe</a>.</p>\r\n" + 
		"<p>Mille mercis à Aurélien qui assure grave avec ce partage !</p>\r\n" + 
		"<p>Cet article merveilleux et sans aucun égal intitulé : <a rel=\"nofollow\" href=\"http://korben.info/dlna-upnp-serveur-windows.html\">Un logiciel pour balancer le son de votre ordinateur sur vos appareils compatibles DLNA / UPNP</a> ; a été publié sur <a rel=\"nofollow\" href=\"http://korben.info\">Korben</a>, le seul site qui t'aime plus fort que tes parents.</p>\r\n" + 
		"";*/

	private void displayList(Bundle save) {
		
		
		SharedPreferences sharedPreferences = getActivity().getPreferences(0);

		// Get the publication lis state
		String savedPublicationListState = sharedPreferences.getString("publication list state", null);
		if(savedPublicationListState != null) {
			
		}
		
		if (sharedPreferences.getInt("selectedSyndicationID", -1) != -1) {
			selectedSyndicationID = sharedPreferences.getInt("selectedSyndicationID", -1);
			loadPublicationsBySyndication();
		}

		else if (sharedPreferences.getInt("selectedCategoryID", -1) != -1) {
			selectedCategoryID = sharedPreferences.getInt("selectedCategoryID", -1);
			loadPublicationsByCategory();
		}

		else if (sharedPreferences.getString("dateNewPublicationsFound", null) != null) {
			dateNewPublicationsFound = sharedPreferences.getString("dateNewPublicationsFound", null);
			loadPublicationsByLastFound(dateNewPublicationsFound);
		}
		
		else if (sharedPreferences.getBoolean("displayFavoritePublications", false)) {
			displayFavoritePublications();
		}
		else {
			loadPublications();
		}
		setFilterText(sharedPreferences.getString("filterText", null));
	}

	@Override
	protected void displayEmptyMessage() {
		
		LinearLayout l = (LinearLayout) getActivity().findViewById(emptyLayoutId);
		l.setVisibility(View.VISIBLE);
		
		getActivity().findViewById(R.id.displayAllButton).setVisibility(View.VISIBLE);
		
		if (selectedSyndicationID != null) {
			// Selected by syndication are empty
			writeEmptyMessage(syndicationName(), R.string.empty_publications_with_syndication,
					R.string.empty_publications_with_syndication_with_filter, getFilterText());

		}
		else if (selectedCategoryID != null) {
			// Selected by category is empty
			writeEmptyMessage(categoryName(), R.string.empty_publications_with_category,
					R.string.empty_publications_with_category_with_filter, getFilterText());
		}
		
		else if (dateNewPublicationsFound != null) {
			// Selected by last date found
			writeEmptyMessage(null, R.string.empty_publications_date_last_found,
					R.string.empty_publications_date_last_found, getFilterText());
		}
		else {
			
			// All publication
			writeEmptyMessage(null, R.string.empty_publications,
					R.string.empty_publications_with_filter, getFilterText());
			
			getActivity().findViewById(R.id.displayAllButton).setVisibility(View.GONE);
		}
		
		// If already read publications are hidden display button for display them
		displayAlreadyReadPublicationsButton();
	}
	
	private void writeEmptyMessage(String name, int idMsgWithoutFilter,
			int idMsgWithFilter, String filter) {
		if (TextUtils.isEmpty(filter)) {
			if (name == null)
				displayEmptyListView(getResources().getString(idMsgWithoutFilter));
			else
				displayEmptyListView(getResources().getString(idMsgWithoutFilter, name));
		} else {
			if (name == null)
				displayEmptyListView(getResources().getString(idMsgWithFilter, filter));
			else
				displayEmptyListView(getResources().getString(idMsgWithFilter, name,	filter));
		}
	}
	
	private void displayEmptyListView(String msg) {
		
		TextView tv = (TextView)getActivity().findViewById(R.id.emptyPublicationsMessage);
		
		Typeface userTypeFace = TypeFaceSingleton.getInstance(getActivity()).getUserTypeFace();
		if (userTypeFace != null) {
			tv.setTypeface(userTypeFace);
		}
		
		int userFontSize = TypeFaceSingleton.getInstance(getActivity()).getUserFontSize();
		if (userFontSize != Constants.FONT_SIZE) {
			tv.setTextSize(userFontSize);
		}
		
		tv.setText(msg);
		
		Button displayAllButton =  (Button)getActivity().findViewById(R.id.displayAllButton);
		if (userTypeFace != null) {
			displayAllButton.setTypeface(userTypeFace);
		}
		if (userFontSize != Constants.FONT_SIZE) {
			displayAllButton.setTextSize(userFontSize);
		}
		
		Button displayAlreadyReadButton =  (Button)getActivity().findViewById(R.id.displayAlreadyReadButton);
		if (userTypeFace != null) {
			displayAlreadyReadButton.setTypeface(userTypeFace);
		}
		if (userFontSize != Constants.FONT_SIZE) {
			displayAlreadyReadButton.setTextSize(userFontSize);
		}
	}
	
	private void displayAlreadyReadPublicationsButton() {
		
		final int viewId = R.id.displayAlreadyReadButton;
		
		if (displayAlreadyReadPublications()) {
			getActivity().findViewById(viewId).setVisibility(View.INVISIBLE);

		} else {
			getActivity().findViewById(viewId).setVisibility(View.VISIBLE);
		}
	}

	private String categoryName() {
		return categoryName(selectedCategoryID);
	}

	private String syndicationName() {
		return syndicationName(selectedSyndicationID);
	}
	
	private String dataNewPublicationsFoundLabel() {
		return getResources().getString(R.string.last_publications);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		
		Cursor cursor = ((PublicationAdapter) l.getAdapter()).getCursor();
		
		if (position == cursor.getCount()) {
			return;
		}
		
		final Integer syndicationId = cursor.getInt(cursor.getColumnIndex(PublicationTable.COLUMN_SYNDICATION_ID));
		final Integer publicationId = cursor.getInt(0);
		final boolean isFavorite = cursor.getInt(6) != 1 ? false : true;
				
		String[] publicationContent = publicationContentRepository
				.retrievePublicationContent(syndicationId, publicationId);
		
		String title = cursor.getString(1);         //getPublicationTitle(cursor);
		String link = publicationContent[0];        //getPublicationUrl(cursor);
		String description = publicationContent[1]; //hasPublicationContentToDisplay(cursor);
		String synsdicationName =  cursor.getString(3);
		
		clickOnPublicationItem(cursor, l, v, position, id);
		
		if (description != null && description.trim().length() > 0) {
			if (isPreferenceToDisplayOnAppReader()) {
				//displayOnApplicationReader(description, link, title);
				displayOnApplicationReader(publicationId, syndicationId,synsdicationName, isFavorite, description, link, title);
			} else {
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
		
		menu.setHeaderTitle(c.getString(c
				.getColumnIndex(SyndicationTable.COLUMN_NAME)));

		nextSelectedSyndicationID = c.getInt(c
				.getColumnIndex(PublicationTable.COLUMN_SYNDICATION_ID));

		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.publications_context, menu);
		
		if (selectedSyndicationID != null) {
			menu.getItem(0).setTitle(getResources().getString(R.string.display_all_publication));
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_see_only:
			if(selectedSyndicationID == null){
				reLoadPublicationsBySyndication(nextSelectedSyndicationID);
				this.moveListViewToTop();
				
			} else {
				reloadPublications();
				this.moveListViewToTop();
			}
			break;

		case R.id.menu_mark_read:
			confirmMarkSyndicationPublicationsAsRead(nextSelectedSyndicationID);
			break;
			
		default:
			break;
		}
		return super.onContextItemSelected(item);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		SharedPreferences sharedPreferences = getActivity().getPreferences(0);
		InstancePublicationListState instancePublicationListState = new InstancePublicationListState();
		publicationsListState = 
				instancePublicationListState.restorePublicationListState(getActivity(), sharedPreferences);
		
		EventBus.getDefault().registerSticky(this);
	}
	
	@Override public void onPause() {
		
		super.onPause();	
		
		
		SharedPreferences.Editor editor = getActivity().getPreferences(0).edit();
		editor.putInt("selectedSyndicationID", -1);
		editor.putInt("selectedCategoryID", -1);
		editor.putString("dateNewPublicationsFound", null);
		editor.putBoolean("displayFavoritePublications", false);
		editor.putString("filterText", null);
		
		if (selectedSyndicationID != null) {
			editor.putInt("selectedSyndicationID", selectedSyndicationID);
		}
		if (selectedCategoryID != null) {
			editor.putInt("selectedCategoryID", selectedCategoryID);
		}
		if (dateNewPublicationsFound != null) {
			editor.putString("dateNewPublicationsFound", dateNewPublicationsFound);
		}
		if (selectFavoritePublications == true) {
			editor.putBoolean("displayFavoritePublications", true);
		}
		if (!TextUtils.isEmpty(getFilterText())) {
			editor.putString("filterText", getFilterText());
		}
		
		int index = getListView().getFirstVisiblePosition();
		editor.putInt("publicationsListViewIndex", index);

		View v = getListView().getChildAt(0);
		int position = (v == null) ? 0 : v.getTop();
		editor.putInt("publicationsListViewPosition", position);
		editor.commit();
		

		publicationsListState.setPositionOnList(position, index);
		publicationsListState.setFilterText(getFilterText() == null ? "" : getFilterText());
		InstancePublicationListState instancePublicationListState = new InstancePublicationListState();
		instancePublicationListState.savePublicationListState(getActivity().getPreferences(0), publicationsListState);
		
		EventBus.getDefault().unregister(this);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	

	@Override
	protected void setListPositionOnScreen() {
		
		SharedPreferences prefs = getActivity().getPreferences(0);
		int index = prefs.getInt("publicationsListViewIndex", -1);
		int position = prefs.getInt("publicationsListViewPosition", -1);
		int cursorCount = prefs.getInt("cursorCount", -1);

		SharedPreferences.Editor editor = prefs.edit();
		
		if (cursorCount != -1) {
			
			// Get differece between old and new cursor
			int newCursorCount = 
				((PublicationAdapter) getListAdapter()).getCursor().getCount() - cursorCount;
			if (newCursorCount > 0) {
				index = index + newCursorCount;
			}
			
			getListView().setSelectionFromTop(index, position); 
			editor.putInt("cursorCount", -1);
			editor.putInt("publicationsListViewIndex", -1);
			editor.putInt("publicationsListViewPosition", -1);
			editor.commit();
		}
		else if (index != -1) {
			
			// Set list view at position
			getListView().setSelectionFromTop(index, position);
			// Reset position save
			editor.putInt("cursorCount", -1);
			editor.putInt("publicationsListViewIndex", -1);
			editor.putInt("publicationsListViewPosition", -1);
			editor.commit();
		}
	}
	
	@Override
	protected void initAdapter() {				
		final String[] from = { 
				PublicationTable.COLUMN_TITLE,  
				PublicationTable.COLUMN_TITLE 
			};
		final int[] to = { 
				android.R.id.text1, 
				android.R.id.text2 
			};
		
		simpleCursorAdapter = new PublicationAdapter(getActivity(),	R.layout.publications, null, from, to, 0);
		setListAdapter((PublicationAdapter)simpleCursorAdapter);		
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		
		getActivity().setProgressBarIndeterminateVisibility(true);
		
		if (bundle != null) {
			if (bundle.getInt("selectedSyndicationID") != 0) {
				selectedSyndicationID = bundle.getInt("selectedSyndicationID");				
			} else if (bundle.getInt("selectedCategoryID") != 0) {
				selectedCategoryID = bundle.getInt("selectedCategoryID");
			}
			else if (bundle.getString("dateNewPublicationsFound") != null) {
				dateNewPublicationsFound = bundle.getString("dateNewPublicationsFound");
			}
			else if (bundle.getBoolean("displayFavoritePublications") == true) {
				// selectFavoritePublications = true;
				return publicationRepository.loadBookmarkedPublications(getFilterText());
			}
		}
		
		return publicationRepository.loadPublications(getFilterText(),
				selectedSyndicationID, selectedCategoryID, dateNewPublicationsFound,
				displayAlreadyReadPublications());
		
		// return state.displayList();
	}
	
	private boolean displayAlreadyReadPublications() {
		return PreferenceManager.getDefaultSharedPreferences(getActivity())
					.getBoolean("pref_display_unread", true);
	}
	
	@Override
	protected void queryTheTextChange() {
		Bundle bundle = new Bundle();
		
		if (selectedSyndicationID != null) {
			bundle.putInt("selectedSyndicationID", selectedSyndicationID);
		}
		
		if (selectedCategoryID != null) {
			bundle.putInt("selectedCategoryID", selectedCategoryID);
		}
		
		if (dateNewPublicationsFound != null) {
			bundle.putString("dateNewPublicationsFound", dateNewPublicationsFound);
		}
		
		if (selectFavoritePublications != null && selectFavoritePublications) {
			bundle.putBoolean("displayFavoritePublications", true);
		}
		
		getLoaderManager().restartLoader(0, bundle, this);
	}
	
	/*
	 * Click on a publication item. It's mark this publication as read and add
	 * one click to the syndication
	 */
	private void clickOnPublicationItem(final Cursor cursor, ListView l, View v,	int position, long id) {
		
		new Runnable() {
			public void run() {
				try {
					publicationRepository.markOnePublicationAsReadByUser(cursor.getInt(0), 
							cursor.getInt(cursor.getColumnIndex(PublicationTable.COLUMN_SYNDICATION_ID)),
							cursor.getInt(cursor.getColumnIndex(SyndicationTable.COLUMN_NUMBER_CLICK)));
					((PublicationAdapter)getListAdapter()).notifyDataSetChanged();
					refreshPublications();
					((SolnRss) getActivity()).refreshSyndications();
					
				} catch (Exception e) {
					Toast.makeText(getActivity(),
							"Error unable to set this publication as read",
							Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}
			}
		}.run();
	}
	
	public void moveListViewToTop() {
		getListView().setSelection(0);
	}
	
	public void loadPublications() {
		getLoaderManager().initLoader(0, null, this);
	}

	public void reloadPublications() {
		this.selectedSyndicationID = null;
		this.selectedCategoryID = null;
		this.dateNewPublicationsFound = null;
		this.selectFavoritePublications = false;
		
		if (isAdded()) {
			getLoaderManager().restartLoader(0, null, this);
			updateActionBarTitle();
		}
	}

	@Override
	public void reloadPublicationsWithAlreadyRead() {
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
		editor.putBoolean("pref_display_unread", true);
		editor.commit();
	}
	
	// Refresh the list after event like a syndication delete.
	public void refreshPublications() {
		if (this.selectedSyndicationID != null) {
			reLoadPublicationsBySyndication(this.selectedSyndicationID);
		} else if (this.selectedCategoryID != null) {
			reLoadPublicationsByCategory(this.selectedCategoryID);
		} else if (this.dateNewPublicationsFound != null) {
			reLoadPublicationsByLastFound(dateNewPublicationsFound);
		} else if (this.selectFavoritePublications == true) {
			displayFavoritePublications();
		}
		else {
			reloadPublications();
		}
	}

	private void loadPublicationsByCategory() {
		Bundle bundle = new Bundle();
		bundle.putInt("selectedCategoryID",selectedCategoryID);
		getLoaderManager().initLoader(0, bundle, this);
		updateActionBarTitle();
	}
	
	public void reLoadPublicationsByCategory(Integer categoryID) {
		this.selectedSyndicationID = null;
		this.dateNewPublicationsFound = null;
		this.selectedCategoryID = categoryID;
		this.selectFavoritePublications = false;
		Bundle bundle = new Bundle();
		bundle.putInt("selectedCategoryID",this.selectedCategoryID);
		if(isAdded()){
			getLoaderManager().restartLoader(0, bundle, this);
			updateActionBarTitle();
		}
	}

	private void loadPublicationsBySyndication() {
		Bundle bundle = new Bundle();
		bundle.putInt("selectedSyndicationID", selectedSyndicationID);
		getLoaderManager().initLoader(0, bundle, this);
		updateActionBarTitle();
	}
	
	public void reLoadPublicationsBySyndication(Integer syndicationID) {
		this.selectedSyndicationID = syndicationID;
		this.selectedCategoryID = null;
		this.dateNewPublicationsFound = null;
		this.selectFavoritePublications = false;
		
		Bundle bundle = new Bundle();
		bundle.putInt("selectedSyndicationID", this.selectedSyndicationID);
		if (isAdded()) {
			getLoaderManager().restartLoader(0, bundle, this);
			updateActionBarTitle();
		}
	}
	
	@Override
	public void reLoadPublicationsAfterCatgoryDeleted(Integer deletedCategoryId) {
		if (selectedCategoryID != null
				&& selectedCategoryID.compareTo(deletedCategoryId) == 0) {
			selectedCategoryID = null;
			reloadPublications();
		}
	}

	@Override
	public void reLoadPublicationsAfterSyndicationDeleted(Integer deletedSyndicationId) {
		if (selectedSyndicationID != null
				&& selectedSyndicationID.compareTo(deletedSyndicationId) == 0) {
			selectedSyndicationID = null;
		}
		refreshPublications();
	}
	
	@Override
	public void reLoadPublicationsByLastFound(String dateNewPublicationsFound) {
		
		this.selectedSyndicationID = null;
		this.selectedCategoryID = null;
		this.selectFavoritePublications = false;
		
		Bundle bundle = new Bundle();
		bundle.putString("dateNewPublicationsFound", dateNewPublicationsFound);

		if(isAdded()){
			getLoaderManager().restartLoader(0, bundle, this);
			updateActionBarTitle();
		}
	}

	// Load application with date last found
	public void loadPublicationsByLastFound(String dateNewPublicationsFound) {
		
		this.selectedSyndicationID = null;
		this.selectedCategoryID = null;
		this.selectFavoritePublications = false;
		
		Bundle bundle = new Bundle();
		bundle.putString("dateNewPublicationsFound", dateNewPublicationsFound);

		if(isAdded()){
			getLoaderManager().initLoader(0, bundle, this);
			updateActionBarTitle();
		}
	}
	
	// Call by broadcast receiver for refresh list with last items found and without change position 
	// when user use application 
	@Override public void reLoadPublicationsWithLastFound() {
		
		SharedPreferences preferences   =  getActivity().getPreferences(0);
		SharedPreferences.Editor editor = preferences.edit();

		int index = getListView().getFirstVisiblePosition();
		
		View v = getListView().getChildAt(0);
		int position = (v == null) ? 0 : v.getTop();
		
		editor.putInt("cursorCount", ((PublicationAdapter)getListAdapter()).getCursor().getCount());
		editor.putInt("publicationsListViewIndex", index);
		editor.putInt("publicationsListViewPosition", position);
		editor.commit();
		
		// Set the number found in menu
		/*Integer muberOfLastFound = preferences.getInt("newPublicationsRecorded", 0);
		if(muberOfLastFound > 0) {
			addNumberOfLastFoundInMenu(muberOfLastFound);
			getActivity().invalidateOptionsMenu();
		}*/
		
		refreshPublications();
	}
	
	@Override
	public void displayFavoritePublications() {
		Bundle bundle = new Bundle();
		
		this.selectedSyndicationID = null;
		this.selectedCategoryID = null;
		this.dateNewPublicationsFound = null;
		this.selectFavoritePublications = true;
		
		bundle.putBoolean("displayFavoritePublications", true);
		
		if (getLoaderManager() != null && getLoaderManager().getLoader(0) != null 
				&& getLoaderManager().getLoader(0).isStarted()) {
			getLoaderManager().restartLoader(0, bundle, this);
			updateActionBarTitle();
		}
		else {
			getLoaderManager().initLoader(0, bundle, this);
			updateActionBarTitle();
		}
	}
	
	private void updateActionBarTitle() {
		String title = null;
		ActionBar bar = getActivity().getActionBar();

		if (this.selectedSyndicationID != null) {
			title = syndicationName();
			
		} else if (this.selectedCategoryID != null) {
			title = categoryName();
		} 
		else if (this.dateNewPublicationsFound!= null) {
			title = dataNewPublicationsFoundLabel();
		} 
		else if (this.selectFavoritePublications) {
            title = getResources().getString(R.string.title_favorite);
		} 

		// title = state.getActionBarTitle();
		if (TextUtils.isEmpty(title)) {
			bar.setTitle(titleToHtml(getActivity().getTitle().toString()));
		} else {
			bar.setTitle(titleToHtml(title));
		}		
	}
	
	private Spanned titleToHtml(String s) {
		return Html.fromHtml("<b><u>" + s + "</u><b>");
	}
	
	private boolean isPreferenceToDisplayOnAppReader() {
		return PreferenceManager.getDefaultSharedPreferences(getActivity())
				.getBoolean("pref_display_publication", true);
	}

	private void displayOnApplicationReader(Integer publicationId,
			Integer syndicationId,String syndicationName, boolean isFavorite, String text,
			String link, String title) {
		Intent i = new Intent(getActivity(), ReaderActivity.class);
		
		i.addFlags(
				Intent.FLAG_ACTIVITY_CLEAR_TOP | 
				Intent.FLAG_ACTIVITY_SINGLE_TOP | 
			    Intent.FLAG_ACTIVITY_NEW_TASK);
		
		i.putExtra("read", text);
		i.putExtra("link", link);
		i.putExtra("title", title);
		
		i.putExtra("publicationId", publicationId);
		i.putExtra("syndicationId", syndicationId);
		i.putExtra("isFavorite", isFavorite);
		i.putExtra("syndicationName", syndicationName);
		startActivity(i);
	}

	private void displayOnSystemBrowser(String url) {
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
	 * After click "mark as read" on context menu, All publication of the selected syndication are marked as read
	 * @param syndicationId
	 */
	private void confirmMarkSyndicationPublicationsAsRead(final Integer syndicationId) {
		
		final Resources r = getResources();
		OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				markSyndicationPublicationsAsRead(syndicationId);
			}
		};

		String name = syndicationName(syndicationId);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());	
		builder.setMessage(r.getString(R.string.confirm_mark_as_read, name)).setTitle(name)
				.setNegativeButton(r.getString(android.R.string.cancel), null)
				.setPositiveButton(r.getString(android.R.string.ok), listener)
				.create().show();
	}
	
	@Override
	public void markAsRead() {
				
		final Resources r = getResources();
		OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				if (selectedSyndicationID != null) {
					markSyndicationPublicationsAsRead(selectedSyndicationID);
					
				} else if (selectedCategoryID != null) {
					markCategoryPublicationsAsRead(selectedCategoryID);
				}
				else if (dateNewPublicationsFound != null) {
					marklastPublicationFoundAsRead(dateNewPublicationsFound);
				}
				else {
					markAllPublicationsAsRead();
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		String who = r.getString(R.string.all);
		CharSequence title = getActivity().getTitle();
		if (selectedSyndicationID != null) {
			who = syndicationName();
			title = who;
		} else if (selectedCategoryID != null) {
			who = categoryName();
			title = who;
		}
		else if (dateNewPublicationsFound != null) {
			who = dataNewPublicationsFoundLabel();
			title = who;
		}
				
		builder.setMessage(r.getString(R.string.confirm_mark_as_read, who)).setTitle(title)
				.setNegativeButton(r.getString(android.R.string.cancel), null)
				.setPositiveButton(r.getString(android.R.string.ok), listener)
				.create().show();
	}

	private void markAllPublicationsAsRead() {
		publicationRepository.markAllPublicationsAsRead();
	}

	@Override
	public void markSyndicationPublicationsAsRead(Integer syndicationId) {
		publicationRepository.markSyndicationPublicationsAsRead(syndicationId);
	}

	@Override
	public void deletePublications(Integer syndicationID) {
		try {
			publicationRepository.deletePublications(syndicationID);
		} catch (Exception e) {
			Toast.makeText(getActivity(), "Sorry an error occured when trying to delete publications.",
					Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		
		if (key.compareTo("pref_display_unread") == 0 
				|| key.compareTo("pref_delete_all_publications") == 0 
				|| key.compareTo("pref_user_font_face") == 0
				|| key.compareTo("pref_user_font_size") == 0) {
			refreshPublications();
		} 
	}
	
	@Override
	public void markCategoryPublicationsAsRead(Integer categoryId) {
		publicationRepository.markCategoryPublicationsAsRead(categoryId);
	}
	
	public void marklastPublicationFoundAsRead(String dateNewPublicationsFound) {
		publicationRepository.marklastPublicationFoundAsRead(dateNewPublicationsFound);
	}

	@Override
	public void removeTooOLdPublications() {

		Intent intent = new Intent(getActivity(), CleanOlderDataService.class);
		PendingIntent pendingIntent = 
				PendingIntent.getService(getActivity(), 0, intent, PendingIntent.FLAG_ONE_SHOT);

		try {
			pendingIntent.send();
		} catch (CanceledException ce) {
			ce.printStackTrace();
		}
		
		
		/*try {
			publicationRepository.removeTooOLdPublications();
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		
	}

	@Override
	public Parcelable getListViewInstanceState() {
		return getListView().onSaveInstanceState();
	}
}
