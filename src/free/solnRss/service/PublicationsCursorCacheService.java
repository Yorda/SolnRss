package free.solnRss.service;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;

public class PublicationsCursorCacheService extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	Cursor cursor = null;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

}
