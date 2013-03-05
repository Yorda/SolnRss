package free.solnRss.service;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

public class ServiceConnector<T> implements ServiceConnection {
	private T service;
	private Boolean isBound = false;

	public ServiceConnector() {
		
	}
	public Boolean isBound() {
		return isBound;
	}

	public void setUnBound() {
		isBound = false;
	}

	@SuppressWarnings("unchecked")
	public void onServiceConnected(ComponentName className, IBinder ibinder) {
		service = ((ServiceBinder<T>) ibinder).getService();
		isBound = true;
	}

	public void onServiceDisconnected(ComponentName className) {
		service = null;
		isBound = false;
	}

	public T getService() {
		return service;
	}

	public void setSrvice(T fs) {
		this.service = fs;
	}
}
