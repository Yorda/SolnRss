package free.solnRss.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import free.solnRss.notification.NewPublicationsNotification;

public class NotificationReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		NewPublicationsNotification.NotifyEvent event = NewPublicationsNotification.NotifyEvent.detachFrom(intent);
		
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		
		if (event.compareTo(NewPublicationsNotification.NotifyEvent.DELETE_NOTIFICATION) == 0) {
			
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putInt("newPublicationsRecorded", 0);
			editor.putString("newPublicationsRecordDate", null);
			editor.commit();
		}
	}
}
