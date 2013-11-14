package free.solnRss.alarmManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

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
	
	public static synchronized FindNewPublicationsAlarmManager getInstance(SharedPreferences sharedPreferences,
			Context context) {
		if(findNewPublicationsAlarmManager == null){
			findNewPublicationsAlarmManager = new FindNewPublicationsAlarmManager(sharedPreferences, context);
			
		}
		return findNewPublicationsAlarmManager;
	}
	
	public static synchronized void createInstance(SharedPreferences sharedPreferences,
			Context context) {
		if(findNewPublicationsAlarmManager == null){
			findNewPublicationsAlarmManager = new FindNewPublicationsAlarmManager(sharedPreferences, context);
			
		}
	}
	
	private FindNewPublicationsAlarmManager(SharedPreferences sharedPreferences,
			Context context) {
		this.pref = sharedPreferences;
		this.context = context;
		
		this.alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		
		this.periodRefreshTimeInMinute = pref.getInt(
				"pref_search_publication_time", 15);
		
		this.pendingIntent = PendingIntent.getService(context, 0, new Intent(
				context, PublicationsFinderService.class), 0);
		
		PreferenceManager.getDefaultSharedPreferences(context)
				.registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.compareTo("pref_search_publication_time") == 0) {
			this.periodRefreshTimeInMinute = pref.getInt(
					"pref_search_publication_time", 15);
			if (isCancel()) {
				// Stop the search
				cancelAlarm();
			} else {
				defineNextTimeToWork();
			}
		}
	}
	
	DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.FRENCH);
	Calendar calendar = new GregorianCalendar();
	
	public void defineNextTimeToWork() {
		
		long periodRefreshTimeInMilisecond = 60000 * periodRefreshTimeInMinute;
		//Log.e(FindNewPublicationsAlarmManager.class.getName(), "Soln.R ->periodRefreshTimeInMilisecond = " + periodRefreshTimeInMilisecond);
		
		long lastRefreshTime = pref.getLong("publicationsLastRefresh", 0);		
		//Log.e(FindNewPublicationsAlarmManager.class.getName(), "Soln.R ->lastRefreshTime = " + sdf.format(lastRefreshTime));
		
		// In case of next refresh time is before now next refresh begin in
		// one minute.
		long nextRefreshTime = Math.max( new Date().getTime() + 60000, 
				lastRefreshTime + periodRefreshTimeInMilisecond);
		//Log.e(FindNewPublicationsAlarmManager.class.getName(), "Soln.R ->nextRefreshTime = " + sdf.format(nextRefreshTime));
		
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, nextRefreshTime,
				periodRefreshTimeInMilisecond, pendingIntent);
	}

	public void cancelAlarm() {
		// Log.e(FindNewPublicationsAlarmManager.class.getName(), "Soln.R ->cancelAlarm = cancel the timer ");
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


