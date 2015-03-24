package free.solnRss.service;


import android.app.IntentService;
import android.content.Intent;
import android.os.ResultReceiver;
import android.support.v4.content.LocalBroadcastManager;
import android.util.SparseArray;
import free.solnRss.business.PublicationFinderBusiness;
import free.solnRss.business.impl.PublicationFinderBusinessImpl;
import free.solnRss.manager.UpdatingProcessConnectionManager;


public class PublicationsFinderService extends IntentService {

	private PublicationFinderBusiness	publicationFinderBusiness;
	private SparseArray<ResultReceiver>	receiverMap			= new SparseArray<ResultReceiver>();
	private int							resultReceiverId	= -1;

	public PublicationsFinderService() {
		super("PublicationsFinderService");
	}

	@Override
	public void onStart(final Intent intent, final int startId) {
		super.onStart(intent, startId);
		registerOrUnregisterReceiver(intent);
	}

	@Override
	protected void onHandleIntent(final Intent intent) {

		if (UpdatingProcessConnectionManager.canUseConnection(getApplicationContext())) {

			publicationFinderBusiness = new PublicationFinderBusinessImpl(getApplicationContext());
			publicationFinderBusiness.searchNewPublications();

			if (publicationFinderBusiness.getNewPublicationsRecorded() > 0) {

				final Intent broadcast = new Intent("newPublicationFound");
				broadcast.putExtra("newPublicationsRecorded", publicationFinderBusiness.getNewPublicationsRecorded());
				LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
			}

		}
	}

	private void registerOrUnregisterReceiver(final Intent intent) {
		if (intent == null) {
			receiverMap = new SparseArray<ResultReceiver>();
		} else if ("UNREGISTER_RECEIVER".equals(intent.getAction())) {
			// Extract the ResultReceiver ID and remove it from the map
			resultReceiverId = intent.getIntExtra("ResultReceiver_ID", 0);
			receiverMap.remove(resultReceiverId);

		} else if ("REGISTER_RECEIVER".equals(intent.getAction())) {
			// Extract the ResultReceiver and store it into the map
			final ResultReceiver receiver = intent.getParcelableExtra("ResultReceiver");
			resultReceiverId = intent.getIntExtra("ResultReceiver_ID", 0);
			receiverMap.put(resultReceiverId, receiver);
		}
	}
}
