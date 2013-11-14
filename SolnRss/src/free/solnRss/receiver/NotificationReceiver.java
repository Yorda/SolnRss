package free.solnRss.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import free.solnRss.activity.SolnRss;
import free.solnRss.notification.NewPublicationsNotification;

public class NotificationReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		NewPublicationsNotification.NotifyEvent event = NewPublicationsNotification.NotifyEvent.detachFrom(intent);
		
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		
		if (event.compareTo(NewPublicationsNotification.NotifyEvent.RESTART_ACTIVITY) == 0) {
			
			startOrRestartMainActivity(context.getApplicationContext(),
					sharedPreferences.getString("newPublicationsRecordDate", null));
		}
		
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt("newPublicationsRecorded", 0);
		editor.putString("newPublicationsRecordDate", null);
		
		editor.commit();
	}
	
	private void startOrRestartMainActivity(Context c, String updateDate){
		Intent i = new Intent(c, SolnRss.class);

		i.addFlags(
		          Intent.FLAG_ACTIVITY_NEW_TASK
		        | Intent.FLAG_ACTIVITY_CLEAR_TOP
		        | Intent.FLAG_ACTIVITY_SINGLE_TOP);

		i.putExtra("SERVICE_RESULT",
				SolnRss.SERVICE_RESULT.NEW_PUBLICATIONS);
		
		i.putExtra("newPublicationsRecordDate", 
				updateDate);
				
		c.startActivity(i);
	}
}
