package free.solnRss.activity;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import free.solnRss.R;
import free.solnRss.adapter.SectionsPagerAdapter;
import free.solnRss.dialog.AddItemDialog;
import free.solnRss.dialog.AddItemDialog.NewAddItemDialogListener;
import free.solnRss.fragment.listener.CategoriesFragmentListener;
import free.solnRss.fragment.listener.PublicationsFragmentListener;
import free.solnRss.fragment.listener.SyndicationsFragmentListener;
import free.solnRss.service.PublicationsFinderService;
import free.solnRss.service.SyndicationFinderService;

public class SolnRss extends Activity implements ActionBar.TabListener,
		SharedPreferences.OnSharedPreferenceChangeListener,
		NewAddItemDialogListener {
	
	private SyndicationsFragmentListener syndicationsListener;
	private CategoriesFragmentListener categoriesListener;
	private PublicationsFragmentListener publicationsListener;

	private SectionsPagerAdapter sectionPageAdapter;
	private ViewPager viewPager;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		if (displayAlreadyReadPublications()) {
			menu.getItem(3).setTitle(
					getResources().getString(R.string.menu_hide_already_read));
		} else {
			menu.getItem(3).setTitle(
					getResources().getString(R.string.menu_show_already_read));
		}
		return super.onMenuOpened(featureId, menu);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.e(SolnRss.this.getClass().getName(), "RESUME");
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		Log.e(SolnRss.this.getClass().getName(), "STOP");
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.e(SolnRss.this.getClass().getName(), "DESTROY");
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences shared, String key) {
		if (key.compareTo("pref_unread_font_weight") == 0) {
			publicationsListener.refreshPublications();
		}
		else if (key.compareTo("pref_display_unread") == 0) {
			publicationsListener.refreshPublications();
		}
		else if (key.compareTo("pref_sort_syndications") == 0) {
			syndicationsListener.reloadSyndications();
		}
		else if (key.compareTo("pref_sort_categories") == 0) {
			categoriesListener.reloadCategories();
		}
		else if (key.compareTo("pref_search_publication_time") == 0) {
			mamageRefreshPublicationTimer();
		}
		else if (key.compareTo("pref_delete_all_publications") == 0) {
			Log.e(SolnRss.this.getClass().getName(), "DELETE ALL PUBLICATIONS ASKED");
			refreshPublications();
		}
	}

	public void mamageRefreshPublicationTimer() {

		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

		int timeInMinute = pref.getInt("pref_search_publication_time", 15);

		Intent intent = new Intent(this, PublicationsFinderService.class);
		
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);

		AlarmManager alarmManager = (AlarmManager) this	.getSystemService(Context.ALARM_SERVICE);

		if (timeInMinute == 0) {
			// Stop the search
			alarmManager.cancel(pendingIntent);			
		} else {

			long lastRefreshTime = pref.getLong("publicationsLastRefresh", 0);
			long nextRefreshTime = SystemClock.elapsedRealtime() + 10000;
			long timeInMili = 60000 * timeInMinute;

			if (lastRefreshTime > 0) {
				nextRefreshTime = Math.max(nextRefreshTime, lastRefreshTime
						+ timeInMili);
			}
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, nextRefreshTime, timeInMili, pendingIntent);
		}
	}

	private boolean displayAlreadyReadPublications() {
		return PreferenceManager.getDefaultSharedPreferences(this)
					.getBoolean("pref_display_unread", true);
	}
	
	private void removeNotification(){
		NotificationManager notificationManager = 
				(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancel(0x000001);
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		Log.e("SolnRss", "CREATE ACTIVITY ID " + this.toString());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_soln_rss);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		//actionBar.setIcon(null);
		
		int apiVersion = android.os.Build.VERSION.SDK_INT;
		if (apiVersion >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			actionBar.setBackgroundDrawable(new ColorDrawable(0xeeeeee));
			actionBar.setStackedBackgroundDrawable(new ColorDrawable(0xeeeeee));
		}
		
		// Set up the ViewPager with the sections adapter.
		viewPager = (ViewPager) findViewById(R.id.pager);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.		
		sectionPageAdapter = new SectionsPagerAdapter(getFragmentManager(), getResources());
		viewPager.setAdapter(sectionPageAdapter);
		viewPager.setOffscreenPageLimit(3);
		
		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});
		
		actionBar.addTab(actionBar.newTab().setIcon(R.drawable.ic_tab_folder)
				.setTabListener(this));
		actionBar.addTab(actionBar.newTab().setIcon(R.drawable.ic_tab_file)
				.setTabListener(this));
		actionBar.addTab(actionBar.newTab().setIcon(R.drawable.ic_tab_earth)
				.setTabListener(this));
		
		actionBar.setTitle(Html.fromHtml("<b><u>" + getTitle() + "</u><b>"));

		PreferenceManager.getDefaultSharedPreferences(this)
				.registerOnSharedPreferenceChangeListener(this);
		
		removeNotification();
		viewPager.setCurrentItem(1);
		mamageRefreshPublicationTimer();
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
			
		case R.id.menu_already_read:
			 updateOptionDisplayPublicationsAlreadyRead();
			return true;
			
		case R.id.menu_add_site:	
			
			if (SyndicationFinderService.isAlreadyRunning == 1) {
				Toast.makeText(SolnRss.this, 
						"Service is already running", Toast.LENGTH_LONG).show();
			} else {
				openDialogForAddSyndication();
			}
			return true;
			
		case R.id.menu_display_all:
			reLoadAllPublications();
			return true;
			
		case R.id.menu_all_read:
			markAllPublicationsAsRead();
			return true;
		
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void updateOptionDisplayPublicationsAlreadyRead() {

		Boolean isShow = displayAlreadyReadPublications() ? false : true;
		
		SharedPreferences.Editor editor = 
				PreferenceManager.getDefaultSharedPreferences(this).edit();
		editor.putBoolean("pref_display_unread", isShow);
		editor.commit();
	}
	
	void openDialogForAddCategorie() {
		displayAddItemDialog(AddItemDialog.Item.Category);
	}

	void openDialogForAddSyndication() {
		displayAddItemDialog(AddItemDialog.Item.Site);
	}

	public void openDialogForAddSyndication(View v) {
		displayAddItemDialog(AddItemDialog.Item.Site);
	}
	
	public void openDialogForAddCategory(View v) {
		displayAddItemDialog(AddItemDialog.Item.Category);
	}
	
	void displayAddItemDialog(AddItemDialog.Item item) {
		
		AddItemDialog dialog = new AddItemDialog();
		Bundle args = new Bundle();
		args.putString("item", item.toString());
		dialog.setArguments(args);
		dialog.show(getFragmentManager(), "dialog_add_item");
	}
	
	@Override
	public void onFinishEditDialog(CharSequence seq, AddItemDialog.Item item) {
		switch (item) {
		case Site:
			addSyndication(seq.toString());
			break;

		case Category:
			addCategorie(seq.toString());
			break;

		default:
			break;
		}
	}
	
	/**
	 * All unread publication set to read
	 */
	private void markAllPublicationsAsRead() {
		publicationsListener.markAllPublicationsAsRead();
	}
	
	void addCategorie(String newCatgorie) {
		if (!TextUtils.isEmpty(newCatgorie)) {
			categoriesListener.addCategorie(this, newCatgorie);
		}
	}
	
	public void addSyndication(String url) {

		if (!isOnline()) {
			warmUser(getResources().getString(R.string.no_connection));
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
		
		/*SyndicationFinderTask task = new SyndicationFinderTask(this,getResources());
		task.execute(url);*/

		Intent intent = new Intent(this, SyndicationFinderService.class);
		intent.setAction("REGISTER_RECEIVER");
		intent.putExtra("ResultReceiver", resultReceiver);
		intent.putExtra("ResultReceiver_ID", hashCode());
		intent.putExtra("url", url);
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		
		try {
			pendingIntent.send();
		} catch (CanceledException ce) {
			ce.printStackTrace();
		}
	}
	
	private ResultReceiver resultReceiver = new ResultReceiver(new Handler()) {
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			
			// Refresh the syndication tab
			refreshSyndications();
			
			Integer newPublicationsNumber = resultData.getInt("newPublicationsNumber");
			String newSyndicationName = resultData.getString("newSyndicationName");
			
			//String text = "Found RSS feed for " + newSyndicationName + " with " + newPublicationsNumber + " new publications.";
			//getResources().getString(R.string.process_ok, newSyndicationName, newPublicationsNumber);
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

	@Override
	public void onTabUnselected(Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(Tab tab, 
			FragmentTransaction fragmentTransaction) {
	}
	
	public void reLoadPublicationsBySyndication(Integer syndicationID) {
		publicationsListener.reLoadPublicationsBySyndication(syndicationID);
		viewPager.setCurrentItem(1);
		publicationsListener.moveListViewToTop();
	}

	public void reLoadPublicationsByCategorie(Integer categorieID) {
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

	public void displaySyndications() {
		syndicationsListener.loadSyndications();
		viewPager.setCurrentItem(2);
		reLoadAllPublications();
	}

	public void refreshSyndications() {
		syndicationsListener.reloadSyndications();
	}
	
	public static enum SERVICE_RESULT {
		NEW_SYNDICATION,
		NEW_PUBLICATIONS
	}
	
	/**
	 * Call by a click on notification for refresh publication list
	 */
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	    setIntent(intent);
	    
		Bundle b = getIntent().getExtras();
		SERVICE_RESULT serviceResult = (SERVICE_RESULT) b.getSerializable("SERVICE_RESULT");
		
		if (serviceResult.compareTo(SERVICE_RESULT.NEW_SYNDICATION) == 0) {
			String newSyndicationId = intent.getStringExtra("newSyndicationId");
			if (newSyndicationId != null) {
				reLoadPublicationsBySyndication(Integer.valueOf(newSyndicationId));
			}
		} else if (serviceResult.compareTo(SERVICE_RESULT.NEW_PUBLICATIONS) == 0) {
			reLoadAllPublications();
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
	
	public boolean isOnline() {
	    ConnectivityManager cm = (ConnectivityManager) 
	        this.getSystemService(Context.CONNECTIVITY_SERVICE);

	    return cm.getActiveNetworkInfo() != null && 
	       cm.getActiveNetworkInfo().isConnectedOrConnecting();
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
	
}
