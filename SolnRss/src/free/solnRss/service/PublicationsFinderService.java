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
import android.content.res.Resources;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.IBinder;
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

public class PublicationsFinderService extends Service {
	private final ServiceBinder<PublicationsFinderService> binder = new ServiceBinder<PublicationsFinderService>();
	@Override
	public void onCreate() {
		binder.localBinder(this);
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}	
	final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRENCH);
	private SyndicationBusiness syndicationBusiness;

	private boolean isWorking = false;
	private final int notificationId = 0x010001;
	final String syndicationTable = SyndicationTable.SYNDICATION_TABLE;
		
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		syndicationBusiness = new SyndicationBusinessImpl();
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				runService();
			}
		};

		int second = 1000;
		int minute = 60 * second;
		int ten_minutes = minute * 10;
		Timer timer = new Timer();
		
		timer.schedule(timerTask, minute, ten_minutes);
		
		return super.onStartCommand(intent, flags, startId);
	}

	public void runService() {
		List<Syndication> syndications = findSyndicationsToRefresh(); 
		refreshPublications(syndications);
	}
	
	private List<Syndication> findSyndicationsToRefresh() {
		
		// Period in minute
		int refresh = 10;

		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime( new Date());
		calendar.add(Calendar.MINUTE, -refresh);

		String projection[] = new String[] {
				syndicationTable + "." + SyndicationTable.COLUMN_ID,
				syndicationTable + "." + SyndicationTable.COLUMN_URL, };

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

	private void refreshPublications(List<Syndication> syndications) {

		if (!isOnline() || syndications.size() <= 0 || isWorking) {
			return;
		}
		try {
			
			isWorking = true;
			SparseArray<List<Publication>> map = new SparseArray<List<Publication>>();
			
			for (Syndication syndication : syndications) {
				Log.e(this.getClass().getName(),"Get new publications for syndication id " + syndication.getId());
				map = findNewPublication(syndication, map);				
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
		if (publication.getPublicationDate() == null) {
			values.put(PublicationTable.COLUMN_PUBLICATION_DATE,
					sdf.format(new Date()));
		} else {
			values.put(PublicationTable.COLUMN_PUBLICATION_DATE,
					sdf.format(publication.getPublicationDate()));
		}	
		return values;
	}
	
	private boolean isPublicationAlreadyRecorded(Integer syndicationId, String title, String url) {
		Uri uri = Uri.parse(PublicationsProvider.URI + "/" + syndicationId);
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
	
	private SparseArray<List<Publication>> findNewPublication(Syndication syndication, SparseArray<List<Publication>> map) {

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
		return map;
	}
	
	private void notifyNewPublications(Integer newPublicationsNumber) {
		if (newPublicationsNumber > 0 && mustDisplaynotification()) {
			createNotification(newPublicationsNumber);
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
		notificationManager.notify(notificationId, notification);
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
	
	public boolean mustDisplaynotification() {
		return PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
				.getBoolean("pref_display_notify", true);
	}
	
	
	
	
	
	/*
	private SyndicationRepository syndicationRepository;
	private PublicationRepository publicationRepository;

	private SyndicationBusiness syndicationBusiness;
	private List<Publication> publications;
	
	private boolean isWorking = false;
	private final int notificationId = 0x010001;

	private final ServiceBinder<PublicationsFinderService> binder = new ServiceBinder<PublicationsFinderService>();

	@Override
	public void onCreate() {
		binder.localBinder(this);
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		syndicationRepository = 
			new SyndicationRepository(getApplicationContext());
		publicationRepository = 
			new PublicationRepository(getApplicationContext());
		syndicationBusiness = 
			new SyndicationBusinessImpl();
		publications = 
			new ArrayList<Publication>();

		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				runService();
			}
		};

		Timer timer = new Timer();
		timer.schedule(timerTask, 60000, 600000);
		return super.onStartCommand(intent, flags, startId);
	}

	public void runService() {
		Log.e(this.getClass().getName(), "Begin to run service for retrieve last publication");
		Log.e(this.getClass().getName(), "Retrieve syndications to refresh ");
		List<Syndication> syndications = 
			syndicationRepository.findSyndicationToUpdate();
		refreshPublications(syndications);
		Log.e(this.getClass().getName(), "End running service for retrieve last publication");

	}

	private void refreshPublications(List<Syndication> syndications) {

		if (!isOnline() || syndications.size() <= 0 || isWorking) {
			return;
		}
		try {
			
			isWorking = true;
			Integer newPublicationsNumber = 0;
			int n = 1 ;
			for (Syndication syndication : syndications) {
				Log.e(this.getClass().getName(), "Get new publications  for " + syndication.getId() + " number " + n + " / " +syndications.size() );
				newPublicationsNumber += findNewPublication(syndication);
				n++;
			}
			notifyNewPublications(newPublicationsNumber);
		} finally {
			isWorking = false;
		}
	}

	private Integer findNewPublication(Syndication syndication) {
		Integer numberOfnewArticles = 0;
		try {
			// Get the new rss
			publications = syndicationBusiness.getLastPublications(syndication.getUrl());
			
			// Escape HTML
			for (Publication p : publications) {
				p.setTitle(StringTools.unescapeHTML(p.getTitle()));
			}
			
			// Update articles with new one
			numberOfnewArticles += 
				publicationRepository.refresh(publications,syndication.getId(), numberOfnewArticles);
			publications.clear();
			// Set new Last update date to syndication
			syndicationRepository.updateLastExtractTime(syndication.getId());

		} catch (ExtractFeedException e) {
			Log.e("LoadArticlesService", "Error when trying to refresh " + syndication.getId());
			e.printStackTrace();
		}
		return numberOfnewArticles;
	}
	
	private void notifyNewPublications(Integer newPublicationsNumber) {
		if (newPublicationsNumber > 0 && mustDisplaynotification()) {
			createNotification(newPublicationsNumber);
		}
	}
	

	private void createNotification(int newPublicationsNumber) {
		Resources r = getResources();
		String title = r.getString(R.string.notify_new_pub_title);

		String text = 
			String.format(r.getString(R.string.notify_new_pub_msg), newPublicationsNumber);

		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		// builder.setSmallIcon(android.R.drawable.arrow_up_float);
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
		notificationManager.notify(notificationId, notification);
	}
	

	public boolean isOnline() {
	    ConnectivityManager cm = (ConnectivityManager) 
	        getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);

	    return cm.getActiveNetworkInfo() != null && 
	       cm.getActiveNetworkInfo().isConnectedOrConnecting();
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}
	
	public boolean mustDisplaynotification() {
		return PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
				.getBoolean("pref_display_notify", true);
	}*/
}
