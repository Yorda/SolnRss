package free.solnRss.manager;

import free.solnRss.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;

public class UpdatingProcessConnectionManager {
	private static String noConnectionReason;
	public static boolean canUseConnection(Context context) {

		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		// For WiFi
		boolean isWifi = connectivityManager.getNetworkInfo(
				ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();

		// For Device connection
		boolean isMobile = connectivityManager.getNetworkInfo(
				ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();

		if (!isMobile && !isWifi) {
			noConnectionReason = context.getResources().getString(R.string.no_connection);
			return false;
		}

		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);

		if (preferences.getBoolean("pref_what_type_of_connection", true)
				&& !isWifi) {
			noConnectionReason = context.getResources().getString(R.string.no_connection_wifi);
			return false;
		}

		return true;
	}
	
	public static String noConnectionReason() {
		return noConnectionReason;
	}
}
