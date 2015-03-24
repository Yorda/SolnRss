package free.solnRss.notification;


import android.content.Intent;
import free.solnRss.notification.NewPublicationsNotification.NotifyEvent;


public class NewSyndicationNotification {

	public static enum NotifySyndicationEvent {
		NEW_SYNDICATION;

		private static final String	name	= NotifyEvent.class.getName();

		public void attachTo(final Intent intent) {
			intent.putExtra(name, ordinal());
		}

		public static NotifySyndicationEvent detachFrom(final Intent intent) {
			if (!intent.hasExtra(name)) {
				// throw new IllegalStateException();
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

	public void notificationForNewSyndication(final int newPublicationsNumber, final String dateNewPublicationsFound) {

	}
}
