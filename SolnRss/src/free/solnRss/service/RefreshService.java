/**
 * Sparse rss
 * 
 * Copyright (c) 2010 Stefan Handschuh
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */

package free.solnRss.service;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

public class RefreshService extends Service 
		implements SharedPreferences.OnSharedPreferenceChangeListener {
	
	private Intent refreshBroadcastIntent;
	private AlarmManager alarmManager;
	private PendingIntent timerIntent;
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.compareTo("pref_search_publication_time") == 0) {
			
			/*int time = PreferenceManager.getDefaultSharedPreferences(this)
					.getInt("pref_search_publication_time", 15);*/
			
			restartTimer(false);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		onRebind(intent);
		return null;
	}

	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// we want to use rebind
		return true; 
	}

	@Override
	public void onCreate() {
		super.onCreate();
		refreshBroadcastIntent = new Intent("free.solnRss.service.PublicationsRefresh");
		alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		restartTimer(true);
	}

	private void restartTimer(boolean created) {
		/*
		if (timerIntent == null) {
			timerIntent = PendingIntent.getBroadcast(this, 0,refreshBroadcastIntent, 0);
		} else {
			alarmManager.cancel(timerIntent);
		}

		int time = 3600000;
	
		time = Math.max(6000,PreferenceManager.getDefaultSharedPreferences(this).getInt("pref_search_publication_time", 15));
		
		long initialRefreshTime = SystemClock.elapsedRealtime() + 10000;

		if (created) {
			long lastRefresh =  
				PreferenceManager.getDefaultSharedPreferences(this).getLong(
					"Strings.PREFERENCE_LASTSCHEDULEDREFRESH", 0);

			if (lastRefresh > 0) {
				// this indicates a service restart by the system
				initialRefreshTime = Math.max(SystemClock.elapsedRealtime() + 10000, lastRefresh + time);
			}
		}

		alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				initialRefreshTime, time, timerIntent);*/
		
		Calendar cal = Calendar.getInstance();

		Intent intent = new Intent(this, PublicationsRefresh.class);
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);

		alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		
		// Start every 30 seconds
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 30*1000, pendingIntent); 
	}

	@Override
	public void onDestroy() {
		if (timerIntent != null) {
			alarmManager.cancel(timerIntent);
		}
		super.onDestroy();
	}
}
