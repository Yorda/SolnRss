package free.solnRss.alarmManager;

import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import free.solnRss.service.PublicationsFinderService;

/**
 * 
 * @author jftomasi
 * 
 */
public class FindNewPublicationsAlarmManager implements
		SharedPreferences.OnSharedPreferenceChangeListener {

	private static FindNewPublicationsAlarmManager findNewPublicationsAlarmManager;

	private SharedPreferences pref;
	private Context context;
	private AlarmManager alarmManager;
	private int periodRefreshTimeInMinute;
	private PendingIntent pendingIntent;

	public static synchronized FindNewPublicationsAlarmManager getInstance(
			SharedPreferences sharedPreferences, Context context) {
		if (findNewPublicationsAlarmManager == null) {
			findNewPublicationsAlarmManager = new FindNewPublicationsAlarmManager(
					sharedPreferences, context);
		}
		return findNewPublicationsAlarmManager;
	}

	public static synchronized void createInstance(
			SharedPreferences sharedPreferences, Context context) {
		if (findNewPublicationsAlarmManager == null) {
			findNewPublicationsAlarmManager = new FindNewPublicationsAlarmManager(
					sharedPreferences, context);
		}
	}

	private FindNewPublicationsAlarmManager(
			SharedPreferences sharedPreferences, Context context) {

		this.pref = sharedPreferences;
		this.context = context;

		alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		periodRefreshTimeInMinute = pref.getInt("pref_search_publication_time", 15);

		pendingIntent = PendingIntent.getService(context, 0, new Intent(
				context, PublicationsFinderService.class), 0);

		PreferenceManager.getDefaultSharedPreferences(context)
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.compareTo("pref_search_publication_time") == 0) {
			this.periodRefreshTimeInMinute = pref.getInt("pref_search_publication_time", 15);
			if (isCancel()) {
				// Stop the search
				cancelAlarm();
			} else {
				defineNextTimeToWork();
			}
		}
	}

	public void defineNextTimeToWork() {

		long periodRefreshTimeInMilisecond = 60000 * periodRefreshTimeInMinute;

		long lastRefreshTime = pref.getLong("publicationsLastRefresh", 0);

		// In case of next refresh time is before now next refresh begin in one
		// minute.
		long nextRefreshTime = Math.max(new Date().getTime() + 60000,
				lastRefreshTime + periodRefreshTimeInMilisecond);

		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, nextRefreshTime,
				periodRefreshTimeInMilisecond, pendingIntent);
	}

	public void cancelAlarm() {
		alarmManager.cancel(pendingIntent);
	}

	public boolean isCancel() {
		return periodRefreshTimeInMinute == 0;
	}

	public boolean isAlarmAlreadyDefined() {
		return (PendingIntent.getBroadcast(context, 0, new Intent(
				"free.solnRss.service.PublicationsFinderService"),
				PendingIntent.FLAG_NO_CREATE) != null);
	}
}
