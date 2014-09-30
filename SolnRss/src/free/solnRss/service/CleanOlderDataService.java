package free.solnRss.service;

import free.solnRss.repository.PublicationRepository;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class CleanOlderDataService extends IntentService {

	public CleanOlderDataService(String name) {
		super(name);

	}
	
	public CleanOlderDataService() {
		super("CleanOlderDataService");
	}

	private PublicationRepository publicationRepository;

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.e(CleanOlderDataService.this.getClass().getName(), "CLEAN TOO OLD DATA");
		publicationRepository = new PublicationRepository(
				getApplicationContext());
		try {
			publicationRepository.removeTooOLdPublications();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
