package free.solnRss.repository;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import model.Publication;
import model.Syndication;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class SyndicationRepository extends Repository {

	final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRENCH);
	
	public SyndicationRepository(Context context) {
		super.context = context;
	}

	/**
	 * Set a syndication inactive
	 * The articles list will be not updated
	 * @param id
	 */
	public void changeActiveStatus(Integer id, Integer status) {
		open(context);
		try {
			String[] whereArgs = new String[] { id.toString() };
			
			ContentValues values = new ContentValues();
			values.put("syn_is_active", status);
			
			sqLiteDatabase.beginTransaction();
			sqLiteDatabase.update("d_syndication", values, "_id = ? ", whereArgs);
			sqLiteDatabase.setTransactionSuccessful();
			
		} finally {
			sqLiteDatabase.endTransaction();
			close();
		}
	}
	
	/**
	 * Delete syndication and all data linked
	 * @param id
	 */
	public void delete(Integer id) {
		open(context);
		try {
			String[] whereArgs = new String[] { id.toString() };
			sqLiteDatabase.beginTransaction();
			sqLiteDatabase.delete("d_categorie_syndication", " syn_syndication_id = ? ", whereArgs);
			sqLiteDatabase.delete("d_publication", "syn_syndication_id = ? ", whereArgs);
			sqLiteDatabase.delete("d_syndication", "_id = ? ", whereArgs);
			sqLiteDatabase.setTransactionSuccessful();
		} finally {
			sqLiteDatabase.endTransaction();
			close();
		}
	}

	public List<Syndication> findAllActiveSyndication() {
		Date now = new Date();
		// Period in minute
		// 
		int refresh = 10;

		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(now);
		calendar.add(Calendar.MINUTE, -refresh);
		
		open(context);
		Cursor c = sqLiteDatabase.rawQuery(
					"select * from d_syndication where syn_is_active = 0",
					new String[]{});

		List<Syndication> syndications = new ArrayList<Syndication>();
		Syndication s = null;
		
		if (c.getCount() > 0) {
			c.moveToFirst();
			do {

				s = new Syndication();
				s.setId(c.getInt(c.getColumnIndex("_id")));
				s.setUrl(c.getString(c.getColumnIndex("syn_url")));
				syndications.add(s);

			} while (c.moveToNext());
		}
		
		close();
		return syndications;
	}
	
	/**
	 * Search all site for update their last articles published
	 */
	public List<Syndication> findSyndicationToUpdate() {
		
		Date now = new Date();
		// Period in minute
		// 
		int refresh = 10;

		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(now);
		calendar.add(Calendar.MINUTE, -refresh);
		
		String[] selectionArgs = new String[1];
		selectionArgs[0]       = sdf.format(calendar.getTime());

		open(context);
		Cursor c = sqLiteDatabase.rawQuery(
					"select * from d_syndication where syn_last_extract_time < Datetime(?) and syn_is_active = 0",
					selectionArgs);

		List<Syndication> syndications = new ArrayList<Syndication>();
		Syndication s = null;
		
		if (c.getCount() > 0) {
			c.moveToFirst();
			do {

				s = new Syndication();
				s.setId(c.getInt(c.getColumnIndex("_id")));
				s.setUrl(c.getString(c.getColumnIndex("syn_url")));
				syndications.add(s);

			} while (c.moveToNext());
		}
		
		close();
		return syndications;
	}
	
	public Cursor syndicationCategorie(Integer categorieId) {
		open(context);
		List<String> arr = new ArrayList<String>();

		StringBuilder sb = new StringBuilder();
		
		sb.append("select ");
		sb.append("s._id, s.syn_name, ");
		sb.append("cs.cas_categorie_id ");
		sb.append("from d_syndication s left join d_categorie_syndication cs on s._id = cs.syn_syndication_id ");
		sb.append("and cs.cas_categorie_id = ? order by s.syn_number_click desc ");

		arr.add(categorieId.toString());
		
		return sqLiteDatabase.rawQuery(sb.toString(),
				arr.toArray(new String[arr.size()]));
	}
	
	public Cursor fetchAllSiteBUG() {
		open(context);
		List<String> arr = new ArrayList<String>();

		StringBuilder sb = new StringBuilder();
		
		/*sb.append("select s._id, "
		+ "s.syn_last_extract_time , "
		+ "s.syn_creation_date, "
		+ "max(a.pub_publication_date), "
		+ "s.syn_name, "
		+ "s.syn_is_active, "
		+ "s.syn_number_click "
		+ " from d_publication a right join d_syndication s  on s._id = a.syn_syndication_id  ");*/
		
		sb.append("select ");
		sb.append("s._id, ");
		sb.append("s.syn_name, ");
		sb.append("s.syn_url, ");
		sb.append("s.syn_is_active, ");
		sb.append("s.syn_number_click, ");
		sb.append("max(p.pub_publication_date) ");
		sb.append("from d_syndication s, d_publication p where p.syn_syndication_id = s._id ");

		sb.append(" order by s.syn_number_click desc");

		return sqLiteDatabase.rawQuery(sb.toString(),
				arr.toArray(new String[arr.size()]));
	}
	
	public Cursor fetchAllSite() {
		open(context);
		String[] columns = { "_id", "syn_name", "syn_url", "syn_is_active", "syn_number_click" };
		return sqLiteDatabase
			.query("d_syndication", columns, null, null, null, null,	" syn_number_click desc ", null);
	}
	
	public boolean isStillRecorded(String url) {
		open(context);

		String[] selectionArgs = new String[1];
		selectionArgs[0] = url;
		Cursor c = sqLiteDatabase.rawQuery(
				"select * from d_syndication where syn_website_url = ? ",
				selectionArgs);
		
		int count = c.getCount();
		close();

		if (count > 0)
			return true;

		return false;
	}
	
	public void addWebSite(Syndication syndication) throws Exception {
		
		String now = sdf.format(new Date());
		
		try {
			open(context);
			
			ContentValues siteValues = new ContentValues();
			siteValues.put("syn_name", syndication.getName());
			siteValues.put("syn_url", syndication.getUrl());
			siteValues.put("syn_website_url", syndication.getWebsiteUrl());
			siteValues.put("syn_creation_date", now);
			siteValues.put("syn_last_extract_time", now);  
			siteValues.put("syn_is_active", 0); 
			siteValues.put("syn_number_click", 0); 
			
			sqLiteDatabase.beginTransaction();
			
			Long id = sqLiteDatabase.insert("d_syndication", null, siteValues);
			
			ContentValues contentValues = null;
			for (Publication publication : syndication.getPublications()) {
				
				contentValues = new ContentValues();
				contentValues.put("syn_syndication_id", id);
				contentValues.put("pub_link", publication.getUrl());
				contentValues.put("pub_title", publication.getTitle());
				contentValues.put("pub_already_read", 0);
				contentValues.put("pub_publication_date", sdf.format(
						(publication.getPublicationDate() == null 
							? new Date() : publication.getPublicationDate())));
				
				sqLiteDatabase.insert("d_publication", null, contentValues);
			}
			sqLiteDatabase.setTransactionSuccessful();

		} finally {
			sqLiteDatabase.endTransaction();
			close();
		}
	}

	public void updateLastExtractTime(Integer id) {

		String now = sdf.format(new Date());
		open(context);
		ContentValues values = new ContentValues();
		values.put("syn_last_extract_time", now);
		sqLiteDatabase.update("d_syndication", values, " _id = ? ",
				new String[] { id.toString() });
		close();
	}
}
