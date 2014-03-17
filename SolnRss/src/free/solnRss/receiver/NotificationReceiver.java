package free.solnRss.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import free.solnRss.notification.NewPublicationsNotification;

public class NotificationReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		NewPublicationsNotification.NotifyEvent event = NewPublicationsNotification.NotifyEvent.detachFrom(intent);
		
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		
		// Notification for new publication found is deleted
		if (event.compareTo(NewPublicationsNotification.NotifyEvent.DELETE_NOTIFICATION) == 0) {
			// Remove data 
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putInt("newPublicationsRecorded", 0);
			editor.putString("newPublicationsRecordDate", null);
			editor.commit();
			Log.e(NotificationReceiver.class.getName(), "NOTIFICATION RECEIVER FOR DELETE NOTIFICATION");
		}
		if (event.compareTo(NewPublicationsNotification.NotifyEvent.RESTART_ACTIVITY) == 0) {
			Log.e(NotificationReceiver.class.getName(), "NOTIFICATION RECEIVER FOR RESTART ACTIVITY");
		}
	}
}
