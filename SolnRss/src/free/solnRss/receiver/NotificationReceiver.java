package free.solnRss.receiver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import free.solnRss.notification.NewPublicationsNotification;


public class NotificationReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, final Intent intent) {

		final NewPublicationsNotification.NotifyEvent event = NewPublicationsNotification.NotifyEvent.detachFrom(intent);

		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

		// Notification for new publication found is deleted
		if (event.compareTo(NewPublicationsNotification.NotifyEvent.DELETE_NOTIFICATION) == 0) {
			// Remove data 
			final SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putInt("newPublicationsRecorded", 0);
			editor.putString("newPublicationsRecordDate", null);
			editor.commit();
		}
	}
}
