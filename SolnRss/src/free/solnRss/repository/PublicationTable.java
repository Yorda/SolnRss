package free.solnRss.repository;

import android.database.sqlite.SQLiteDatabase;

public class PublicationTable {

	public static final String PUBLICATION_TABLE = "d_publication";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_LINK = "pub_link";
	public static final String COLUMN_TITLE = "pub_title";
	public static final String COLUMN_ALREADY_READ = "pub_already_read";
	public static final String COLUMN_PUBLICATION = "pub_publication";
	public static final String COLUMN_PUBLICATION_DATE = "pub_publication_date";
	public static final String COLUMN_SYNDICATION_ID = "syn_syndication_id";

	// Database creation SQL statement
	private static final String DATABASE_CREATE = "create table d_publication (\r\n"
			+ "	_id INTEGER PRIMARY KEY autoincrement,\r\n"
			+ "	pub_link text NOT NULL,\r\n"
			+ "	pub_title text NOT NULL,\r\n"
			+ "	pub_already_read integer,\r\n"
			+ "	pub_publication text,\r\n"
			+ "	pub_publication_date datetime NOT NULL,\r\n"
			+ "	syn_syndication_id INTEGER NOT NULL,\r\n"
			+ "	FOREIGN KEY(syn_syndication_id) REFERENCES d_syndication( _id)\r\n"
			+ ");";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
	}

}
