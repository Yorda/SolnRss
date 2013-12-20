package free.solnRss.observer;

import free.solnRss.fragment.PublicationsFragment;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

public class PublicationsAddObserver extends ContentObserver {

	PublicationsFragment publicationsFragment;

	public PublicationsAddObserver(Handler handler,
			PublicationsFragment publicationsFragment) {
		super(handler);
	}

	public PublicationsAddObserver(Handler handler) {
		super(handler);
	}

	@Override
	public void onChange(boolean selfChange) {
		super.onChange(selfChange);
	}

	@Override
	public void onChange(boolean selfChange, Uri uri) {
		Log.e(PublicationsAddObserver.class.getName(), "CALL THE OBSERVER, NEW PUBLICATIONS FOUND !");
	}
}
