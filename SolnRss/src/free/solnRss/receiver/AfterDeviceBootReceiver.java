package free.solnRss.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import free.solnRss.alarmManager.FindNewPublicationsAlarmManager;

public class AfterDeviceBootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		
		if (preferences.getBoolean("pref_how_to_start_refresh", true)) {
			FindNewPublicationsAlarmManager finder = FindNewPublicationsAlarmManager
					.getInstance(preferences, context);
			if (!finder.isCancel()) {
				finder.defineNextTimeToWork();
			}
		}

	}

}