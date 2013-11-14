package free.solnRss.service;

import free.solnRss.business.PublicationFinderBusiness;
import free.solnRss.business.impl.PublicationFinderBusinessImpl;
import android.app.IntentService;
import android.content.Intent;
import android.os.ResultReceiver;
import android.util.SparseArray;

public class PublicationsFinderService extends IntentService {

	private PublicationFinderBusiness publicationFinderBusiness;
	private SparseArray<ResultReceiver> receiverMap = new SparseArray<ResultReceiver>();
	private int resultReceiverId = -1;

	public PublicationsFinderService() {
		super("PublicationsFinderService2");
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		registerOrUnregisterReceiver(intent);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		publicationFinderBusiness = new PublicationFinderBusinessImpl(getApplicationContext());
		publicationFinderBusiness.searchNewPublications();
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
}
