package free.solnRss.repository;

import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntry;

import free.solnRss.utility.SyndicateUtil;

public class RepositoryHelper extends SQLiteOpenHelper {

	public static int VERSION = 10;
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
		if (newVersion == 10) {
			upgradeToV10(db);
		}
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
			
			,"create table d_rss_title_url (\r\n"+
			"	_id INTEGER PRIMARY KEY autoincrement,\r\n" +
			"	rss_url text,\r\n"+
			"	rss_title text,\r\n"+
			"	syn_syndication_id INTEGER NOT NULL,\r\n" + 
			"	FOREIGN KEY(syn_syndication_id) REFERENCES d_syndication( _id)\r\n" + 
			");"
			};
	
	private void upgradeToV10(SQLiteDatabase db) {
		SyndicateUtil syndicateUtil = new SyndicateUtil();
		db.beginTransaction();
		try {
			
			db.execSQL(tables[4]);
			
			Cursor c = db.rawQuery(" select _id, syn_last_rss_published from d_syndication ",null);
			
			c.moveToFirst();
			Integer id = -1;
			String lastRSS = null;
			
			while (c.moveToNext()) {
				id = c.getInt(0);
				lastRSS = c.getString(1);
				
				if (!TextUtils.isEmpty(lastRSS)) {
					syndicateUtil.init(lastRSS);
					List<SyndEntry> entries = syndicateUtil.lastEntries();
					
					for (SyndEntry e : entries) {
						String[] args = new String[3];
						args[0] = e.getLink();
						args[1] = e.getTitle();
						args[2] = id.toString();
						db.execSQL("insert into d_rss_title_url (rss_url, rss_title, syn_syndication_id) values (?,?,?) ",args);
					}
				}
			}
			
			db.execSQL(" update d_syndication set syn_last_rss_published = null ");
			
			db.setTransactionSuccessful();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}
}
