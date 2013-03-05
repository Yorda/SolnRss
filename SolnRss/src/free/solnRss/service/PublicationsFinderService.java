package free.solnRss.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import model.Publication;
import model.Syndication;
import all.business.SyndicationBusiness;
import all.business.impl.SyndicationBusinessImpl;
import all.exception.ExtractFeedException;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import free.solnRss.R;
import free.solnRss.activity.SolnRss;
import free.solnRss.repository.PublicationRepository;
import free.solnRss.repository.SyndicationRepository;
import free.solnRss.tools.StringTools;

public class PublicationsFinderService extends Service {

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
	
	/**
	 * Check if device is connected to Internet
	 * @return
	 */
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
	}
}
