package free.solnRss.service;


import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;


public class ServiceConnector<T> implements ServiceConnection {
	private T		service;
	private Boolean	isBound	= false;

	public ServiceConnector() {

	}

	public Boolean isBound() {
		return isBound;
	}

	public void setUnBound() {
		isBound = false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onServiceConnected(final ComponentName className, final IBinder ibinder) {
		service = ((ServiceBinder<T>) ibinder).getService();
		isBound = true;
	}

	@Override
	public void onServiceDisconnected(final ComponentName className) {
		service = null;
		isBound = false;
	}

	public T getService() {
		return service;
	}

	public void setSrvice(final T fs) {
		this.service = fs;
	}
}
