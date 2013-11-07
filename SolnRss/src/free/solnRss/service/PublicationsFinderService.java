package free.solnRss.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.SparseArray;
import free.solnRss.R;
import free.solnRss.activity.SolnRss;
import free.solnRss.business.SyndicationBusiness;
import free.solnRss.business.impl.SyndicationBusinessImpl;
import free.solnRss.manager.UpdatingProcessConnectionManager;
import free.solnRss.model.Publication;
import free.solnRss.model.Syndication;
import free.solnRss.provider.SolnRssProvider;
import free.solnRss.repository.PublicationRepository;
import free.solnRss.repository.PublicationTable;
import free.solnRss.repository.SyndicationRepository;
import free.solnRss.repository.SyndicationTable;

public class PublicationsFinderService extends IntentService {

	private final Uri uri = Uri.parse(SolnRssProvider.URI + "/publication");
	
	private SparseArray<ResultReceiver> receiverMap = new SparseArray<ResultReceiver>();
	private int resultReceiverId = -1;
	private SyndicationBusiness syndicationBusiness = new SyndicationBusinessImpl();
	private final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",	Locale.FRENCH);
	private boolean isWorking = false;
	
	private PublicationRepository publicationRepository;
	private SyndicationRepository syndicationRepository;
	
	//private ImageToDataTool imageToDataTool = new ImageToDataTool();
	//private ReadabilityUtil readabilityUtil = new ReadabilityUtil();

	public PublicationsFinderService() {
		super("PublicationsRefresh");
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		registerOrUnregisterReceiver(intent);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		publicationRepository = new PublicationRepository(getApplicationContext());
		syndicationRepository = new SyndicationRepository(getApplicationContext());
		
		List<Syndication> syndications = findSyndicationsToRefresh();
		refreshPublications(syndications);

		SharedPreferences pref = this.getSharedPreferences(getPackageName()
				+ "_preferences", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putLong("publicationsLastRefresh", Calendar.getInstance()
				.getTimeInMillis());
		editor.commit();
	}
	
	public void test(Context context){
		publicationRepository = new PublicationRepository(context);
		syndicationRepository = new SyndicationRepository(context);
		
		List<Syndication> syndications = findSyndicationsToRefresh();
		refreshPublications(syndications);
	}

	private void registerOrUnregisterReceiver(Intent intent) {
		if (intent == null) {
			receiverMap = new SparseArray<ResultReceiver>();
			
		} else if ("UNREGISTER_RECEIVER".equals(intent.getAction())) {
			// Extract the ResultReceiver ID and remove it from the map
			resultReceiverId = intent.getIntExtra("ResultReceiver_ID", 0);
			receiverMap.remove(resultReceiverId);
			
		} else if ("REGISTER_RECEIVER".equals(intent.getAction())) {
			// Extract the ResultReceiver and store it into the map
			ResultReceiver receiver = intent
					.getParcelableExtra("ResultReceiver");
			resultReceiverId = intent.getIntExtra("ResultReceiver_ID", 0);
			receiverMap.put(resultReceiverId, receiver);
		}
	}

	private List<Syndication> findSyndicationsToRefresh() {
		
		Cursor cursor = syndicationRepository.findSyndicationsToRefresh();
		
		List<Syndication> syndications = new ArrayList<Syndication>();
		Syndication syndication = null;
		
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			do {
				syndication = new Syndication();
				syndication.setId(cursor.getInt(cursor.getColumnIndex(SyndicationTable.COLUMN_ID))); 
				syndication.setName(cursor.getString(cursor.getColumnIndex(SyndicationTable.COLUMN_NAME)));
				syndication.setOldRss(cursor.getString(cursor.getColumnIndex(SyndicationTable.COLUMN_LAST_RSS_PUBLISHED)));
				syndication.setUrl(cursor.getString(cursor.getColumnIndex(SyndicationTable.COLUMN_URL)));
				syndications.add(syndication);
			} while (cursor.moveToNext());
			
			cursor.close();
		}
		return syndications;
	}

	private void refreshPublications(List<Syndication> syndications) {

		if (!UpdatingProcessConnectionManager
				.canUseConnection(getApplicationContext())
				|| syndications.size() <= 0 || isWorking) {
			return;
		}
		
		try {
			isWorking = true;
			for (Syndication syndication : syndications) {
				if (UpdatingProcessConnectionManager.canUseConnection(getApplicationContext())) {
					// Log.e(this.getClass().getName(),"Get new publications for syndication name "+
					// syndication.getName());
					findNewPublication(syndication);
				} else {
					break;
				}
			}

			List<ContentValues> contentValues = new ArrayList<ContentValues>();
			ContentValues values = null;

			for (Syndication syndication : syndications) {
				for (Publication publication : syndication.getPublications()) {
					boolean isAlreadyRecorded = false;

					if (!TextUtils.isEmpty(syndication.getOldRss())) {
						try {
							isAlreadyRecorded = syndication
									.isPublicationAlreadyRecorded(
											publication.getTitle(),
											publication.getUrl());
						} catch (Exception e) {
							// In case of error keep old method who's search in
							// database
							isAlreadyRecorded = isPublicationAlreadyRecorded(
									syndication.getId(),
									publication.getTitle(),
									publication.getUrl());
						}

					} else {
						isAlreadyRecorded = isPublicationAlreadyRecorded(
								syndication.getId(), publication.getTitle(),
								publication.getUrl());
					}

					if (!isAlreadyRecorded) {
						values = addNewPublication(syndication.getId(), publication);
						contentValues.add(values);
					}
				}
			}

			if (contentValues.size() > 0) {
				notifyNewPublications(insertNewPublications(contentValues));
			}

			// Must refresh syndication (update last extract date and rss)
			updateLastUpdateSyndicationTime(syndications);

		} finally {
			isWorking = false;
		}
	}

	private void updateLastUpdateSyndicationTime(List<Syndication> syndications) {
		syndicationRepository.updateLastUpdateSyndicationTime(syndications);
	}

	private void findNewPublication(Syndication syndication) {
		try {
			// Get the new rss
			syndication = syndicationBusiness.getLastPublications(syndication);
		} catch (Exception e) {
			/*
			 * Log.e("LoadArticlesService", "Error when trying to refresh " +
			 * syndication.getName() + " - " + e.getCause());
			 */
			// TODO set this syndication in error
		}
	}

	private int insertNewPublications(List<ContentValues> publications) {
		return getContentResolver().bulkInsert(uri,
				publications.toArray(new ContentValues[publications.size()]));
	}

	/*
	 * Make all fix needed by the publication's description
	 */
	private String makeFixInPublication(String description) {
		if (TextUtils.isEmpty(description)) {
			return description;
		}
		return replaceBadYoutubeUrlInIframe(description);
	}

	private String replaceBadYoutubeUrlInIframe(String description) {
		return description.replaceAll("src=\"//www.youtube.com/embed",
				"src=\"http://www.youtube.com/embed");
	}

	private ContentValues addNewPublication(Integer syndicationId, Publication publication) {
		
		ContentValues values = new ContentValues();
		values.put(PublicationTable.COLUMN_SYNDICATION_ID, syndicationId);
		values.put(PublicationTable.COLUMN_LINK, publication.getUrl());
		values.put(PublicationTable.COLUMN_PUBLICATION, getReadable(syndicationId, publication));
		values.put(PublicationTable.COLUMN_TITLE, publication.getTitle());
		values.put(PublicationTable.COLUMN_ALREADY_READ, 0);
		values.put(PublicationTable.COLUMN_PUBLICATION_DATE,sdf.format(new Date()));

		return values;
	}

	private String getReadable(Integer syndicationId, Publication publication) {
		
		String toRead = null;
		toRead = makeFixInPublication(publication.getDescription());
		//toRead = imageToDataTool.replaceImageByBase64Data(toRead);
		return toRead;
	}
	
	private boolean isPublicationAlreadyRecorded(Integer syndicationId,
			String title, String url) {
		return publicationRepository.isPublicationAlreadyRecorded(
				syndicationId, title, url);
	}

	private void notifyNewPublications(Integer newPublicationsNumber) {
		if (newPublicationsNumber > 0 && mustDisplayNotification()) {
			notificationForNewPublications(newPublicationsNumber);
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
