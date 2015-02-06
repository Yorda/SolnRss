package free.solnRss.activity;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;
import de.greenrobot.event.EventBus;
import free.solnRss.R;
import free.solnRss.adapter.PublicationAdapter;
import free.solnRss.adapter.SectionsPagerAdapter;
import free.solnRss.alarmManager.FindNewPublicationsAlarmManager;
import free.solnRss.dialog.OneEditTextDialogBox;
import free.solnRss.event.ChangePublicationListStateEvent;
import free.solnRss.fragment.listener.CategoriesFragmentListener;
import free.solnRss.fragment.listener.PublicationsFragmentListener;
import free.solnRss.fragment.listener.SyndicationsFragmentListener;
import free.solnRss.manager.UpdatingProcessConnectionManager;
import free.solnRss.notification.NewPublicationsNotification;
import free.solnRss.service.SyndicationFinderService;
import free.solnRss.singleton.TypeFaceSingleton;
import free.solnRss.state.BookmarkedPublicationsListState;
import free.solnRss.state.PublicationsByLastSearchListState;

public class SolnRss extends Activity implements ActionBar.TabListener,
		SharedPreferences.OnSharedPreferenceChangeListener {

	private SyndicationsFragmentListener syndicationsListener;
	private CategoriesFragmentListener categoriesListener;
	private PublicationsFragmentListener publicationsListener;
	private ViewPager viewPager;
	
	// ---
	// Refresh list after found new publications
	// --
	private BroadcastReceiver newPublicationsFoundBroadcastReceiver = 
			new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			publicationsListener.reLoadPublicationsWithLastFound();
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS); 	
		
		setContentView(R.layout.activity_soln_rss);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setTitle(Html.fromHtml("<b><u>" + getTitle() + "</u><b>"));
		
		int apiVersion = android.os.Build.VERSION.SDK_INT;
		if (apiVersion >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			actionBar.setBackgroundDrawable(new ColorDrawable(0xeeeeee));
			actionBar.setStackedBackgroundDrawable(new ColorDrawable(0xeeeeee));
			getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
		}
		
		// Set up the ViewPager with the sections adapter.
		viewPager = (ViewPager) findViewById(R.id.pager);
		
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.		
		SectionsPagerAdapter sectionPageAdapter = new SectionsPagerAdapter(getFragmentManager(), getResources());
		viewPager.setAdapter(sectionPageAdapter);
		viewPager.setOffscreenPageLimit(3);
		
		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
						tabReSelected = position;
					}
				});
		
		actionBar.addTab(actionBar.newTab().setIcon(R.drawable.ic_tab_folder)
				.setTabListener(this));
		actionBar.addTab(actionBar.newTab().setIcon(R.drawable.ic_tab_file)
				.setTabListener(this));
		actionBar.addTab(actionBar.newTab().setIcon(R.drawable.ic_tab_earth)
				.setTabListener(this));

		viewPager.setCurrentItem(1);
		
		removeNotification();
		registerPreferenceManager();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		NewPublicationsNotification.NotifyEvent event = NewPublicationsNotification.NotifyEvent.detachFrom(getIntent());

		// Because https://stackoverflow.com/questions/6584997/cant-remove-intent-extra
		// Restart activity.
		if (event != null
				&& event.compareTo(NewPublicationsNotification.NotifyEvent.RESTART_ACTIVITY) == 0) {	
			
			String dateNewPublicationsFound = getIntent().getStringExtra("dateNewPublicationsFound");
			
			if (!TextUtils.isEmpty(dateNewPublicationsFound)) {
				
				SharedPreferences.Editor editor = getPreferences(0).edit();
				
				editor.putInt("selectedSyndicationID", -1);
				editor.putInt("selectedCategoryID", -1);
				editor.putString("filterText", null);
				editor.putInt("publicationsListViewIndex", -1);
				editor.putInt("publicationsListViewPosition", -1);
				editor.putString("dateNewPublicationsFound", dateNewPublicationsFound);
				editor.commit();
				
				getIntent().putExtra("dateNewPublicationsFound", new String());
			} 
			
			String lastFoundNumber = getIntent().getStringExtra("lastFoundNumber");
			if(!TextUtils.isEmpty(dateNewPublicationsFound)) {
				fireEventForPublicationBylastSearch(lastFoundNumber);
				getIntent().putExtra("lastFoundNumber", new String());
			}
			
		}
	}
	
	private void fireEventForPublicationBylastSearch(String lastFoundNumber) {
		ChangePublicationListStateEvent event = new ChangePublicationListStateEvent();
		PublicationsByLastSearchListState publicationsByLastSearchListState = new PublicationsByLastSearchListState();
		publicationsByLastSearchListState.init(this);
		publicationsByLastSearchListState.setLastFound(Integer.valueOf(lastFoundNumber));
		event.setState(publicationsByLastSearchListState);
		EventBus.getDefault().postSticky(event);
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences shared, String key) {
		
	}

	public void registerPreferenceManager() {

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		FindNewPublicationsAlarmManager.createInstance(sharedPreferences, this);
		
		TypeFaceSingleton.getInstance(this);
		
		LocalBroadcastManager.getInstance(this).registerReceiver(newPublicationsFoundBroadcastReceiver, 
				new IntentFilter("newPublicationFound"));
	}
	
	private void removeNotification() {
		
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancel(0x000001);

		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		editor.putInt("newPublicationsRecorded", 0);
		editor.putString("newPublicationsRecordDate", null);
		editor.commit();
	}
	
	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		if(menu != null){
			if (displayAlreadyReadPublications()) {
				menu.getItem(2).setTitle(
						getResources().getString(R.string.menu_hide_already_read));
			} else {
				menu.getItem(2).setTitle(
						getResources().getString(R.string.menu_show_already_read));
			}
		}
		return super.onMenuOpened(featureId, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		
		case R.id.menu_settings:
			// Open preferences screen
			Intent i = new Intent(SolnRss.this, SettingsActivity.class);
			startActivity(i);
			return true;

		case R.id.menu_add_categorie:
			openDialogForAddCategorie(); 
			return true;
			
		case R.id.menu_display_favorite:
			fireEventForDisplayBookmarkedPublications();
			displayPublicationFavorite();
			return true;
			
		case R.id.menu_already_read:
			 updateOptionDisplayPublicationsAlreadyRead();
			return true;
			
		case R.id.menu_add_site:	
			if (SyndicationFinderService.isAlreadyRunning == 1) {
				Toast.makeText(SolnRss.this, "Service is already running", Toast.LENGTH_LONG).show();
			} else {
				openDialogForAddSyndication();
			}
			return true;
			
		case R.id.menu_all_read:
			markAllPublicationsAsRead();
			return true;
		
		case android.R.id.home:
			viewPager.setCurrentItem(1);
			reLoadAllPublications();
			return true;
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void fireEventForDisplayBookmarkedPublications() {
		ChangePublicationListStateEvent event = new ChangePublicationListStateEvent();
		BookmarkedPublicationsListState bookmarkedPublicationsListState = new BookmarkedPublicationsListState();
		bookmarkedPublicationsListState.init(this);
		event.setState(bookmarkedPublicationsListState);
		EventBus.getDefault().postSticky(event);
	}
	
	private void updateOptionDisplayPublicationsAlreadyRead() {

		Boolean isShow = displayAlreadyReadPublications() ? false : true;
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		editor.putBoolean("pref_display_unread", isShow);
		editor.commit();
	}
	
	public void openDialogForAddCategory(View v){
		openDialogForAddCategorie();
	}
	
	public void openDialogForAddCategorie() {
		
		OneEditTextDialogBox oneEditTextDialogBox;
		oneEditTextDialogBox = new OneEditTextDialogBox(this, 
				getResources().getString(R.string.add_categorie) ,
				getResources().getString(R.string.new_category_hint),  
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				EditText e = (EditText)((AlertDialog)dialog).findViewById(R.id.one_edit_text_dialog);
				addCategorie(e.getText().toString());
			}
		});
		
		oneEditTextDialogBox.displayDialogBox();
	}

	public void openDialogForAddSyndication(View v){
		openDialogForAddSyndication();
	}
	
	public void openDialogForAddSyndication() {
		
		OneEditTextDialogBox oneEditTextDialogBox;
		oneEditTextDialogBox = new OneEditTextDialogBox(this, 
				getResources().getString(R.string.add_site) ,
				getResources().getString(R.string.new_syndication_hint),  
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				EditText e = (EditText)((AlertDialog)dialog).findViewById(R.id.one_edit_text_dialog);
				addSyndication(e.getText().toString());
			}
		});
		
		oneEditTextDialogBox.displayDialogBox();
	}
	
	/**
	 * All unread publication set to read
	 */
	private void markAllPublicationsAsRead() {
		publicationsListener.markAsRead();
	}
	
	void addCategorie(String newCatgorie) {
		if (!TextUtils.isEmpty(newCatgorie)) {
			categoriesListener.addCategory(this, newCatgorie);
		}
	}
	
	public void addSyndication(String url) {

		if (!UpdatingProcessConnectionManager.canUseConnection(getApplicationContext())) {
			warmUser(UpdatingProcessConnectionManager.noConnectionReason());
			return;
		}

		if (url == null || url.trim().length() == 0) {
			warmUser(getResources().getString(R.string.empty_url));
			return;
		}
		url = url.trim();
		
		if (!url.startsWith("http://")) {
			url = "http://" + url;
		}

		Intent intent = new Intent(this, SyndicationFinderService.class);
		intent.setAction("REGISTER_RECEIVER");
		intent.putExtra("ResultReceiver", findNewSyndicationResultReceiver);
		intent.putExtra("ResultReceiver_ID", hashCode());
		intent.putExtra("url", url);
		
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, 
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		try {
			pendingIntent.send();
		} catch (CanceledException ce) {
			ce.printStackTrace();
		}
	}
	
	private ResultReceiver findNewSyndicationResultReceiver = new ResultReceiver(new Handler()) {
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			// Refresh the syndication tab
			refreshSyndications();
			
			Integer newPublicationsNumber = resultData.getInt("newPublicationsNumber");
			String newSyndicationName = resultData.getString("newSyndicationName");
			
			Toast.makeText(SolnRss.this, getResources().getString(R.string.process_ok, 
					newSyndicationName, newPublicationsNumber),	Toast.LENGTH_LONG).show();
		};
	};
	
	
	@Override
	public void onTabSelected(Tab tab, 
			FragmentTransaction fragmentTransaction) {
		switch(tab.getPosition()){
		case 0:
			if (categoriesListener != null) {
				categoriesListener.loadCategories();
			}
			break;
		case 1:
			break;
		case 2:
			if (syndicationsListener != null) {
				syndicationsListener.loadSyndications();
			}
			break;
		}
		if (viewPager.getCurrentItem() != tab.getPosition()){
		    viewPager.setCurrentItem(tab.getPosition());
		}
	}
	
	// Use for move list to top when select a tab twice
	private int tabReSelected = 1;
		
	@Override
	public void  onTabReselected (Tab tab, 
			FragmentTransaction fragmentTransaction) {
		switch(tab.getPosition()){
		case 0:
			if (tabReSelected == 0) {
				categoriesListener.moveListViewToTop();
			}
			tabReSelected = 0;
			break;
		case 1:
			if (tabReSelected == 1) {
				publicationsListener.moveListViewToTop();
			}
			tabReSelected = 1;
			break;
		case 2:
			if (tabReSelected == 2) {
				syndicationsListener.moveListViewToTop();
			}
			tabReSelected = 2;
			break;
		}
	}
	
	@Override
	public void onTabUnselected(Tab tab,
			FragmentTransaction fragmentTransaction) {
	}	
	
	/**
	 * Call by a click on notification for refresh publication list
	 */
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
	    setIntent(intent);
	    NewPublicationsNotification.NotifyEvent event = NewPublicationsNotification.NotifyEvent.detachFrom(intent);
	    
		if (event != null
				&& event.compareTo(NewPublicationsNotification.NotifyEvent.RESTART_ACTIVITY) == 0) {
			
			resetPublicationsPositionListVew();
			publicationsListener.reLoadPublicationsByLastFound(intent.getExtras().getString("dateNewPublicationsFound"));
			
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putInt("newPublicationsRecorded", 0);
			editor.putString("newPublicationsRecordDate", null);
			editor.commit();
			
			String lastFoundNumber = getIntent().getStringExtra("lastFoundNumber");
			fireEventForPublicationBylastSearch(lastFoundNumber);
		}
	};
	
	private void warmUser(String msg) {
		String ok = getResources().getString(android.R.string.ok);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(msg);

		OnClickListener listener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		};
		builder.setPositiveButton(ok, listener);

		builder.create();
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private boolean displayAlreadyReadPublications() {
		return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
				"pref_display_unread", true);
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(newPublicationsFoundBroadcastReceiver);
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		if (isFinishing()) {
			publicationsListener.removeTooOLdPublications();
		}
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		default:
			break;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}
	
	public void setCategoriesFragmentListener(CategoriesFragmentListener categoriesFragmentListener) {
		this.categoriesListener = categoriesFragmentListener;
	}

	public SyndicationsFragmentListener getSyndicationsFragmentListener() {
		return syndicationsListener;
	}

	public void setSyndicationsFragmentListener(
			SyndicationsFragmentListener syndicationsFragmentListener) {
		this.syndicationsListener = syndicationsFragmentListener;
	}

	public void setPublicationsFragmentListener(
			PublicationsFragmentListener publicationsFragmentListener) {
		this.publicationsListener = publicationsFragmentListener;
	}
	
	public PublicationsFragmentListener getPublicationsFragmentListener() {
		return publicationsListener;
	}
	
	public void resetPublicationsPositionListVew() {

		SharedPreferences prefs = this.getPreferences(0);
		SharedPreferences.Editor editor = prefs.edit();
		// Reset position save
		editor.putInt("cursorCount", -1);
		editor.putInt("publicationsListViewIndex", -1);
		editor.putInt("publicationsListViewPosition", -1);
		editor.commit();
	}
	
	public void reLoadPublicationsBySyndication(Integer syndicationID) {
		resetPublicationsPositionListVew();
		publicationsListener.reLoadPublicationsBySyndication(syndicationID);
		viewPager.setCurrentItem(1);
		publicationsListener.moveListViewToTop();
	}

	public void reLoadPublicationsByCategorie(Integer categorieID) {
		resetPublicationsPositionListVew();
		publicationsListener.reLoadPublicationsByCategory(categorieID);
		viewPager.setCurrentItem(1);
		publicationsListener.moveListViewToTop();
	}
	
	public void reLoadCategoriesAfterSyndicationDeleted() {
		categoriesListener.reLoadCategoriesAfterSyndicationDeleted();
	}
	
	public void refreshPublications() {
		publicationsListener.refreshPublications();
	}
	
	public void reLoadPublicationsAfterCatgoryDeleted(Integer deletedCategoryId) {
		publicationsListener.reLoadPublicationsAfterCatgoryDeleted(deletedCategoryId);
	}
	
	public void reLoadPublicationsAfterSyndicationDeleted(Integer deletedSyndicationId) {
		publicationsListener.reLoadPublicationsAfterSyndicationDeleted(deletedSyndicationId);
	}
	
	public void reLoadAllPublications() {
		publicationsListener.reloadPublications();
		publicationsListener.moveListViewToTop();
	}
	
	public void reloadPublicationsWithAlreadyRead(View v) {
		publicationsListener.reloadPublicationsWithAlreadyRead();
		publicationsListener.moveListViewToTop();
	}
	
	public void reLoadAllPublications(View v) {
		reLoadAllPublications();
	}
	
	public void displayPublicationFavorite() {
		publicationsListener.displayFavoritePublications();
	}

	public void displaySyndications() {
		syndicationsListener.loadSyndications();
		viewPager.setCurrentItem(2);
		reLoadAllPublications();
	}

	public void refreshSyndications() {
		syndicationsListener.reloadSyndications();
	}
}
