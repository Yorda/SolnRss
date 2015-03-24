package free.solnRss.manager;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import free.solnRss.R;


public class UpdatingProcessConnectionManager {
	private static String	noConnectionReason;

	public static boolean canUseConnection(final Context context) {

		final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		// For WiFi
		final boolean isWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();

		boolean isMobile = false;

		// For Device connection
		// Could be null on tablet
		if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null) {

			isMobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
		}

		if (!isMobile && !isWifi) {
			noConnectionReason = context.getResources().getString(R.string.no_connection);
			return false;
		}

		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

		if (preferences.getBoolean("pref_what_type_of_connection", true) && !isWifi) {
			noConnectionReason = context.getResources().getString(R.string.no_connection_wifi);
			return false;
		}

		return true;
	}

	public static String noConnectionReason() {
		return noConnectionReason;
	}
}
