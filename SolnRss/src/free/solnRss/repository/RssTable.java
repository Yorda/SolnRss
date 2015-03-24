package free.solnRss.repository;


import android.database.sqlite.SQLiteDatabase;


public class RssTable {

	public static final String	RSS_TABLE				= "d_rss_title_url";
	public static final String	COLUMN_ID				= "_id";
	public static final String	COLUMN_TITLE			= "rss_title";
	public static final String	COLUMN_URL				= "rss_url";
	public static final String	COLUMN_SYNDICATION_ID	= "syn_syndication_id";

	// Database creation SQL statement
	private static final String	DATABASE_CREATE			= "create table d_rss_title_url (\r\n" + "	_id INTEGER PRIMARY KEY autoincrement,\r\n"
																+ "	rss_url text,\r\n" + "	rss_title text,\r\n"
																+ "	syn_syndication_id INTEGER NOT NULL,\r\n"
																+ "	FOREIGN KEY(syn_syndication_id) REFERENCES d_syndication( _id)\r\n" + ");";

	public static void onCreate(final SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(final SQLiteDatabase database, final int oldVersion, final int newVersion) {
	}
}
