package free.solnRss.fragment;


import android.app.ActionBar;
import android.app.AlertDialog;
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
import free.solnRss.fragment.listener.PublicationsFragmentListener;
import free.solnRss.repository.PublicationContentRepository;
import free.solnRss.repository.PublicationRepository;
import free.solnRss.repository.PublicationTable;
import free.solnRss.repository.SyndicationTable;
import free.solnRss.singleton.TypeFaceSingleton;
import free.solnRss.utility.Constants;


public class PublicationsFragment extends AbstractFragment implements PublicationsFragmentListener,
SharedPreferences.OnSharedPreferenceChangeListener {

	private PublicationRepository			publicationRepository;
	private PublicationContentRepository	publicationContentRepository;
	private String							dateNewPublicationsFound;
	private Integer							selectedSyndicationID;
	private Integer							nextSelectedSyndicationID;				// Selected by context menu
	private Integer							selectedCategoryID;
	private Boolean							selectFavoritePublications	= false;

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup vg, final Bundle save) {

		final View fragment = inflater.inflate(R.layout.fragment_publications, vg, false);
		listContainer = fragment.findViewById(R.id.listContainer);
		progressContainer = fragment.findViewById(R.id.progressContainer);
		emptyLayoutId = R.id.emptyPublicationsLayout;
		listShown = true;

		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);

		return fragment;
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		publicationRepository = new PublicationRepository(getActivity());
		publicationContentRepository = new PublicationContentRepository(getActivity());
		displayList(savedInstanceState);

		registerForContextMenu(getListView());

		getListView().setTextFilterEnabled(true);

		((SolnRss) getActivity()).setPublicationsFragmentListener(this);

		setListShown(false);

		setHasOptionsMenu(true);
	}

	private void displayList(final Bundle save) {

		final SharedPreferences sharedPreferences = getActivity().getPreferences(0);

		// Get the publication list state
		final String savedPublicationListState = sharedPreferences.getString("publication list state", null);
		if (savedPublicationListState != null) {

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
		} else {
			loadPublications();
		}
		setFilterText(sharedPreferences.getString("filterText", null));
	}

	@Override
	protected void displayEmptyMessage() {

		final LinearLayout l = (LinearLayout) getActivity().findViewById(emptyLayoutId);
		l.setVisibility(View.VISIBLE);

		getActivity().findViewById(R.id.displayAllButton).setVisibility(View.VISIBLE);

		if (selectedSyndicationID != null) {
			// Selected by syndication are empty
			writeEmptyMessage(syndicationName(), R.string.empty_publications_with_syndication,
					R.string.empty_publications_with_syndication_with_filter, getFilterText());

		} else if (selectedCategoryID != null) {
			// Selected by category is empty
			writeEmptyMessage(categoryName(), R.string.empty_publications_with_category, R.string.empty_publications_with_category_with_filter,
					getFilterText());
		}

		else if (dateNewPublicationsFound != null) {
			// Selected by last date found
			writeEmptyMessage(null, R.string.empty_publications_date_last_found, R.string.empty_publications_date_last_found, getFilterText());
		} else {

			// All publication
			writeEmptyMessage(null, R.string.empty_publications, R.string.empty_publications_with_filter, getFilterText());

			getActivity().findViewById(R.id.displayAllButton).setVisibility(View.GONE);
		}

		// If already read publications are hidden display button for display them
		displayAlreadyReadPublicationsButton();
	}

	private void writeEmptyMessage(final String name, final int idMsgWithoutFilter, final int idMsgWithFilter, final String filter) {
		if (TextUtils.isEmpty(filter)) {
			if (name == null) {
				displayEmptyListView(getResources().getString(idMsgWithoutFilter));
			} else {
				displayEmptyListView(getResources().getString(idMsgWithoutFilter, name));
			}
		} else {
			if (name == null) {
				displayEmptyListView(getResources().getString(idMsgWithFilter, filter));
			} else {
				displayEmptyListView(getResources().getString(idMsgWithFilter, name, filter));
			}
		}
	}

	private void displayEmptyListView(final String msg) {

		final TextView tv = (TextView) getActivity().findViewById(R.id.emptyPublicationsMessage);

		final Typeface userTypeFace = TypeFaceSingleton.getInstance(getActivity()).getUserTypeFace();
		if (userTypeFace != null) {
			tv.setTypeface(userTypeFace);
		}

		final int userFontSize = TypeFaceSingleton.getInstance(getActivity()).getUserFontSize();
		if (userFontSize != Constants.FONT_SIZE) {
			tv.setTextSize(userFontSize);
		}

		tv.setText(msg);

		final Button displayAllButton = (Button) getActivity().findViewById(R.id.displayAllButton);
		if (userTypeFace != null) {
			displayAllButton.setTypeface(userTypeFace);
		}
		if (userFontSize != Constants.FONT_SIZE) {
			displayAllButton.setTextSize(userFontSize);
		}

		final Button displayAlreadyReadButton = (Button) getActivity().findViewById(R.id.displayAlreadyReadButton);
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
	public void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {

		final Cursor cursor = ((PublicationAdapter) l.getAdapter()).getCursor();

		if (position == cursor.getCount()) {
			return;
		}

		final Integer syndicationId = cursor.getInt(cursor.getColumnIndex(PublicationTable.COLUMN_SYNDICATION_ID));
		final Integer publicationId = cursor.getInt(0);
		final boolean isFavorite = cursor.getInt(6) != 1 ? false : true;

		final String[] publicationContent = publicationContentRepository.retrievePublicationContent(syndicationId, publicationId);

		final String title = cursor.getString(1); //getPublicationTitle(cursor);
		final String link = publicationContent[0]; //getPublicationUrl(cursor);
		final String description = publicationContent[1]; //hasPublicationContentToDisplay(cursor);
		final String synsdicationName = cursor.getString(3);

		clickOnPublicationItem(cursor, l, v, position, id);

		if (description != null && description.trim().length() > 0) {
			if (isPreferenceToDisplayOnAppReader()) {
				//displayOnApplicationReader(description, link, title);
				displayOnApplicationReader(publicationId, syndicationId, synsdicationName, isFavorite, description, link, title);
			} else {
				displayOnSystemBrowser(link);
			}
		} else {
			displayOnSystemBrowser(link);
		}
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		nextSelectedSyndicationID = null;

		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		final Cursor c = ((PublicationAdapter) getListAdapter()).getCursor();
		c.moveToPosition(info.position);

		menu.setHeaderTitle(c.getString(c.getColumnIndex(SyndicationTable.COLUMN_NAME)));

		nextSelectedSyndicationID = c.getInt(c.getColumnIndex(PublicationTable.COLUMN_SYNDICATION_ID));

		final MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.publications_context, menu);

		if (selectedSyndicationID != null) {
			menu.getItem(0).setTitle(getResources().getString(R.string.display_all_publication));
		}
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_see_only:
				if (selectedSyndicationID == null) {
					reLoadPublicationsBySyndication(nextSelectedSyndicationID);
					moveListViewToTop();

				} else {
					reloadPublications();
					moveListViewToTop();
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
	}

	@Override
	public void onPause() {

		super.onPause();

		final SharedPreferences.Editor editor = getActivity().getPreferences(0).edit();
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

		final int index = getListView().getFirstVisiblePosition();
		editor.putInt("publicationsListViewIndex", index);

		final View v = getListView().getChildAt(0);
		final int position = v == null ? 0 : v.getTop();
		editor.putInt("publicationsListViewPosition", position);
		editor.commit();

		EventBus.getDefault().unregister(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void setListPositionOnScreen() {

		final SharedPreferences prefs = getActivity().getPreferences(0);
		int index = prefs.getInt("publicationsListViewIndex", -1);
		final int position = prefs.getInt("publicationsListViewPosition", -1);
		final int cursorCount = prefs.getInt("cursorCount", -1);

		final SharedPreferences.Editor editor = prefs.edit();

		if (cursorCount != -1) {

			// Get differece between old and new cursor
			final int newCursorCount = ((PublicationAdapter) getListAdapter()).getCursor().getCount() - cursorCount;
			if (newCursorCount > 0) {
				index = index + newCursorCount;
			}

			getListView().setSelectionFromTop(index, position);
			editor.putInt("cursorCount", -1);
			editor.putInt("publicationsListViewIndex", -1);
			editor.putInt("publicationsListViewPosition", -1);
			editor.commit();
		} else if (index != -1) {

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
		final String[] from = { PublicationTable.COLUMN_TITLE, PublicationTable.COLUMN_TITLE };
		final int[] to = { android.R.id.text1, android.R.id.text2 };

		simpleCursorAdapter = new PublicationAdapter(getActivity(), R.layout.publications, null, from, to, 0);
		setListAdapter(simpleCursorAdapter);
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle bundle) {

		getActivity().setProgressBarIndeterminateVisibility(true);

		if (bundle != null) {
			if (bundle.getInt("selectedSyndicationID") != 0) {
				selectedSyndicationID = bundle.getInt("selectedSyndicationID");
			} else if (bundle.getInt("selectedCategoryID") != 0) {
				selectedCategoryID = bundle.getInt("selectedCategoryID");
			} else if (bundle.getString("dateNewPublicationsFound") != null) {
				dateNewPublicationsFound = bundle.getString("dateNewPublicationsFound");
			} else if (bundle.getBoolean("displayFavoritePublications") == true) {
				// selectFavoritePublications = true;
				return publicationRepository.loadBookmarkedPublications(getFilterText());
			}
		}

		return publicationRepository.loadPublications(getFilterText(), selectedSyndicationID, selectedCategoryID, dateNewPublicationsFound,
				displayAlreadyReadPublications());

		// return state.displayList();
	}

	private boolean displayAlreadyReadPublications() {
		return PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("pref_display_unread", true);
	}

	@Override
	protected void queryTheTextChange() {
		final Bundle bundle = new Bundle();

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
	 * Click on a publication item. It's mark this publication as read and add one click to the syndication
	 */
	private void clickOnPublicationItem(final Cursor cursor, final ListView l, final View v, final int position, final long id) {

		new Runnable() {
			@Override
			public void run() {
				try {
					publicationRepository.markOnePublicationAsReadByUser(cursor.getInt(0),
							cursor.getInt(cursor.getColumnIndex(PublicationTable.COLUMN_SYNDICATION_ID)),
							cursor.getInt(cursor.getColumnIndex(SyndicationTable.COLUMN_NUMBER_CLICK)));
					((PublicationAdapter) getListAdapter()).notifyDataSetChanged();
					refreshPublications();
					((SolnRss) getActivity()).refreshSyndications();

				} catch (final Exception e) {
					Toast.makeText(getActivity(), "Error unable to set this publication as read", Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}
			}
		}.run();
	}

	@Override
	public void moveListViewToTop() {
		getListView().setSelection(0);
	}

	@Override
	public void loadPublications() {
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void reloadPublications() {
		selectedSyndicationID = null;
		selectedCategoryID = null;
		dateNewPublicationsFound = null;
		selectFavoritePublications = false;

		if (isAdded()) {
			getLoaderManager().restartLoader(0, null, this);
			updateActionBarTitle();
		}
	}

	@Override
	public void reloadPublicationsWithAlreadyRead() {
		final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
		editor.putBoolean("pref_display_unread", true);
		editor.commit();
	}

	// Refresh the list after event like a syndication delete.
	@Override
	public void refreshPublications() {
		if (selectedSyndicationID != null) {
			reLoadPublicationsBySyndication(selectedSyndicationID);
		} else if (selectedCategoryID != null) {
			reLoadPublicationsByCategory(selectedCategoryID);
		} else if (dateNewPublicationsFound != null) {
			reLoadPublicationsByLastFound(dateNewPublicationsFound);
		} else if (selectFavoritePublications == true) {
			displayFavoritePublications();
		} else {
			reloadPublications();
		}
	}

	private void loadPublicationsByCategory() {
		final Bundle bundle = new Bundle();
		bundle.putInt("selectedCategoryID", selectedCategoryID);
		getLoaderManager().initLoader(0, bundle, this);
		updateActionBarTitle();
	}

	@Override
	public void reLoadPublicationsByCategory(final Integer categoryID) {
		selectedSyndicationID = null;
		dateNewPublicationsFound = null;
		selectedCategoryID = categoryID;
		selectFavoritePublications = false;
		final Bundle bundle = new Bundle();
		bundle.putInt("selectedCategoryID", selectedCategoryID);
		if (isAdded()) {
			getLoaderManager().restartLoader(0, bundle, this);
			updateActionBarTitle();
		}
	}

	private void loadPublicationsBySyndication() {
		final Bundle bundle = new Bundle();
		bundle.putInt("selectedSyndicationID", selectedSyndicationID);
		getLoaderManager().initLoader(0, bundle, this);
		updateActionBarTitle();
	}

	@Override
	public void reLoadPublicationsBySyndication(final Integer syndicationID) {
		selectedSyndicationID = syndicationID;
		selectedCategoryID = null;
		dateNewPublicationsFound = null;
		selectFavoritePublications = false;

		final Bundle bundle = new Bundle();
		bundle.putInt("selectedSyndicationID", selectedSyndicationID);
		if (isAdded()) {
			getLoaderManager().restartLoader(0, bundle, this);
			updateActionBarTitle();
		}
	}

	@Override
	public void reLoadPublicationsAfterCatgoryDeleted(final Integer deletedCategoryId) {
		if (selectedCategoryID != null && selectedCategoryID.compareTo(deletedCategoryId) == 0) {
			selectedCategoryID = null;
			reloadPublications();
		}
	}

	@Override
	public void reLoadPublicationsAfterSyndicationDeleted(final Integer deletedSyndicationId) {
		if (selectedSyndicationID != null && selectedSyndicationID.compareTo(deletedSyndicationId) == 0) {
			selectedSyndicationID = null;
		}
		refreshPublications();
	}

	@Override
	public void reLoadPublicationsByLastFound(final String dateNewPublicationsFound) {

		selectedSyndicationID = null;
		selectedCategoryID = null;
		selectFavoritePublications = false;

		final Bundle bundle = new Bundle();
		bundle.putString("dateNewPublicationsFound", dateNewPublicationsFound);

		if (isAdded()) {
			getLoaderManager().restartLoader(0, bundle, this);
			updateActionBarTitle();
		}
	}

	// Load application with date last found
	public void loadPublicationsByLastFound(final String dateNewPublicationsFound) {

		selectedSyndicationID = null;
		selectedCategoryID = null;
		selectFavoritePublications = false;

		final Bundle bundle = new Bundle();
		bundle.putString("dateNewPublicationsFound", dateNewPublicationsFound);

		if (isAdded()) {
			getLoaderManager().initLoader(0, bundle, this);
			updateActionBarTitle();
		}
	}

	// Call by broadcast receiver for refresh list with last items found and without change position
	// when user use application
	@Override
	public void reLoadPublicationsWithLastFound() {

		final SharedPreferences preferences = getActivity().getPreferences(0);
		final SharedPreferences.Editor editor = preferences.edit();

		final int index = getListView().getFirstVisiblePosition();

		final View v = getListView().getChildAt(0);
		final int position = v == null ? 0 : v.getTop();

		editor.putInt("cursorCount", ((PublicationAdapter) getListAdapter()).getCursor().getCount());
		editor.putInt("publicationsListViewIndex", index);
		editor.putInt("publicationsListViewPosition", position);
		editor.commit();

		// Set the number found in menu
		/*
		 * Integer muberOfLastFound = preferences.getInt("newPublicationsRecorded", 0); if(muberOfLastFound > 0) {
		 * addNumberOfLastFoundInMenu(muberOfLastFound); getActivity().invalidateOptionsMenu(); }
		 */

		refreshPublications();
	}

	@Override
	public void displayFavoritePublications() {
		final Bundle bundle = new Bundle();

		selectedSyndicationID = null;
		selectedCategoryID = null;
		dateNewPublicationsFound = null;
		selectFavoritePublications = true;

		bundle.putBoolean("displayFavoritePublications", true);

		if (getLoaderManager() != null && getLoaderManager().getLoader(0) != null && getLoaderManager().getLoader(0).isStarted()) {
			getLoaderManager().restartLoader(0, bundle, this);
			updateActionBarTitle();
		} else {
			getLoaderManager().initLoader(0, bundle, this);
			updateActionBarTitle();
		}
	}

	private void updateActionBarTitle() {
		String title = null;
		final ActionBar bar = getActivity().getActionBar();

		if (selectedSyndicationID != null) {
			title = syndicationName();

		} else if (selectedCategoryID != null) {
			title = categoryName();
		} else if (dateNewPublicationsFound != null) {
			title = dataNewPublicationsFoundLabel();
		} else if (selectFavoritePublications) {
			title = getResources().getString(R.string.title_favorite);
		}

		// title = state.getActionBarTitle();
		if (TextUtils.isEmpty(title)) {
			bar.setTitle(titleToHtml(getActivity().getTitle().toString()));
		} else {
			bar.setTitle(titleToHtml(title));
		}
	}

	private Spanned titleToHtml(final String s) {
		return Html.fromHtml("<b><u>" + s + "</u><b>");
	}

	private boolean isPreferenceToDisplayOnAppReader() {
		return PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("pref_display_publication", true);
	}

	private void displayOnApplicationReader(final Integer publicationId, final Integer syndicationId, final String syndicationName,
			final boolean isFavorite, final String text, final String link, final String title) {
		final Intent i = new Intent(getActivity(), ReaderActivity.class);

		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

		i.putExtra("read", text);
		i.putExtra("link", link);
		i.putExtra("title", title);

		i.putExtra("publicationId", publicationId);
		i.putExtra("syndicationId", syndicationId);
		i.putExtra("isFavorite", isFavorite);
		i.putExtra("syndicationName", syndicationName);
		startActivity(i);
	}

	private void displayOnSystemBrowser(final String url) {
		// Start a browser
		final Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		try {
			startActivity(openUrlIntent);
		} catch (final Exception e) {
			Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.open_browser_bad_url), Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * After click "mark as read" on context menu, All publication of the selected syndication are marked as read
	 *
	 * @param syndicationId
	 */
	private void confirmMarkSyndicationPublicationsAsRead(final Integer syndicationId) {

		final Resources r = getResources();
		final OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				markSyndicationPublicationsAsRead(syndicationId);
			}
		};

		final String name = syndicationName(syndicationId);
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(r.getString(R.string.confirm_mark_as_read, name)).setTitle(name)
		.setNegativeButton(r.getString(android.R.string.cancel), null).setPositiveButton(r.getString(android.R.string.ok), listener).create()
		.show();
	}

	@Override
	public void markAsRead() {

		final Resources r = getResources();
		final OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {

				if (selectedSyndicationID != null) {
					markSyndicationPublicationsAsRead(selectedSyndicationID);

				} else if (selectedCategoryID != null) {
					markCategoryPublicationsAsRead(selectedCategoryID);
				} else if (dateNewPublicationsFound != null) {
					marklastPublicationFoundAsRead(dateNewPublicationsFound);
				} else {
					markAllPublicationsAsRead();
				}
			}
		};

		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		String who = r.getString(R.string.all);
		CharSequence title = getActivity().getTitle();
		if (selectedSyndicationID != null) {
			who = syndicationName();
			title = who;
		} else if (selectedCategoryID != null) {
			who = categoryName();
			title = who;
		} else if (dateNewPublicationsFound != null) {
			who = dataNewPublicationsFoundLabel();
			title = who;
		}

		builder.setMessage(r.getString(R.string.confirm_mark_as_read, who)).setTitle(title)
		.setNegativeButton(r.getString(android.R.string.cancel), null).setPositiveButton(r.getString(android.R.string.ok), listener).create()
		.show();
	}

	private void markAllPublicationsAsRead() {
		publicationRepository.markAllPublicationsAsRead();
	}

	@Override
	public void markSyndicationPublicationsAsRead(final Integer syndicationId) {
		publicationRepository.markSyndicationPublicationsAsRead(syndicationId);
	}

	@Override
	public void deletePublications(final Integer syndicationID) {
		try {
			publicationRepository.deletePublications(syndicationID);
		} catch (final Exception e) {
			Toast.makeText(getActivity(), "Sorry an error occured when trying to delete publications.", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {

		if (key.compareTo("pref_display_unread") == 0 || key.compareTo("pref_delete_all_publications") == 0
				|| key.compareTo("pref_user_font_face") == 0 || key.compareTo("pref_user_font_size") == 0) {
			refreshPublications();
		}
	}

	@Override
	public void markCategoryPublicationsAsRead(final Integer categoryId) {
		publicationRepository.markCategoryPublicationsAsRead(categoryId);
	}

	public void marklastPublicationFoundAsRead(final String dateNewPublicationsFound) {
		publicationRepository.marklastPublicationFoundAsRead(dateNewPublicationsFound);
	}

	@Override
	public void removeTooOLdPublications() {
		try {
			publicationRepository.removeTooOLdPublications();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Parcelable getListViewInstanceState() {
		return getListView().onSaveInstanceState();
	}
}
