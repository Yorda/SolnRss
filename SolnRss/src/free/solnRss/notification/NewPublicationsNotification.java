package free.solnRss.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import free.solnRss.R;
import free.solnRss.receiver.NotificationReceiver;

public class NewPublicationsNotification {

	public static enum NotifyEvent {
		RESTART_ACTIVITY, DELETE_NOTIFICATION;

		private static final String name = NotifyEvent.class.getName();

		public void attachTo(Intent intent) {
			intent.putExtra(name, ordinal());
		}

		public static NotifyEvent detachFrom(Intent intent) {
			if (!intent.hasExtra(name))
				throw new IllegalStateException();
			return values()[intent.getIntExtra(name, -1)];
		}
	}
	
	private Context context;
	
	public NewPublicationsNotification(Context context) {
		this.context = context;
	}
	
	public  void notificationForNewPublications(int newPublicationsNumber, String updateDate) {

		newPublicationsNumber = updateNewPublicationsNumberAndTime(newPublicationsNumber, updateDate);
		
		String text = context.getResources().getQuantityString(
				R.plurals.notify_new_pub_msg,
				newPublicationsNumber, 
				newPublicationsNumber);
		
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(context.getResources().getString(R.string.notify_new_pub_title))
				.setContentText(text)
				.setNumber(newPublicationsNumber)
				.setContentIntent(createNotificationEvent(NotifyEvent.RESTART_ACTIVITY))
				.setDeleteIntent(createNotificationEvent(NotifyEvent.DELETE_NOTIFICATION));
		
		Notification notification = builder.build();
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(0x000001, notification);
	}
	
	private int updateNewPublicationsNumberAndTime(int newPublicationsNumber, String updateDate) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		
		SharedPreferences.Editor editor = sharedPreferences.edit();
		
		int number = sharedPreferences.getInt("newPublicationsRecorded", 0);
		number += newPublicationsNumber;
		
		editor.putInt("newPublicationsRecorded", number);
		if(sharedPreferences.getString("newPublicationsRecordDate", null) == null){
			editor.putString("newPublicationsRecordDate", updateDate);
		}
		
		editor.commit();
		
		return number;
	}
	
	private PendingIntent createNotificationEvent(NotifyEvent event) {
		Intent intent = new Intent(context, NotificationReceiver.class);
		event.attachTo(intent);
		return PendingIntent.getBroadcast(context, event.ordinal(), intent, 
				PendingIntent.FLAG_UPDATE_CURRENT);
	}
}
