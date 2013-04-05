package free.solnRss.service;

import free.solnRss.provider.PublicationsProvider;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;

public class PublicationsCursorCacheService extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	Cursor cursor = null;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		String selection = null;
		String[] args = null;
		
		Uri uri = PublicationsProvider.URI;
		
		cursor = getApplication().getContentResolver().query(uri, 
				PublicationsProvider.projection, selection, args, null);
		
		return super.onStartCommand(intent, flags, startId);
	}

}
