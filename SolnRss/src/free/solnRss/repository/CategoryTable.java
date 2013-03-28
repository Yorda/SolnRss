package free.solnRss.repository;

import android.database.sqlite.SQLiteDatabase;

public class CategoryTable {

	public static final String CATEGORY_TABLE = "d_categorie";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "cat_name";
	
	private static final String DATABASE_CREATE = "create table d_category (\r\n"
			+ "	_id INTEGER PRIMARY KEY autoincrement,\r\n"
			+ "	cat_name text NOT NULL \r\n" + ");";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		
	}
}
