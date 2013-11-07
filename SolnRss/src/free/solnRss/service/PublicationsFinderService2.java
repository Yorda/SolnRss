package free.solnRss.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.SparseArray;

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntry;

import free.solnRss.R;
import free.solnRss.activity.SolnRss;
import free.solnRss.business.SyndicationBusiness;
import free.solnRss.business.impl.SyndicationBusinessImpl;
import free.solnRss.exception.ExtractFeedException;
import free.solnRss.provider.SolnRssProvider;
import free.solnRss.repository.PublicationRepository;
import free.solnRss.repository.PublicationTable;
import free.solnRss.repository.RssRepository;
import free.solnRss.repository.RssTable;
import free.solnRss.repository.SyndicationRepository;
import free.solnRss.repository.SyndicationTable;
import free.solnRss.utility.StringUtil;

public class PublicationsFinderService2 extends IntentService {

	private final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRENCH);
	private SyndicationBusiness syndicationBusiness = new SyndicationBusinessImpl();
	
	private PublicationRepository publicationRepository;
	private SyndicationRepository syndicationRepository;
	private RssRepository rssRepository;

	private SharedPreferences sharedPreferences;
	private SparseArray<String> syndications = new SparseArray<String>();
	
	private List<SyndEntry> syndEntries = new ArrayList<SyndEntry>();
	private ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
	
	private Date updateDate;
	
	public PublicationsFinderService2() {
		super("PublicationsFinderService2");
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {

		init(getApplicationContext());

		findSyndicationsToRefresh();

		getNewRss(getApplicationContext());

		updateLastRefreshTime();
	}
	
	public void init(Context context) {
		publicationRepository = new PublicationRepository(context);
		syndicationRepository = new SyndicationRepository(context);
		sharedPreferences = getSharedPreferences(getPackageName() + "_preferences", Context.MODE_PRIVATE);
	}
	
	private void getNewRss(Context context) {
		
		int newPublicationsRecorded = 0;
		
		updateDate = new Date();
		String updateDateFormat = sdf.format(updateDate);
		
		int key = -1;
		String url = null;
		
		operations.clear();
		syndEntries.clear();
		
		for (int i = 0, nsize = syndications.size(); i < nsize; i++) {
			
			key = syndications.keyAt(i);
			url = syndications.get(key);
			
			try {
				syndEntries = syndicationBusiness.newRssPublished(url);
			} catch (ExtractFeedException fe) {
				// Set this syndication in error
				continue;
			}
			
			for(SyndEntry syndEntry : syndEntries){
				// Load all recorded RSS info for this syndication
				if (!rssRepository.isAlreadyRetrieved(syndEntry.getTitle(), syndEntry.getLink())) {
					
					operations.add(ContentProviderOperation.newInsert(
							Uri.parse(SolnRssProvider.URI + "/publication"))
							.withValues(addNewPublication(key, syndEntry, updateDateFormat))
							.withYieldAllowed(true).build());
					
					newPublicationsRecorded++;
				}
			}
			
			operations.add(ContentProviderOperation.newDelete(Uri.parse(SolnRssProvider.URI + "/rss"))
					.withValue("syn_syndication_id", key)
					.withYieldAllowed(true).build());
			
			for(SyndEntry syndEntry : syndEntries) {
				
				operations.add(ContentProviderOperation.newInsert(Uri.parse(SolnRssProvider.URI + "/rss"))
						.withValue(RssTable.COLUMN_URL, syndEntry.getLink())
						.withValue(RssTable.COLUMN_TITLE, syndEntry.getTitle())
						.withValue(RssTable.COLUMN_SYNDICATION_ID, key)
						.withYieldAllowed(true).build());
			}
			
			// Update last refreshTime for syndication
			operations.add(ContentProviderOperation.newUpdate(Uri.parse(SolnRssProvider.URI + "/syndication"))
					.withValue(SyndicationTable.COLUMN_LAST_EXTRACT_TIME, updateDateFormat)
					.withSelection(SyndicationTable.COLUMN_ID,
							new String[] { Integer.valueOf(key).toString() })
					.withYieldAllowed(true).build());
		}
		
		// Run the batch
		try {
			context.getContentResolver().applyBatch(SolnRssProvider.AUTHORITY, operations);
		} catch (OperationApplicationException e) {
			newPublicationsRecorded = 0;
		} catch (RemoteException r) {
			newPublicationsRecorded = 0;
		}
		
		// Notify
		if (newPublicationsRecorded > 0 && mustDisplayNotification()) {
			notificationForNewPublications(newPublicationsRecorded);
		}
	}

	private ContentValues addNewPublication(Integer syndicationId, SyndEntry syndEntry, String date) {
		
		ContentValues values = new ContentValues();
		values.put(PublicationTable.COLUMN_SYNDICATION_ID, syndicationId);
		values.put(PublicationTable.COLUMN_LINK, syndEntry.getLink());
		values.put(PublicationTable.COLUMN_PUBLICATION, makeSomeFixInDescription(syndEntry.getDescription().getValue()));
		values.put(PublicationTable.COLUMN_TITLE, StringUtil.unescapeHTML(syndEntry.getTitle()));
		values.put(PublicationTable.COLUMN_ALREADY_READ, 0);
		values.put(PublicationTable.COLUMN_PUBLICATION_DATE, date);

		return values;
	}
	
	protected void findSyndicationsToRefresh() {
		syndications.clear();
		Cursor cursor = syndicationRepository.findSyndicationsToRefresh2(timeToRefresh());
		
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			do {				
				syndications.put(cursor.getInt(0), cursor.getString(1));
			} while (cursor.moveToNext());
			
			cursor.close();
		}
	}
	
	private Date timeToRefresh() {
		int refresh = sharedPreferences.getInt("pref_search_publication_time",15);
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(new Date());
		calendar.add(Calendar.MINUTE, -refresh);
		return calendar.getTime();
	}

	private void updateLastRefreshTime() {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putLong("publicationsLastRefresh", Calendar.getInstance().getTimeInMillis());
		editor.commit();
	}
	
	protected int insertNewPublications(List<ContentValues> publications) {
		return publicationRepository.insertNewPublications(publications);
	}
	
	private String makeSomeFixInDescription(String description) {
		String fixedDescription = null;
		
		if (!TextUtils.isEmpty(description)) {
			fixedDescription = StringUtil.unescapeHTML(fixedDescription);
			fixedDescription = fixYouTubeLinkInIFrame(description);
		}
		return fixedDescription;
	}
	
	private String fixYouTubeLinkInIFrame(String description) {
		return description.replaceAll("src=\"//www.youtube.com/embed",
				"src=\"http://www.youtube.com/embed");
	}
	
	private void notificationForNewPublications(int newPublicationsNumber) {

		String text = getResources().getQuantityString(R.plurals.notify_new_pub_msg,
				newPublicationsNumber, newPublicationsNumber);

		// Create pending intent for going back on screen
		Intent intent = new Intent(this, SolnRss.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);

		intent.putExtra("SERVICE_RESULT",
				SolnRss.SERVICE_RESULT.NEW_PUBLICATIONS);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 3,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(getResources().getString(R.string.notify_new_pub_title))
				.setContentText(text)
				.setContentIntent(pendingIntent);
		
		Notification notification = builder.build();
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(0x000001, notification);
	}

	public boolean mustDisplayNotification() {
		return PreferenceManager.getDefaultSharedPreferences(
				getApplicationContext())
				.getBoolean("pref_display_notify", true);
	}
}
