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

	public static int VERSION = 11;
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
		if (newVersion == 11) {
			upgradeToV11(db);
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
			//"  	syn_last_rss_published text \r\n" +
			"  	syn_last_rss_search_result INTEGER \r\n" +
			"); \r\n"
			,
			
			"create table d_publication (\r\n" + 
			"	_id INTEGER PRIMARY KEY autoincrement,\r\n" + 
			//"	pub_link text NOT NULL,\r\n" +  
			"	pub_title text NOT NULL,\r\n" + 
			"	pub_already_read integer,\r\n" + 
			//"	pub_publication text,\r\n" + 
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
			
			,"create table d_publication_content (\r\n" + 
			"	_id INTEGER PRIMARY KEY autoincrement,\r\n" + 
			"	pct_link text NOT NULL,\r\n" + 
			"	pct_publication text,\r\n" + 
			"	pub_publication_id INTEGER NOT NULL,\r\n" + 
			"	FOREIGN KEY(pub_publication_id) REFERENCES d_publication( _id)\r\n" + 
			"); \r\n"
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
			
			String tmp = "create table d_syndication_tmp (\r\n" + 
			"	_id INTEGER PRIMARY KEY autoincrement,\r\n" + 
			"	syn_name text NOT NULL,	\r\n" + 
			"	syn_url text NOT NULL,\r\n" + 
			"	syn_website_url text NOT NULL,\r\n" + 
			"	syn_is_active INTEGER NOT NULL,\r\n" + 
			"	syn_number_click INTEGER NOT NULL,\r\n" + 
			"	syn_last_extract_time datetime NOT NULL,\r\n" + 
			"	syn_creation_date datetime NOT NULL,\r\n" + 
			"   syn_display_on_timeline INTEGER NOT NULL \r\n" +
			"); \r\n";
			
			db.execSQL(tmp);
			
			c = db.rawQuery(" select _id,syn_name,syn_url,syn_website_url,syn_is_active,syn_number_click,syn_last_extract_time,syn_creation_date,syn_display_on_timeline  from d_syndication ",null);
			c.moveToFirst();
			
			do {
				
				db.execSQL("insert into d_syndication_tmp " +
						"(_id,syn_name,syn_url,syn_website_url,syn_is_active,syn_number_click,syn_last_extract_time,syn_creation_date,syn_display_on_timeline) " +
						"values (?,?,?,?,?,?,?,?,?) ",new String[]{
						Integer.valueOf(c.getInt(0)).toString(), 
						c.getString(1),
						c.getString(2),
						c.getString(3),
						Integer.valueOf(c.getInt(4)).toString(),
						Integer.valueOf(c.getInt(5)).toString(), 
						c.getString(6),
						c.getString(7),
						Integer.valueOf(c.getInt(8)).toString()
				} );
			}while (c.moveToNext());
			
			db.execSQL("drop table d_syndication ");	
			
			String newTable = "create table d_syndication (\r\n" + 
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
					"); \r\n";
			
			db.execSQL(newTable);	
			
			c = db.rawQuery(" select _id,syn_name,syn_url,syn_website_url,syn_is_active,syn_number_click,syn_last_extract_time,syn_creation_date,syn_display_on_timeline  from d_syndication_tmp ",null);
			c.moveToFirst();
			
			
			do {
				
				db.execSQL("insert into d_syndication " +
						"(_id,syn_name,syn_url,syn_website_url,syn_is_active,syn_number_click,syn_last_extract_time,syn_creation_date,syn_display_on_timeline) " +
						"values (?,?,?,?,?,?,?,?,?) ",new String[]{
						Integer.valueOf(c.getInt(0)).toString(), 
						c.getString(1),
						c.getString(2),
						c.getString(3),
						Integer.valueOf(c.getInt(4)).toString(),
						Integer.valueOf(c.getInt(5)).toString(), 
						c.getString(6),
						c.getString(7),
						Integer.valueOf(c.getInt(8)).toString()
				} );
			}while (c.moveToNext());
			
			
			db.execSQL("drop table d_syndication_tmp ");	
			
			db.setTransactionSuccessful();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}

	
	protected void upgradeToV11(SQLiteDatabase db) {
		db.beginTransaction();
		try {

			String sql = "create table d_publication_content (\r\n" + 
					"	_id INTEGER PRIMARY KEY autoincrement,\r\n" + 
					"	pct_link text NOT NULL,\r\n" + 
					"	pct_publication text,\r\n" + 
					"	pub_publication_id INTEGER NOT NULL,\r\n" + 
					"	FOREIGN KEY(pub_publication_id) REFERENCES d_publication( _id)\r\n" + 
					"); \r\n";

			
			db.execSQL(sql);
					
			sql= "insert into d_publication_content(pct_link,pct_publication,pub_publication_id) select pub_link, pub_publication, _id from d_publication ";
			
			db.execSQL(sql);
			
			sql = "create table d_publication_tmp (\r\n" + 
					"	_id INTEGER PRIMARY KEY autoincrement,\r\n" + 
					"	pub_link text NOT NULL,\r\n" +  //
					"	pub_title text NOT NULL,\r\n" + 
					"	pub_already_read integer,\r\n" + 
					"	pub_publication text,\r\n" + 
					"	pub_publication_date datetime NOT NULL,\r\n" + 
					"	syn_syndication_id INTEGER NOT NULL,\r\n" + 
					"	FOREIGN KEY(syn_syndication_id) REFERENCES d_syndication( _id)\r\n" + 
					"); \r\n";
			
			db.execSQL(sql);
			
			sql= "insert into d_publication_tmp select * from d_publication ";
			
			db.execSQL(sql);
			
			db.execSQL("drop table d_publication ");	
			
			sql = "create table d_publication (\r\n" + 
					"	_id INTEGER PRIMARY KEY autoincrement,\r\n" + 
					"	pub_title text NOT NULL,\r\n" + 
					"	pub_already_read integer,\r\n" + 
					"	pub_publication_date datetime NOT NULL,\r\n" + 
					"	syn_syndication_id INTEGER NOT NULL,\r\n" + 
					"	FOREIGN KEY(syn_syndication_id) REFERENCES d_syndication( _id)\r\n" + 
					"); \r\n";
			
			db.execSQL(sql);
			
			sql= "insert into d_publication (_id,pub_title,pub_already_read,pub_publication_date,syn_syndication_id) select _id,pub_title,pub_already_read,pub_publication_date,syn_syndication_id from d_publication_tmp ";
			
			db.execSQL(sql);
			
			db.execSQL("drop table d_publication_tmp ");
			
			db.setTransactionSuccessful();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}

	}
}
