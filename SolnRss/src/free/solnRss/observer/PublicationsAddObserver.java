package free.solnRss.observer;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

public class PublicationsAddObserver extends ContentObserver {

	public PublicationsAddObserver(Handler handler) {
		super(handler);
	}

	@Override
	public void onChange(boolean selfChange) {
		super.onChange(selfChange);
	}

	@Override
	public void onChange(boolean selfChange, Uri uri) {
		
	}
}
