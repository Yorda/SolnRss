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
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import free.solnRss.R;
import free.solnRss.activity.ReaderActivity;
import free.solnRss.activity.SolnRss;
import free.solnRss.adapter.PublicationAdapter;
import free.solnRss.fragment.listener.PublicationsFragmentListener;
import free.solnRss.repository.PublicationRepository;
import free.solnRss.repository.PublicationTable;
import free.solnRss.repository.SyndicationTable;

public class PublicationsFragment extends AbstractFragment implements
		PublicationsFragmentListener {

	private PublicationRepository publicationRepository;
	private Integer selectedSyndicationID;
	private Integer nextSelectedSyndicationID; // selected by context menu
	private Integer selectedCategoryID;

	@Override protected void displayEmptyMessage() {
		LinearLayout l = (LinearLayout) getActivity().findViewById(emptyLayoutId);
		l.setVisibility(View.VISIBLE);
		
		String filter = getFilterText();
		getActivity().findViewById(R.id.displayAllButton).setVisibility(View.VISIBLE);
		
		// If a syndication is selected and it's empty
		if (selectedSyndicationID != null) {
			if (TextUtils.isEmpty(filter)) {
				setEmptyMessage(getResources().getString(
						R.string.empty_publications_with_syndication,
						syndicationName()));
			} else {
				setEmptyMessage(getResources().getString(
						R.string.empty_publications_with_syndication_with_filter,
						syndicationName(), filter));
			}

		} // If a category is selected and it's empty
		else if (selectedCategoryID != null) {
			if (TextUtils.isEmpty(filter)) {
				setEmptyMessage(getResources().getString(
						R.string.empty_publications_with_category,
						categoryName()));
			} else {
				setEmptyMessage(getResources().getString(
						R.string.empty_publications_with_category_with_filter,
						categoryName(), filter));
			}
		}
		// If all publications are empty
		else {
			
			if (TextUtils.isEmpty(filter)) {
				setEmptyMessage(getResources().getString(
						R.string.empty_publications));
			} else {
				setEmptyMessage(getResources().getString(
						R.string.empty_publications_with_filter, filter));
			}
			
			getActivity().findViewById(R.id.displayAllButton)
					.setVisibility(View.GONE);
		}
		// If already read publications are hidden display button for display them
		displayAlreadyReadPublicationsButton();
	}
	
	private void displayAlreadyReadPublicationsButton() {
		
		if (displayAlreadyReadPublications()) {
			getActivity().findViewById(R.id.displayAlreadyReadButton)
					.setVisibility(View.INVISIBLE);

		} else {
			getActivity().findViewById(R.id.displayAlreadyReadButton)
					.setVisibility(View.VISIBLE);
		}
	}
	
	private void setEmptyMessage(String msg) {
		TextView tv = (TextView)getActivity().findViewById(R.id.emptyPublicationsMessage);
		tv.setText(msg);
	}

	private String categoryName() {
		return categoryName(selectedCategoryID);
	}

	private String syndicationName() {
		return syndicationName(selectedSyndicationID);
	}
	
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
		
		publicationRepository = new PublicationRepository(getActivity());
		restoreOrFirstDisplay(savedInstanceState);
		
		registerForContextMenu(getListView());
		
		getListView().setTextFilterEnabled(true);
		
		((SolnRss)getActivity()).setPublicationsFragmentListener(this);
		
		setListShown(false);
		
		setHasOptionsMenu(true);		
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		
    	Cursor cursor = ((PublicationAdapter) l.getAdapter()).getCursor();
		clickOnPublicationItem(cursor, l, v, position, id);
		
		String link = getPublicationUrl(cursor);
		String title = getPublicationTitle(cursor);
		String description = hasPublicationContentToDisplay(cursor);
		
		if (description != null && description.trim().length() > 0) {
			if (isPreferenceToDisplayOnAppReader()) {
				displayOnApplicationReader(description, link, title);
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
	
	@Override public void onPause() {
		super.onPause();
		SharedPreferences.Editor editor = getActivity().getPreferences(0)
				.edit();
		if (selectedSyndicationID != null) {
			editor.putInt("selectedSyndicationID", selectedSyndicationID);
		} else {
			editor.putInt("selectedSyndicationID", -1);
		}

		if (selectedCategoryID != null) {
			editor.putInt("selectedCategoryID", selectedCategoryID);
		} else {
			editor.putInt("selectedCategoryID", -1);
		}
		
		if (!TextUtils.isEmpty(getFilterText())) {
			editor.putString("filterText", getFilterText());
		} else {
			editor.putString("filterText", null);
		}

		savePositionOnScreen(editor);
		editor.commit();
	}
	
	private void restoreOrFirstDisplay(Bundle save) {
		
		SharedPreferences prefs = getActivity().getPreferences(0);
		if (prefs.getInt("selectedSyndicationID", -1) != -1) {
			selectedSyndicationID = prefs.getInt("selectedSyndicationID", -1);
		}
		if (prefs.getInt("selectedCategoryID", -1) != -1) {
			selectedCategoryID = prefs.getInt("selectedCategoryID", -1);
		}
		setFilterText(prefs.getString("filterText", null));
		
		if (selectedSyndicationID != null) {
			loadPublicationsBySyndication();
		} else if (selectedCategoryID != null) {
			loadPublicationsByCategory();
		} else {
			loadPublications();
		}
	}
	
	private void savePositionOnScreen(SharedPreferences.Editor editor) {

		getListView().setScrollbarFadingEnabled(false);
		int index = getListView().getFirstVisiblePosition();
		editor.putInt("publicationsListViewIndex", index);

		View v = getListView().getChildAt(0);
		int position = (v == null) ? 0 : v.getTop();
		editor.putInt("publicationsListViewPosition", position);
	}
	
	@Override
	protected void setListPositionOnScreen() {
		SharedPreferences prefs = getActivity().getPreferences(0);
		int index = prefs.getInt("publicationsListViewIndex", -1);
		int position = prefs.getInt("publicationsListViewPosition", -1);

		if (index != -1) {
			// Set list view at position
			getListView().setSelectionFromTop(index, position);
			// Reset position save
			SharedPreferences.Editor editor = prefs.edit();
			editor.putInt("publicationsListViewIndex", -1);
			editor.putInt("publicationsListViewPosition", -1);
			editor.commit();
		}
	}
	
	@Override
	protected void initAdapter() {
		final String[] from = { PublicationTable.COLUMN_TITLE,  PublicationTable.COLUMN_LINK };
		final int[] to = { android.R.id.text1, android.R.id.text2 };
		simpleCursorAdapter = new PublicationAdapter(getActivity(),R.layout.publications, null, from, to, 0);
		setListAdapter((PublicationAdapter)simpleCursorAdapter);		
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		
		if (bundle != null) {
			if (bundle.getInt("selectedSyndicationID") != 0) {
				selectedSyndicationID = bundle.getInt("selectedSyndicationID");				
			} else if (bundle.getInt("selectedCategoryID") != 0) {
				selectedCategoryID = bundle.getInt("selectedCategoryID");
			}
		}

		return publicationRepository.loadPublications(getFilterText(),
				selectedSyndicationID, selectedCategoryID,
				displayAlreadyReadPublications());
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

		getLoaderManager().restartLoader(0, bundle, this);
	}
	
	/*
	 * Click on a publication item. It's mark this publication as read and add
	 * one click to the syndication
	 */
	private void clickOnPublicationItem(Cursor cursor, ListView l, View v,	int position, long id) {
		
		// Set this publication as already read
		// cursor.getColumnIndex(PublicationTable.COLUMN_ID)
		int publicationId = cursor.getInt(0);

		publicationRepository.markPublicationAsRead(publicationId);
		// refreshPublications();
		//markPublicationAsRead(publicationId);
		
		// Add a click to the syndication
		// cursor.getColumnIndex(PublicationTable.COLUMN_SYNDICATION_ID)
		int syndicationId = cursor.getInt(6	);
		
		// cursor.getColumnIndex(SyndicationTable.COLUMN_NUMBER_CLICK)
		int numberOfClick = cursor.getInt(7);

		((SolnRss) getActivity()).getSyndicationsFragmentListener()
				.addOneReadToSyndication(syndicationId, numberOfClick);
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
		if (isAdded()) {
			getLoaderManager().restartLoader(0, null, this);
			updateActionBarTitle();
		}
	}

	@Override
	public void reloadPublicationsWithAlreadyRead() {
		
		SharedPreferences.Editor editor = PreferenceManager
				.getDefaultSharedPreferences(getActivity()).edit();
		editor.putBoolean("pref_display_unread", true);
		editor.commit();
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
		updateActionBarTitle();
	}
	
	public void reLoadPublicationsByCategory(Integer categoryID) {
		this.selectedSyndicationID = null;
		this.selectedCategoryID = categoryID;
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
	
	private void updateActionBarTitle() {
		String title = null;
		ActionBar bar = getActivity().getActionBar();
		
		if (this.selectedSyndicationID != null) {
			title = syndicationName();
			
		} else if (this.selectedCategoryID != null) {
			title = categoryName();
		} 
		
		if (TextUtils.isEmpty(title)) {
			bar.setTitle(titleToHtml(getActivity().getTitle().toString()));
		} else {
			bar.setTitle(titleToHtml(title));
		}
	}
	
	private Spanned titleToHtml(String s) {
		return Html.fromHtml("<b><u>" + s + "</u><b>");
	}
	
	public String getPublicationUrl(Cursor cursor) {
		return cursor.getString(cursor
				.getColumnIndex(PublicationTable.COLUMN_LINK));
	}

	public String getPublicationTitle(Cursor cursor) {
		return cursor.getString(cursor
				.getColumnIndex(PublicationTable.COLUMN_TITLE));
	}
	
	public String hasPublicationContentToDisplay(Cursor cursor) {
		return cursor.getString(cursor
				.getColumnIndex(PublicationTable.COLUMN_PUBLICATION));
	}

	private boolean isPreferenceToDisplayOnAppReader() {
		return PreferenceManager.getDefaultSharedPreferences(getActivity())
				.getBoolean("pref_display_publication", true);
	}

	private void displayOnApplicationReader(String text, String link,String title) {
		Intent i = new Intent(getActivity(), ReaderActivity.class);
		i.putExtra("read", text);
		i.putExtra("link", link);
		i.putExtra("title", title);
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
					
				} else {
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
		publicationRepository.deletePublications(syndicationID);
	}	
	
	@Override
	public void markCategoryPublicationsAsRead(Integer categoryId) {
		publicationRepository.markCategoryPublicationsAsRead(categoryId);
	}
	
	
	/*@Deprecated
	private void markAsRead(final Integer syndicationId){
		ContentValues values = new ContentValues();
		values.put(PublicationTable.COLUMN_ALREADY_READ, "1");
		String selection = null;
		String[] args = null;
		if (syndicationId != null) {
			selection = " syn_syndication_id = ? ";
			args = new String[1];
			args[0] = syndicationId.toString();
		}
		getActivity().getContentResolver().update(PublicationsProvider.URI, values, selection, args);
		getLoaderManager().restartLoader(0, null, this);
	}
	
	@Deprecated
	public void markPublicationAsRead(int publicationId) {
		
		ContentValues values = new ContentValues();
		values.put(PublicationTable.COLUMN_ALREADY_READ, "1");
		
		String where = PublicationTable.COLUMN_ID + " = ? ";
		String args[] = { String.valueOf(publicationId) };
		
		getActivity().getContentResolver().update(PublicationsProvider.URI, values, where, args);
		
		refreshPublications();
		
	}
	
	@Deprecated
	public void addOneClickToSyndication(int syndicationId, int numberOfClick) {
		Uri uri = Uri.parse(SyndicationsProvider.URI + "/click/" + syndicationId);
		ContentValues values = new ContentValues();
		getActivity().getContentResolver().update(uri, values, null, null);
	}

	private void markCategoryPublicationsAsRead() {
	ContentValues values = new ContentValues();
	values.put(PublicationTable.COLUMN_ALREADY_READ, "1");
	String selection = " syn_syndication_id in (select syn_syndication_id from d_categorie_syndication where cas_categorie_id = ?) ";
	String[] args = { selectedCategoryID.toString() };

	getActivity().getContentResolver().update(PublicationsProvider.URI,
			values, selection, args);
	getLoaderManager().restartLoader(0, null, this);
	}*/

	

}
