package free.solnRss.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.app.NotificationCompat;
import android.util.SparseArray;
import free.solnRss.R;
import free.solnRss.activity.SolnRss;
import free.solnRss.business.SyndicationBusiness;
import free.solnRss.business.impl.SyndicationBusinessImpl;
import free.solnRss.manager.UpdatingProcessConnectionManager;
import free.solnRss.model.Syndication;
import free.solnRss.notification.NewSyndicationNotification;
import free.solnRss.repository.SyndicationRepository;
import free.solnRss.utility.HttpUtil;

public class SyndicationFinderService extends IntentService {

	private SparseArray<ResultReceiver> receiverMap = new SparseArray<ResultReceiver>();
	private int resultReceiverId = -1;
	
	public static Integer isAlreadyRunning = 0;
	private final Integer numberOfSteps = 100;

	private SyndicationBusiness syndicationBusiness = new SyndicationBusinessImpl();
	private NotificationCompat.Builder builder;
	private NotificationManager notificationManager;
	
	public SyndicationFinderService(String name) {
		super(name);
	}

	public SyndicationFinderService() {
		super("SyndicationFinderService");
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
			ResultReceiver receiver = intent.getParcelableExtra("ResultReceiver");
			resultReceiverId = intent.getIntExtra("ResultReceiver_ID", 0);
			receiverMap.put(resultReceiverId, receiver);
		}
	}
	
	// Create the notification
	protected void createNotification() {

		builder = new NotificationCompat.Builder(this);
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		builder = new NotificationCompat.Builder(this);
		
		builder.setContentTitle(getString(R.string.load_syndication))
				.setContentText(getString(R.string.retrieve_http))  
				.setSmallIcon(R.drawable.ic_launcher);
		
		// Begin the download progress bar
		builder.setProgress(numberOfSteps, 0, false);
		
		Notification notification = builder.build();
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		 
		// Displays the progress bar for the first time.
		notificationManager.notify(0,notification);
	}

	private void processNotification(Integer level, String text) {
		if (level.compareTo(numberOfSteps) == 0) {
			builder.setContentText(text).setProgress(0, 0, false);
		} else {
			builder.setContentText(text).setProgress(numberOfSteps, level, false);
		}
		
		Notification notification = builder.build();
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		
		notificationManager.notify(0, notification);
	}

	// 1 - Is the URL is ok ?
	protected boolean isUrlIsright(String url) {
		if (!HttpUtil.isValidUrl(url)) {
			processNotification(numberOfSteps, 
					getString(R.string.bad_url));
			return false;
		}
		return true;
	}

	// 2 - Is already a syndication with this url ?
	protected boolean isAlreadyInDatabase(String url) {
		SyndicationRepository repository = new SyndicationRepository(getApplicationContext());

		if (repository.isStillRecorded(url)) {
			processNotification(numberOfSteps,
					getString(R.string.site_already_recorded));
			return true;
		}
		return false;
	}

	// 3 - Retrieve content
	protected String retrieveHttpContent(String url) {
		String contentFromHttp = null;
		try {
			contentFromHttp = HttpUtil.retrieveHtml(url);
			if (contentFromHttp == null) {
				processNotification(numberOfSteps,
						getString(R.string.feed_not_found));
			}
		} catch (Exception e) {
			processNotification(numberOfSteps,
					getString(R.string.feed_not_found));
		}
		return contentFromHttp;
	}

	// 4 - Search the RSS feed
	protected Syndication searchRssFeed(String html, String url) {
		Syndication syndication = null;
		try {
			syndication = syndicationBusiness.retrieveSyndicationContent(html,url);
			if (syndication == null) {
				processNotification(numberOfSteps,
						getString(R.string.feed_search_error));
			}
		} catch (Exception e) {
			processNotification(numberOfSteps,
					getString(R.string.feed_search_error));
		}
		return syndication;
	}

	// 5 - Record new syndication
	protected Long recordNewSyndication(Syndication syndication) {
		SyndicationRepository repository = new SyndicationRepository(
				getApplicationContext());

		Long newSyndicationId = null;
		try {
			newSyndicationId = repository.addWebSite(syndication);
		} catch (Exception e) {
			processNotification(numberOfSteps,
					getString(R.string.site_record_error));
		}
		return newSyndicationId;
	}

	// 6 - End process
	protected void refreshActivity(Syndication syndication) {
		notifyNewSyndication(syndication);
	}
	
	private void notifyNewSyndication(Syndication syndication) {
		
		if (receiverMap.get(resultReceiverId) != null) {
			ResultReceiver resultReceiver = receiverMap.get(resultReceiverId);
			int resultCode = 0;
			Bundle resultData = new Bundle();
			resultData.putInt("newSyndicationId", syndication.getId());
			resultData.putString("newSyndicationName", syndication.getName());
			resultData.putInt("newPublicationsNumber", syndication.getPublications() != null 
					? syndication.getPublications().size() : 0);
			resultReceiver.send(resultCode, resultData);
		}
		
		// Create pending intent for going back on screen
		Intent intent = new Intent(this, SolnRss.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		
		
		NewSyndicationNotification.NotifySyndicationEvent event = 
				NewSyndicationNotification.NotifySyndicationEvent.NEW_SYNDICATION;
		event.attachTo(intent);
		
		intent.putExtra("newSyndicationId", syndication.getId().toString());
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 2,intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		builder.setContentIntent(pendingIntent);
		builder.build();
		
		String syndicationName = syndication.getName();
		int numberPublicationsFound = 0;

		if (syndication.getPublications() != null) {
			numberPublicationsFound = syndication.getPublications().size();
		}

		String message = getResources().getString(R.string.process_ok, syndicationName, numberPublicationsFound);
		
		processNotification(numberOfSteps, message);
	}	
	
	@Override
	protected void onHandleIntent(Intent intent) {
		try {

			isAlreadyRunning = 1 ;
			
			registerOrUnregisterReceiver(intent);
			
			createNotification();

			// step = 0;
			String url = intent.getStringExtra("url");

			if (!UpdatingProcessConnectionManager
					.canUseConnection(getApplicationContext())) {
				return;
			}

			if (!isUrlIsright(url)) {
				return;
			}

			if (isAlreadyInDatabase(url)) {
				return;
			}

			processNotification(20, getString(R.string.searching_feed));

			String contentRetrievedFormUrl = retrieveHttpContent(url);
			if (contentRetrievedFormUrl == null) {
				return;
			}

			processNotification(50, getString(R.string.found_feed));

			Syndication syndication = searchRssFeed(contentRetrievedFormUrl, url);
			if (syndication == null) {
				return;
			}

			processNotification(70, getString(R.string.record_feed));
			Long newSyndicationId = recordNewSyndication(syndication);
			if (newSyndicationId == null) {
				return;
			}
			syndication.setId(newSyndicationId.intValue());

			refreshActivity(syndication);

		} catch (Exception e) {
			
		} finally {
			isAlreadyRunning = 0;
		}
	}
}
