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
import android.preference.PreferenceManager;
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
	
	public Cursor findSyndicationsToRefresh() {
		
		// Period in minute
		int refresh = PreferenceManager.getDefaultSharedPreferences(context)
				.getInt("pref_search_publication_time", 15);

		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(new Date());
		calendar.add(Calendar.MINUTE, -refresh);

		String projection[] = new String[] {
				SyndicationTable.SYNDICATION_TABLE + "."
						+ SyndicationTable.COLUMN_ID,
				SyndicationTable.SYNDICATION_TABLE + "."
						+ SyndicationTable.COLUMN_NAME,
				SyndicationTable.SYNDICATION_TABLE + "."
						+ SyndicationTable.COLUMN_LAST_RSS_PUBLISHED,
				SyndicationTable.SYNDICATION_TABLE + "."
						+ SyndicationTable.COLUMN_URL, };

		String selection = "syn_last_extract_time < Datetime(?) and syn_is_active = ? ";
		String[] selectionArgs = new String[2];
		selectionArgs[0] = sdf.format(calendar.getTime());
		selectionArgs[1] = "0";

		return context.getContentResolver().query(uri, projection, selection,
				selectionArgs, SyndicationTable.COLUMN_ID + " asc ");
	}

	public void updateLastUpdateSyndicationTime(List<Syndication> syndications) {
		ContentValues values = null;
		for (Syndication syndication : syndications) {

			values = new ContentValues();

			if (!TextUtils.isEmpty(syndication.getRss())) {
				values.put(SyndicationTable.COLUMN_LAST_RSS_PUBLISHED,
					syndication.getRss());
			}
			
			values.put(SyndicationTable.COLUMN_LAST_EXTRACT_TIME,
					sdf.format(new Date()));

			context.getContentResolver().update(uri, values, " _id = ? ",
					new String[] { syndication.getId().toString() });
		}
	}

	
	public void changeSyndicationDisplayMode(Integer id,
			Integer isDisplayOnMainTimeLine) {
		ContentValues values = new ContentValues();
		values.put("syn_display_on_timeline", isDisplayOnMainTimeLine);
		context.getContentResolver().update(uri, values, " _id = ? ",
				new String[] { id.toString() });
	}
	
	public void changeSyndicationActivityStatus(Integer id, Integer status) {
		ContentValues values = new ContentValues();
		values.put("syn_is_active", status);
		context.getContentResolver().update(uri, values, "_id = ? ",
				new String[] { id.toString() });
	}
	
	public static String orderBy(Context context) {
		String orderSyndicationBy = SyndicationTable.COLUMN_NAME + " asc";
		if (PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean("pref_sort_syndications", true)) {
			orderSyndicationBy = SyndicationTable.COLUMN_NUMBER_CLICK + " desc";
		}
		return orderSyndicationBy;
	}
	

	public void renameSyndication(Integer id, String newName) {
		ContentValues values = new ContentValues();
		values.put("syn_name",  newName);
		context.getContentResolver().update(uri, values,"_id = ? ",
				new String[] { id.toString() });
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

	public boolean isStillRecorded(String url) {
		String[] selectionArgs = new String[1];
		selectionArgs[0] = url;
		Cursor c = RepositoryHelper
				.getInstance(context)
				.getReadableDatabase()
				.rawQuery("select _id from d_syndication where syn_website_url = ? ",	selectionArgs);

		int count = c.getCount();

		if (count > 0)
			return true;
		c.close();
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
}
