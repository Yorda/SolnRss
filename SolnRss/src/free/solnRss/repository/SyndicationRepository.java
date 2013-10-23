package free.solnRss.repository;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import free.solnRss.model.Publication;
import free.solnRss.model.Syndication;
import free.solnRss.provider.SolnRssProvider;

public class SyndicationRepository {
	
	final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",	Locale.FRENCH);
	private Context context;

	private StringBuilder selection = new StringBuilder();
	private List<String> args = new ArrayList<String>();
	
	public SyndicationRepository(Context context) {
		this.context = context;
	}

	public static final String syndicationTable = SyndicationTable.SYNDICATION_TABLE;
	
	Uri uri = Uri.parse(SolnRssProvider.URI + "/syndication");
	
	public final String syndicationProjection[] = 
			new String[] {
		syndicationTable + "." + SyndicationTable.COLUMN_ID,
		syndicationTable + "." + SyndicationTable.COLUMN_NAME,
		syndicationTable + "." + SyndicationTable.COLUMN_URL,
		syndicationTable + "." + SyndicationTable.COLUMN_IS_ACTIVE,
		syndicationTable + "." + SyndicationTable.COLUMN_NUMBER_CLICK,
		syndicationTable + "." + SyndicationTable.COLUMN_DISPLAY_ON_TIMELINE
	};
	
	public CursorLoader loadSyndications(String filterText) {

		selection.setLength(0);
		args.clear();

		if (!TextUtils.isEmpty(filterText)) {
			selection.append(SyndicationTable.COLUMN_NAME + " like ? ");
			args.add("%" + filterText.toString() + "%");
		}
		return new CursorLoader(context, uri, syndicationProjection,
				selection.toString(), args.toArray(new String[args.size()]),
				null);
	}
	
	public void addOneReadToSyndication(Integer syndicationId, Integer numberOfClick) {
		ContentValues values = new ContentValues();
		values.put("syn_number_click", numberOfClick+1);
		context.getContentResolver().update(uri, values, "_id = ? ",
				new String[] { syndicationId.toString() });
	}
	
	

	
	/**
	 * Set a syndication inactive
	 * The articles list will be not updated
	 * @param id
	 */
	public void changeActiveStatus(Integer id, Integer status) {
		String[] whereArgs = new String[] { id.toString() };
		ContentValues values = new ContentValues();
		values.put("syn_is_active", status);
		RepositoryHelper.getInstance(context).getWritableDatabase()
				.update("d_syndication", values, "_id = ? ", whereArgs);
	}
	
	/**
	 * Delete syndication and all data linked
	 * 
	 * @param id
	 */
	public void delete(Integer id) {
		SQLiteDatabase db  = RepositoryHelper.getInstance(context).getWritableDatabase();
		try {
			
			String[] whereArgs = new String[] { id.toString() };
			db.beginTransaction();
			db.delete("d_categorie_syndication",
					" syn_syndication_id = ? ", whereArgs);
			db.delete("d_publication", "syn_syndication_id = ? ", whereArgs);
			db.delete("d_syndication", "_id = ? ", whereArgs);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}
	
	/**
	 * Search all site for update their last articles published
	 */
	public List<Syndication> findSyndicationToUpdate() {
		
		Date now = new Date();
		// Period in minute
		int refresh = 10;

		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(now);
		calendar.add(Calendar.MINUTE, -refresh);
		
		String[] selectionArgs = new String[1];
		selectionArgs[0]       = sdf.format(calendar.getTime());

		Cursor c = RepositoryHelper.getInstance(context).getReadableDatabase().rawQuery(
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
		
		
		return syndications;
	}
	
	public Cursor syndicationCategorie(Integer categorieId) {

		List<String> arr = new ArrayList<String>();

		StringBuilder sb = new StringBuilder();
		
		sb.append("select ");
		sb.append("s._id, s.syn_name, ");
		sb.append("cs.cas_categorie_id ");
		sb.append("from d_syndication s left join d_categorie_syndication cs on s._id = cs.syn_syndication_id ");
		sb.append("and cs.cas_categorie_id = ? order by s.syn_number_click desc ");

		arr.add(categorieId.toString());
		
		return RepositoryHelper.getInstance(context).getReadableDatabase().rawQuery(sb.toString(),
				arr.toArray(new String[arr.size()]));
	}

	public boolean isStillRecorded(String url) {
		String[] selectionArgs = new String[1];
		selectionArgs[0] = url;
		Cursor c = RepositoryHelper
				.getInstance(context)
				.getReadableDatabase()
				.rawQuery("select * from d_syndication where syn_website_url = ? ",	selectionArgs);

		int count = c.getCount();

		if (count > 0)
			return true;

		return false;
	}
	
	public long addWebSite(Syndication syndication) throws Exception {
		
		String now = sdf.format(new Date());
		SQLiteDatabase db  = RepositoryHelper.getInstance(context).getWritableDatabase();
		Long newIdInserted = null;
		try {
			ContentValues siteValues = new ContentValues();
			siteValues.put("syn_name", syndication.getName());
			siteValues.put("syn_url", syndication.getUrl());
			siteValues.put("syn_website_url", syndication.getWebsiteUrl());
			siteValues.put("syn_creation_date", now);
			siteValues.put("syn_last_extract_time", now);  
			siteValues.put("syn_is_active", 0); 
			siteValues.put("syn_display_on_timeline", 1);
			siteValues.put("syn_number_click", 0); 
			
			db.beginTransaction();
			
			newIdInserted = db.insert("d_syndication", null, siteValues);
			syndication.setId(Integer.valueOf(newIdInserted.toString()));
			
			ContentValues contentValues = null;
			for (Publication publication : syndication.getPublications()) {
				
				contentValues = new ContentValues();
				contentValues.put("syn_syndication_id", newIdInserted);
				contentValues.put("pub_link", publication.getUrl());
				contentValues.put("pub_title", publication.getTitle());
				contentValues.put("pub_already_read", 0);
				contentValues.put("pub_publication", publication.getDescription());
				
				contentValues.put("pub_publication_date", sdf.format(
						(publication.getPublicationDate() == null 
							? new Date() : publication.getPublicationDate())));
				
				db.insert("d_publication", null, contentValues);
			}
			db.setTransactionSuccessful();

		} finally {
			db.endTransaction();
			
		}
		return newIdInserted;
	}

	@Deprecated
	public Cursor fetchAllSite() {
		String[] columns = { "_id", "syn_name", "syn_url", "syn_is_active", "syn_number_click" };
		return RepositoryHelper.getInstance(context).getReadableDatabase()
			.query("d_syndication", columns, null, null, null, null,	" syn_number_click desc ", null);
	}
	
	@Deprecated
	public void updateLastExtractTime(Integer id) {

		String now = sdf.format(new Date());
		ContentValues values = new ContentValues();
		values.put("syn_last_extract_time", now);
		 RepositoryHelper.getInstance(context).getWritableDatabase().update("d_syndication", values, " _id = ? ",
				new String[] { id.toString() });
		
	}
}
