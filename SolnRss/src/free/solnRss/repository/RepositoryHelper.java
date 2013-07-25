package free.solnRss.repository;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class RepositoryHelper extends SQLiteOpenHelper {

	public static int VERSION = 9;
	public static String DATABASE_NAME = "SOLNRSS2.db";

	private static RepositoryHelper instance;

	public RepositoryHelper(Context context) {
		super(context, DATABASE_NAME, null, VERSION);
	}

	/*
	 * In order to avoid database locked exception open only one connection for all application
	 */
	public static synchronized RepositoryHelper getInstance(Context context) {
		if (instance == null)
			instance = new RepositoryHelper(context);
		return instance;
	}

	public RepositoryHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		 setDatabase(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	private void setDatabase(SQLiteDatabase db) {
		try {

			for (String table : tables) {
				db.execSQL(table);
			}

		} catch (Exception e) {
			Log.e(this.getClass().getName(), "Unable to create the database ");
			e.printStackTrace();
		}
	}
	
	final String[] tables = { "create table d_syndication (\r\n" + 
			"	_id INTEGER PRIMARY KEY autoincrement,\r\n" + 
			"	syn_name text NOT NULL,	\r\n" + 
			"	syn_url text NOT NULL,\r\n" + 
			"	syn_website_url text NOT NULL,\r\n" + 
			"	syn_is_active INTEGER NOT NULL,\r\n" + 
			"	syn_number_click INTEGER NOT NULL,\r\n" + 
			"	syn_last_extract_time datetime NOT NULL,\r\n" + 
			"	syn_creation_date datetime NOT NULL,\r\n" + 
			"   syn_display_on_timeline INTEGER NOT NULL, \r\n" +
			"  	syn_last_rss_published text \r\n" +
			"); \r\n"
			,
			
			"create table d_publication (\r\n" + 
			"	_id INTEGER PRIMARY KEY autoincrement,\r\n" + 
			"	pub_link text NOT NULL,\r\n" + 
			"	pub_title text NOT NULL,\r\n" + 
			"	pub_already_read integer,\r\n" + 
			"	pub_publication text,\r\n" + 
			"	pub_publication_date datetime NOT NULL,\r\n" + 
			"	syn_syndication_id INTEGER NOT NULL,\r\n" + 
			"	FOREIGN KEY(syn_syndication_id) REFERENCES d_syndication( _id)\r\n" + 
			"); \r\n"
			,
			
			"create table d_categorie (\r\n" + 
			"	_id INTEGER PRIMARY KEY autoincrement,\r\n" + 
			"	cat_name text NOT NULL\r\n" + 
			");\r\n" 
			,
			
			"create table d_categorie_syndication (\r\n" + 
			"	_id INTEGER PRIMARY KEY autoincrement,\r\n" + 
			"	cas_categorie_id INTEGER NOT NULL,\r\n" + 
			"	syn_syndication_id INTEGER NOT NULL,\r\n" + 
			"	FOREIGN KEY(cas_categorie_id) REFERENCES d_categorie( _id),\r\n" + 
			"	FOREIGN KEY(syn_syndication_id) REFERENCES d_syndication( _id)\r\n" + 
			");"
			};
}
