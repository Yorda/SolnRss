package free.solnRss.activity;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import free.solnRss.R;
import free.solnRss.adapter.SectionsPagerAdapter;
import free.solnRss.alarmManager.FindNewPublicationsAlarmManager;
import free.solnRss.dialog.OneEditTextDialogBox;
import free.solnRss.fragment.listener.CategoriesFragmentListener;
import free.solnRss.fragment.listener.PublicationsFragmentListener;
import free.solnRss.fragment.listener.SyndicationsFragmentListener;
import free.solnRss.manager.UpdatingProcessConnectionManager;
import free.solnRss.service.SyndicationFinderService;

public class SolnRss extends Activity implements ActionBar.TabListener,
		SharedPreferences.OnSharedPreferenceChangeListener {
	
	public static enum SERVICE_RESULT {
		NEW_SYNDICATION, NEW_PUBLICATIONS
	}

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
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences shared, String key) {

		if (key.compareTo("pref_display_unread") == 0) {
			publicationsListener.refreshPublications();
		} 
		else if (key.compareTo("pref_delete_all_publications") == 0) {
			publicationsListener.refreshPublications();
		} 
		else if (key.compareTo("pref_max_publication_item") == 0) {
			publicationsListener.refreshPublications();
		} 
		else if (key.compareTo("pref_sort_syndications") == 0) {
			syndicationsListener.reloadSyndications();
		} 
		else if (key.compareTo("pref_sort_categories") == 0) {
			categoriesListener.reloadCategories();
		} 
	}

	public void registerPreferenceManager() {
		PreferenceManager.getDefaultSharedPreferences(this)
				.registerOnSharedPreferenceChangeListener(this);
		
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		
		FindNewPublicationsAlarmManager.createInstance(pref, this);
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
		
		super.onCreate(savedInstanceState);
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

		removeNotification();
		viewPager.setCurrentItem(1);
		
		registerPreferenceManager();
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
	public void onBackPressed() {
		super.onBackPressed();
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
	
	private void updateOptionDisplayPublicationsAlreadyRead() {

		Boolean isShow = displayAlreadyReadPublications() ? false : true;
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		editor.putBoolean("pref_display_unread", isShow);
		editor.commit();
	}
	
	private void openDialogForAddCategorie() {
		
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

	private void openDialogForAddSyndication() {
		
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
			categoriesListener.addCategorie(this, newCatgorie);
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
	
	/*public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm.getActiveNetworkInfo() != null
				&& cm.getActiveNetworkInfo().isConnectedOrConnecting();
	}*/
	

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
}
