package free.solnRss.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.SparseArray;
import free.solnRss.R;
import free.solnRss.activity.SolnRss;
import free.solnRss.business.SyndicationBusiness;
import free.solnRss.business.impl.SyndicationBusinessImpl;
import free.solnRss.model.Publication;
import free.solnRss.model.Syndication;
import free.solnRss.provider.PublicationsProvider;
import free.solnRss.provider.SyndicationsProvider;
import free.solnRss.repository.PublicationTable;
import free.solnRss.repository.SyndicationTable;
import free.solnRss.tools.StringTools;

public class PublicationsFinderService extends Service implements
		SharedPreferences.OnSharedPreferenceChangeListener {

	final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRENCH);
	private boolean isWorking = false;
	
	private final ServiceBinder<PublicationsFinderService> binder = 
			new ServiceBinder<PublicationsFinderService>();
	
	@Override
	public void onCreate() {
		binder.localBinder(this);
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	private Timer timer = null;
	private TimerTask timerTask = null;
	
	private void createTimerTask() {
		timerTask = new TimerTask() {
			@Override
			public void run() {
				runService();
			}
		};
	}
	
	private long minuteToMiliSecond(int minutes) {
		long second = 1000;
		long oneMinute = 60 * second;
		return minutes * oneMinute;
	}
	
	private SparseArray<ResultReceiver> receiverMap = new SparseArray<ResultReceiver>();
	private int resultReceiverId = -1;
	
	private void registerOrUnregisterReceiver(Intent intent) {
		// 
		if(intent == null){
			receiverMap = new SparseArray<ResultReceiver>();
		}
		else if ("UNREGISTER_RECEIVER".equals(intent.getAction())) {
			// Extract the ResultReceiver ID and remove it from the map
			resultReceiverId = intent.getIntExtra("ResultReceiver_ID", 0);
			receiverMap.remove(resultReceiverId);
		}
		else if ("REGISTER_RECEIVER".equals(intent.getAction())) {
			// Extract the ResultReceiver and store it into the map
			ResultReceiver receiver = intent.getParcelableExtra("ResultReceiver");
			resultReceiverId = intent.getIntExtra("ResultReceiver_ID", 0);
			receiverMap.put(resultReceiverId, receiver);
		} 
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,	String key) {
		if (key.compareTo("pref_search_publication_time") == 0) {
			
			int time = PreferenceManager.getDefaultSharedPreferences(this)
					.getInt("pref_search_publication_time", 15);
			
			//Log.e(SolnRss.class.getName(), "SERVICE MUST MAKE SEARCH EVERY  " + time + " MINUTE ");
			
			// TODO stop search if new time is 0 and test
			if(time == 0){
				timerTask.cancel();
				timer.cancel();
				timer = null;
				timerTask = null;
			}else {
				timer = new Timer("Search RSS feed timer", true);
				createTimerTask();
				timer.schedule(timerTask, time, time);
			}
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		createTimerTask() ;
		
		int second = 1000;
		int minute = 60 * second;
		
		PreferenceManager.getDefaultSharedPreferences(this)
				.registerOnSharedPreferenceChangeListener(this);
		
		int minutes = PreferenceManager.getDefaultSharedPreferences(this)
				.getInt("pref_search_publication_time", 15);
		
		// If 0 don't start a timer task
		if(minutes > 0){
			long times = minuteToMiliSecond(minutes);
			timer = new Timer("Search RSS feed timer", true);
			timer.schedule(timerTask, minute,times);
		}
		
		registerOrUnregisterReceiver(intent);
		return super.onStartCommand(intent, flags, startId);
	}

	public void runService() {
		List<Syndication> syndications = findSyndicationsToRefresh(); 
		refreshPublications(syndications);
	}
	
	
	private List<Syndication> findSyndicationsToRefresh() {
		
		// Period in minute
		int refresh = PreferenceManager.getDefaultSharedPreferences(this).getInt(
				"pref_search_publication_time", 15);

		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime( new Date());
		calendar.add(Calendar.MINUTE, -refresh);

		String projection[] = new String[] {
				SyndicationTable.SYNDICATION_TABLE + "." + SyndicationTable.COLUMN_ID,
				SyndicationTable.SYNDICATION_TABLE + "." + SyndicationTable.COLUMN_URL, };

		String selection = "syn_last_extract_time < Datetime(?) and syn_is_active = ? ";
		String[] selectionArgs = new String[2];
		selectionArgs[0] = sdf.format(calendar.getTime());
		selectionArgs[1] = "0";

		Cursor cursor = getContentResolver().query(
				SyndicationsProvider.URI,
				projection, selection, selectionArgs,
				SyndicationTable.COLUMN_ID + " asc ");

		List<Syndication> syndications = new ArrayList<Syndication>();
		Syndication syndication = null;
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			do {
				syndication = new Syndication();
				syndication.setId(cursor.getInt(cursor
						.getColumnIndex(SyndicationTable.COLUMN_ID)));
				syndication.setUrl(cursor.getString(cursor
						.getColumnIndex(SyndicationTable.COLUMN_URL)));
				syndications.add(syndication);
			} while (cursor.moveToNext());
			cursor.close();
		}
		return syndications;
	}

	SparseArray<List<Publication>> map = new SparseArray<List<Publication>>();
	
	private void refreshPublications(List<Syndication> syndications) {

		if (!isOnline() || syndications.size() <= 0 || isWorking) {
			return;
		}
		try {
			isWorking = true;
			map.clear();
			for (Syndication syndication : syndications) {
				if (isOnline()) {
					Log.e(this.getClass().getName(),"Get new publications for syndication id "+ syndication.getId());
					findNewPublication(syndication);
				}
				else {
					break;
				}
			}
			
			List<ContentValues> arr = new ArrayList<ContentValues>();
			ContentValues values = null;
			
			List<Publication> publications = null;
			for (int i = 0; i < map.size(); i++) {
				
				int syndicationId = map.keyAt(i);
				publications = map.get(syndicationId);
				
				for (Publication publication : publications){
					
					boolean isAlreadyRecorded = isPublicationAlreadyRecorded(
							syndicationId, publication.getTitle(),publication.getUrl());
					
					if (!isAlreadyRecorded) {
						values = addNewPublication(syndicationId,publication);
						arr.add(values);
					}
				}
			}
			
			int newInserted = 0;
			if(arr.size() > 0){
				newInserted = insertNewPublications(arr);
				notifyNewPublications(newInserted);
			}
			
		} finally {
			isWorking = false;
		}
	}

	private int insertNewPublications(List<ContentValues> publications) {
		return getContentResolver().bulkInsert(PublicationsProvider.URI,
				publications.toArray(new ContentValues[publications.size()]));
	}
	
	private ContentValues addNewPublication(Integer syndicationId, Publication publication){
		ContentValues values = new ContentValues();
		values.put(PublicationTable.COLUMN_SYNDICATION_ID, syndicationId);
		values.put(PublicationTable.COLUMN_LINK, publication.getUrl());
		values.put(PublicationTable.COLUMN_PUBLICATION, publication.getDescription());
		values.put(PublicationTable.COLUMN_TITLE, publication.getTitle());
		values.put(PublicationTable.COLUMN_ALREADY_READ, 0);
		
		values.put(PublicationTable.COLUMN_PUBLICATION_DATE,sdf.format(new Date()));
		
		/*
		if (publication.getPublicationDate() == null) {
			values.put(PublicationTable.COLUMN_PUBLICATION_DATE,
					sdf.format(new Date()));
		} else {
			values.put(PublicationTable.COLUMN_PUBLICATION_DATE,
					sdf.format(publication.getPublicationDate()));
		}*/
		
		return values;
	}
	
	private boolean isPublicationAlreadyRecorded(Integer syndicationId, String title, String url) {
		Uri uri = Uri.parse(PublicationsProvider.URI + "/publicationInSyndication/" + syndicationId);
		Cursor cursor = getContentResolver().query(
				uri,
				new String[] { PublicationTable.PUBLICATION_TABLE + "." + PublicationTable.COLUMN_ID },
				PublicationTable.COLUMN_TITLE + " = ? and "
						+ PublicationTable.COLUMN_LINK + " = ? ",
				new String[] { 
						title,
						url 
					}, 
				null);
		boolean isAlreadyRecorded = true;
		if (cursor.getCount() < 1) {
			isAlreadyRecorded = false;
		}
		cursor.close();
		return isAlreadyRecorded;
	}
	
	private SyndicationBusiness syndicationBusiness = new SyndicationBusinessImpl();
	
	private void findNewPublication(Syndication syndication) {

		try {
			// Get the new rss
			List<Publication>publications = syndicationBusiness.getLastPublications(syndication.getUrl());
			
			// Escape HTML
			for (Publication p : publications) {
				p.setTitle(StringTools.unescapeHTML(p.getTitle()));
			}
			
			map.put(syndication.getId(), publications);

		} catch (Exception e) {
			Log.e("LoadArticlesService", 
					"Error when trying to refresh "	+ syndication.getId() + " - " + e.getCause());
		}
	}
	
	private void notifyNewPublications(Integer newPublicationsNumber) {
		if (newPublicationsNumber > 0 && mustDisplayNotification()) {
			createNotification(newPublicationsNumber);
		}
		
		if (receiverMap.get(resultReceiverId) != null) {
			ResultReceiver resultReceiver = receiverMap.get(resultReceiverId);
			int resultCode = 0;
			Bundle resultData = new Bundle();
			resultData.putInt("newPublicationsNumber", newPublicationsNumber);
			resultReceiver.send(resultCode, resultData);
		}
	}	
	
	/**
	 * Create the notification for warm user that new publications are found
	 * 
	 * @param newPublicationsNumber
	 */
	private void createNotification(int newPublicationsNumber) {
		Resources r = getResources();
		String title = r.getString(R.string.notify_new_pub_title);

		String text = 
			String.format(r.getString(R.string.notify_new_pub_msg), newPublicationsNumber);

		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setSmallIcon(R.drawable.ic_launcher);

		builder.setContentTitle(title);
		builder.setContentText(text);

		// Create pending intent for going back on screen
		Intent intent = new Intent(this, SolnRss.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);

		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
		builder.setContentIntent(pendingIntent);

		Notification notification = builder.build();

		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(0x000001, notification);
	}
	
	/**
	 * Check if device is connected to Internet
	 * @return
	 */
	public boolean isOnline() {
	    ConnectivityManager cm = (ConnectivityManager) 
	        getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
	    return cm.getActiveNetworkInfo() != null && 
	       cm.getActiveNetworkInfo().isConnected();
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}
	
	public boolean mustDisplayNotification() {
		return PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
				.getBoolean("pref_display_notify", true);
	}
}
