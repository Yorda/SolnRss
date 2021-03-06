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
import free.solnRss.activity.SolnRss;
import free.solnRss.receiver.NotificationReceiver;


public class NewPublicationsNotification {

	public static enum NotifyEvent {
		RESTART_ACTIVITY, DELETE_NOTIFICATION;

		private static final String	name	= NotifyEvent.class.getName();

		public void attachTo(final Intent intent) {
			intent.putExtra(name, ordinal());
		}

		public static NotifyEvent detachFrom(final Intent intent) {
			if (!intent.hasExtra(name)) {
				return null;
			}
			final int i = intent.getIntExtra(name, -1);
			if (i == -1) {
				return null;
			}
			intent.removeExtra(name);

			return values()[i];
		}
	}

	private Context	context;

	public NewPublicationsNotification(final Context context) {
		this.context = context;
	}

	public void notificationForNewPublications(int newPublicationsNumber, String dateNewPublicationsFound) {

		//newPublicationsNumber = updateNewPublicationsNumberAndTime(newPublicationsNumber, dateNewPublicationsFound);
		newPublicationsNumber = updateNewPublicationsNumber(newPublicationsNumber);
		dateNewPublicationsFound = updateNewPublicationsTime(dateNewPublicationsFound);

		final String text = context.getResources().getQuantityString(R.plurals.notify_new_pub_msg, newPublicationsNumber, newPublicationsNumber);

		final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		final NotificationCompat.Builder builder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(context.getResources().getString(R.string.notify_new_pub_title)).setContentText(text)
				.setNumber(newPublicationsNumber)
				.setContentIntent(createPendingIntent(NotifyEvent.RESTART_ACTIVITY, dateNewPublicationsFound, newPublicationsNumber))
				.setDeleteIntent(createNotificationEvent(NotifyEvent.DELETE_NOTIFICATION));

		final Notification notification = builder.build();
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(0x000001, notification);
	}

	private int updateNewPublicationsNumber(final int newPublicationsNumber) {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

		final SharedPreferences.Editor editor = sharedPreferences.edit();

		int number = sharedPreferences.getInt("newPublicationsRecorded", 0);
		number += newPublicationsNumber;
		editor.putInt("newPublicationsRecorded", number);

		editor.commit();

		return number;
	}

	private String updateNewPublicationsTime(final String dateNewPublicationsFound) {

		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		final SharedPreferences.Editor editor = sharedPreferences.edit();

		String lastDate = sharedPreferences.getString("newPublicationsRecordDate", null);

		if (lastDate == null) {
			lastDate = dateNewPublicationsFound;
			editor.putString("newPublicationsRecordDate", lastDate);
		}

		editor.commit();

		return lastDate;
	}

	private PendingIntent createPendingIntent(final NotifyEvent event, final String dateNewPublicationsFound, final int lastFoundNumber) {

		final Intent intent = new Intent(context, SolnRss.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

		intent.putExtra("dateNewPublicationsFound", dateNewPublicationsFound);

		intent.putExtra("lastFoundNumber", Integer.valueOf(lastFoundNumber).toString());

		event.attachTo(intent);

		return PendingIntent.getActivity(context, event.ordinal(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	private PendingIntent createNotificationEvent(final NotifyEvent event) {
		final Intent intent = new Intent(context, NotificationReceiver.class);
		event.attachTo(intent);
		return PendingIntent.getBroadcast(context, event.ordinal(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}
}
