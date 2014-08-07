package free.solnRss.repository;

import android.database.sqlite.SQLiteDatabase;

public class SyndicationTable {

	public static final String SYNDICATION_TABLE = "d_syndication";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "syn_name";
	public static final String COLUMN_URL = "syn_url";
	public static final String COLUMN_SITE_URL = "syn_website_url";
	public static final String COLUMN_IS_ACTIVE = "syn_is_active";
	public static final String COLUMN_NUMBER_CLICK = "syn_number_click";
	public static final String COLUMN_LAST_EXTRACT_TIME    = "syn_last_extract_time";
	public static final String COLUMN_LAST_EXTRACT_DATE   = "syn_creation_date";
	public static final String COLUMN_DISPLAY_ON_TIMELINE = "syn_display_on_timeline";
	public static final String COLUMN_LAST_RSS_SEARCH_RESULT = "syn_last_rss_search_result";	
	
	private static final String DATABASE_CREATE ="create table d_syndication (\r\n" + 
			"	_id INTEGER PRIMARY KEY autoincrement,\r\n" + 
			"	syn_name text NOT NULL,	\r\n" + 
			"	syn_url text NOT NULL,\r\n" + 
			"	syn_website_url text NOT NULL,\r\n" + 
			"	syn_is_active INTEGER NOT NULL,\r\n" + 
			"	syn_number_click INTEGER NOT NULL,\r\n" + 
			"	syn_last_extract_time datetime NOT NULL,\r\n" + 
			"	syn_creation_date datetime NOT NULL,\r\n" + 
			"   syn_display_on_timeline INTEGER NOT NULL, \r\n" +
			"  	syn_last_rss_search_result INTEGER \r\n" +
			");";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
	}
}
