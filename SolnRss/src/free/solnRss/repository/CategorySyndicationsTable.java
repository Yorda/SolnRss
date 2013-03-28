package free.solnRss.repository;

import android.database.sqlite.SQLiteDatabase;

public class CategorySyndicationsTable {

	public static final String CATEGORY_SYNDICATION_TABLE = "d_category_syndication";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_CATEGORY_ID = "cas_category_id";
	public static final String COLUMN_SYNDICATION_ID = "syn_syndication_id";
	
	private static final String DATABASE_CREATE = "create table d_category_syndication (\r\n"
			+ "	_id INTEGER PRIMARY KEY autoincrement,\r\n"
			+ "	cas_category_id INTEGER NOT NULL,\r\n"
			+ "	syn_syndication_id INTEGER NOT NULL,\r\n"
			+ "	FOREIGN KEY(cas_category_id) REFERENCES d_category( _id),\r\n"
			+ "	FOREIGN KEY(syn_syndication_id) REFERENCES d_syndication( _id)\r\n"
			+ ");";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
	}
}
