package free.solnRss.observer;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import free.solnRss.fragment.PublicationsFragment;

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
		
	}
}
