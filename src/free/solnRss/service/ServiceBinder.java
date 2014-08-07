package free.solnRss.service;

import android.os.Binder;

/**
 * For bind service with an activity
 * 
 * @author jftomasi
 *
 * @param <S>
 */
public class ServiceBinder<S> extends Binder {

	protected String TAG = "LocalBinder";
	private S service;

	public void localBinder(S service) {
		this.service = service;
	}

	public S getService() {
		return this.service;
	}

}
