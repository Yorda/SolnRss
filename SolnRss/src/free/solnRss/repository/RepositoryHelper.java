package free.solnRss.repository;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class RepositoryHelper extends SQLiteOpenHelper {

	public static int VERSION = 16;
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
		/*if (newVersion == 16) {
			upgradeToV16(db);
		}*/
	}

	private void setDatabase(SQLiteDatabase db) {
		try {
			for (String table : tables) {
				db.execSQL(table);
			}

		} catch (Exception e) {
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
			"  	syn_last_rss_search_result INTEGER \r\n" +
			"); \r\n"
			,
			
			"create table d_publication (\r\n" + 
			"	_id INTEGER PRIMARY KEY autoincrement,\r\n" + 
			"	pub_title text NOT NULL,\r\n" + 
			"	pub_already_read integer,\r\n" + 
			"	pub_publication_date datetime NOT NULL,\r\n" + 
			"	syn_syndication_id INTEGER NOT NULL,\r\n" + 
			"	pub_favorite INTEGER, \r\n" + 
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
			
			,"create table d_rss_title_url (\r\n"+
			"	_id INTEGER PRIMARY KEY autoincrement,\r\n" +
			"	rss_url text,\r\n"+
			"	rss_title text,\r\n"+
			"	syn_syndication_id INTEGER NOT NULL,\r\n" + 
			"	FOREIGN KEY(syn_syndication_id) REFERENCES d_syndication( _id)\r\n" + 
			");",
			 "create table d_publication_image (\r\n"+
						"	_id INTEGER PRIMARY KEY autoincrement,\r\n" +
						"	image_url text,\r\n"+
						"	image_path text,\r\n"+
						"	image_name text,\r\n"+
						"	pub_publication_id INTEGER NOT NULL,\r\n" + 
						"	FOREIGN KEY(pub_publication_id) REFERENCES d_publication( _id)\r\n" + 
						");"
			
			/*,"create table r_error (\r\n"+
			"	err_code INTEGER,\r\n"+
			"	err_msg  text\r\n"+ 
			");" 
					+ " insert into r_error values (1, 'err_search_bad_url');"
					+ " insert into r_error values (2, 'err_load_http_data');"
					+ " insert into r_error values (3, 'err_extract_rss');"*/
			
			};
	

	
	protected void upgradeToV16(SQLiteDatabase db) {
		try {
			db.beginTransaction();
			String sql = "create table d_publication_image (\r\n"+
					"	_id INTEGER PRIMARY KEY autoincrement,\r\n" +
					"	image_url text,\r\n"+
					"	image_path text,\r\n"+
					"	image_name text,\r\n"+
					"	pub_publication_id INTEGER NOT NULL,\r\n" + 
					"	FOREIGN KEY(pub_publication_id) REFERENCES d_publication( _id)\r\n" + 
					");";
			
			db.execSQL(sql);
						
			db.setTransactionSuccessful();
			
		} catch (Exception e) {
			e.printStackTrace();
			
		} finally {
			db.endTransaction();
		}
	}
	
}
