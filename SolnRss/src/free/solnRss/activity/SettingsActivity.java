package free.solnRss.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.util.Log;
import free.solnRss.R;
import free.solnRss.provider.PublicationTable;
import free.solnRss.provider.PublicationsProvider;
import free.solnRss.provider.SyndicationTable;

public class SettingsActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction().replace(
				android.R.id.content,
				new SettingPreferenceFragment()).commit();
		
		testContentProvider();
	}

	public static class SettingPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(final Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.layout.activity_settings_rss);
		}
	}
	
	private void testContentProvider() {

		final String publicationTable = PublicationTable.PUBLICATION_TABLE;
		final String syndicationTable = SyndicationTable.SYNDICATION_TABLE;
		
		String columns[] = new String[] {
			publicationTable + "." + PublicationTable.COLUMN_ID,
			publicationTable + "." + PublicationTable.COLUMN_TITLE, 
			publicationTable + "." + PublicationTable.COLUMN_LINK,
			publicationTable + "." + PublicationTable.COLUMN_ALREADY_READ, 
			syndicationTable + "." + SyndicationTable.COLUMN_NAME 
		};
		
		Cursor cursor = getContentResolver().query(PublicationsProvider.URI, columns, null, null, null);
		int i = 0, max = 25;
		
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			do {
				String value = cursor.getString(cursor.getColumnIndex(PublicationTable.COLUMN_TITLE));
				Log.e(SettingsActivity.class.getName(), cursor.getCount() + " --------------> "	+ value);
				i++;
				
			} while (cursor.moveToNext() && i < max);
		}
		
	}
}
