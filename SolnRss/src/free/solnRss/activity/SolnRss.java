package free.solnRss.activity;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;
import free.solnRss.R;
import free.solnRss.adapter.SectionsPagerAdapter;
import free.solnRss.dialog.AddItemDialog;
import free.solnRss.dialog.AddItemDialog.NewAddItemDialogListener;
import free.solnRss.fragment.listener.CategoriesFragmentListener;
import free.solnRss.fragment.listener.PublicationsFragmentListener;
import free.solnRss.fragment.listener.SyndicationsFragmentListener;
import free.solnRss.task.SyndicationFinderTask;

public class SolnRss extends FragmentActivity implements ActionBar.TabListener,
		SharedPreferences.OnSharedPreferenceChangeListener,
		NewAddItemDialogListener {
	
	private SyndicationsFragmentListener syndicationsListener;
	private CategoriesFragmentListener categoriesListener;
	private PublicationsFragmentListener publicationsListener;

	private SectionsPagerAdapter sectionPageAdapter;
	private ViewPager viewPager;
	private SearchView searchView;
	
	private String filterText;
	
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
			publicationsListener.refreshPublications(this);
		}

		if (key.compareTo("pref_display_unread") == 0) {
			publicationsListener.refreshPublications(this);
		}
	}

	private void removeNotification(){
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
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

		// Set up the ViewPager with the sections adapter.
		viewPager = (ViewPager) findViewById(R.id.pager);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		sectionPageAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), getResources());
		viewPager.setAdapter(sectionPageAdapter);
		
		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < sectionPageAdapter.getCount(); i++) {
			actionBar.addTab(actionBar.newTab()
					.setText(sectionPageAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		  
		// Start the service for retrieve new publications
		//Intent service = new Intent(this, PublicationsCursorCacheService.class);
		//startService(service);
		
		PreferenceManager
				.getDefaultSharedPreferences(this)
				.registerOnSharedPreferenceChangeListener(this);
		
		removeNotification();
		viewPager.setCurrentItem(1);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case RESULT_OK:
			if (data.hasExtra("refresh")) {
				ActivityResult action = 
					ActivityResult.valueOf(data.getExtras().getString("refresh"));
				
				switch (action) {
				case DELETE:
					String deleteMsg = getResources().getString(R.string.delete_ok);
					Toast.makeText(this, deleteMsg, Toast.LENGTH_LONG).show();
					reLoadAllPublications();
					displaySyndications(null);
					break;

				case CLEAN:
					reLoadAllPublications();
					break;
				default:
					break;
				}
			}
			break;
		default:
			break;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_soln_rss, menu);
		MenuItem searchItem = menu.findItem(R.id.action_search);
		
		searchView = (SearchView) searchItem.getActionView();
		setupSearchView(searchItem);
		
		return true;
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
			
		case R.id.menu_add_site:
			openDialogForAddSyndication();
			return true;
			
		case R.id.menu_display_all:
			reLoadAllPublications();
			return true;
			
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	void openDialogForAddCategorie() {
		displayAddItemDialog(AddItemDialog.Item.Categorie);
	}

	void openDialogForAddSyndication() {
		displayAddItemDialog(AddItemDialog.Item.Site);
	}

	void displayAddItemDialog(AddItemDialog.Item item) {
		
		AddItemDialog dialog = new AddItemDialog();
		Bundle args = new Bundle();
		args.putString("item", item.toString());
		dialog.setArguments(args);
		dialog.show(getSupportFragmentManager(), "dialog_add_item");
	}
	
	@Override
	public void onFinishEditDialog(CharSequence seq, AddItemDialog.Item item) {
		switch (item) {
		case Site:
			addSyndication(seq.toString());
			break;

		case Categorie:
			addCategorie(seq.toString());
			break;

		default:
			break;
		}
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
		
		SyndicationFinderTask task = 
			new SyndicationFinderTask(this, getResources());
		task.execute(url);
	}
	
	@Override
	public void onTabSelected(Tab tab, 
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in the ViewPager.
		//Fragment fragment = null;
		switch(tab.getPosition()){
		case 0:
			if (categoriesListener != null) {
				categoriesListener.loadCategories(this);
			}
			break;
		case 2:
			if (syndicationsListener != null) {
				syndicationsListener.loadSyndications(this);
			}
			break;
		}
		viewPager.setCurrentItem(tab.getPosition());
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
		publicationsListener.reLoadPublicationsBySyndication(this, syndicationID);
		viewPager.setCurrentItem(1);
		publicationsListener.moveListViewToTop();
	}

	public void reLoadPublicationsByCategorie(Integer categorieID) {
		publicationsListener.reLoadPublicationsByCategorie(this, categorieID);
		viewPager.setCurrentItem(1);
		publicationsListener.moveListViewToTop();
	}
	
	public void reLoadAllPublications() {
		publicationsListener.reloadPublications(this);
		publicationsListener.moveListViewToTop();
	}
	
	public void reLoadAllPublications(View v){
		reLoadAllPublications();
	}
	
	public void displaySyndications(Integer syndicationID) {
		syndicationsListener.loadSyndications(this);
		viewPager.setCurrentItem(2);
	}	

	/**
	 * Call by a click on notification for refresh publication list
	 */
	protected void onNewIntent(Intent intent) {
		reLoadAllPublications();
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
	
	/*
	 * The search view in action bar 
	 */
	private void setupSearchView(MenuItem searchItem) {

		boolean isAlwaysExpanded = false;
		if (isAlwaysExpanded) {
			searchView.setIconifiedByDefault(false);
		} else {
			searchItem.setShowAsAction(
					MenuItem.SHOW_AS_ACTION_IF_ROOM
					| MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW
				);
		}
		
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				return false;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
				filterPublications(newText);
				return true;
			}
		});
	}
	
	/*
	 * Apply the text for filter in publication's list
	 */
	private void filterPublications(String text) {
		publicationsListener.filterPublications(text);
	}

	public CategoriesFragmentListener getCategoriesFragmentListener() {
		return categoriesListener;
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

	public PublicationsFragmentListener getPublicationsFragmentListener() {
		return publicationsListener;
	}

	public void setPublicationsFragmentListener(
			PublicationsFragmentListener publicationsFragmentListener) {
		this.publicationsListener = publicationsFragmentListener;
	}

	public String getFilterText() {
		return filterText;
	}

	public void setFilterText(String filterText) {
		this.filterText = filterText;
	}
	
	
/*public Fragment retrieveCategoriesFragment() {
	return getSupportFragmentManager().findFragmentByTag(
			SectionsPagerAdapter.getFragementTag(viewPager.getId(), 0));
}*/

/*public Fragment retrievePublicationsFragment() {
	return getSupportFragmentManager().findFragmentByTag(
			SectionsPagerAdapter.getFragementTag(viewPager.getId(), 1));
}*/

/*public Fragment retrieveSyndicationsFragment() {
	return getSupportFragmentManager().findFragmentByTag(
			SectionsPagerAdapter.getFragementTag(viewPager.getId(), 2));
}*/
}
