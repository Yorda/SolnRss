package free.solnRss.repository;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntry;

import free.solnRss.model.Publication;
import free.solnRss.model.Syndication;
import free.solnRss.provider.SolnRssProvider;
import free.solnRss.utility.SyndicateUtil;

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

	public void updateLastUpdateSyndicationTime(List<Syndication> syndications) {
		ContentValues values = null;
		for (Syndication syndication : syndications) {

			values = new ContentValues();
			
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
	
	public Cursor findSyndicationsToRefresh(Date timeToRefresh) {
		String projection[] = new String[] {
				SyndicationTable.SYNDICATION_TABLE + "."
						+ SyndicationTable.COLUMN_ID,
				SyndicationTable.SYNDICATION_TABLE + "."
						+ SyndicationTable.COLUMN_URL, };

		String selection = "syn_last_extract_time < Datetime(?) and syn_is_active = ? ";
		String[] selectionArgs = new String[2];
		selectionArgs[0] = sdf.format(timeToRefresh);
		selectionArgs[1] = "0";

		return context.getContentResolver().query(uri, projection, selection,
				selectionArgs, SyndicationTable.COLUMN_ID + " asc ");
	}

	public long addWebSite(Syndication syndication) throws Exception {
		String now = sdf.format(new Date());
		Long newSyndicationId = null;
		
		ContentValues cv = new ContentValues();
		cv.put("syn_name", syndication.getName());
		cv.put("syn_url", syndication.getUrl());
		cv.put("syn_website_url", syndication.getWebsiteUrl());
		cv.put("syn_creation_date", now);
		cv.put("syn_last_extract_time", now);
		cv.put("syn_is_active", 0);
		cv.put("syn_display_on_timeline", 1);
		cv.put("syn_number_click", 0);

		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		operations.add(ContentProviderOperation.newInsert(uri).withValues(cv)
				.withYieldAllowed(true).build());
		
		for (Publication publication : syndication.getPublications()) {
			
			cv = new ContentValues();
			//cv.put("pub_link", publication.getUrl());
			cv.put("pub_title", publication.getTitle());
			cv.put("pub_already_read", 0);
			//cv.put("pub_publication", publication.getDescription());
			
			cv.put("pub_publication_date", sdf.format(
					(publication.getPublicationDate() == null 
						? new Date() : publication.getPublicationDate())));
			
			operations.add(ContentProviderOperation
					.newInsert(Uri.parse(SolnRssProvider.URI + "/publication"))
					.withValues(cv)
					.withValueBackReference("syn_syndication_id", 0)
					.withYieldAllowed(true).build());
			
			cv = new ContentValues();
			cv.put("pct_link", publication.getUrl());
			cv.put("pct_publication", publication.getDescription());
		
			operations.add(ContentProviderOperation
					.newInsert(PublicationContentRepository.uri)
					.withValues(cv)
					.withValueBackReference("pub_publication_id", operations.size() - 1)
					.withYieldAllowed(true).build());
		}		

		String rss = syndication.getRss();
		if (!TextUtils.isEmpty(rss)) {
			SyndicateUtil syndicateUtil = new SyndicateUtil();
			syndicateUtil.init(rss);
			List<SyndEntry> entries = syndicateUtil.lastEntries();
			for (SyndEntry e : entries) {
				cv = new ContentValues();
				cv.put("rss_url", e.getLink());
				cv.put("rss_title", e.getTitle());
				operations.add(ContentProviderOperation
						.newInsert(Uri.parse(SolnRssProvider.URI + "/rss"))
						.withValues(cv)
						.withValueBackReference("syn_syndication_id", 0)
						.withYieldAllowed(true).build());
			}
		}
		
		ContentProviderResult[] res = context.getContentResolver().applyBatch(
				SolnRssProvider.AUTHORITY, operations);
		
		newSyndicationId = Long.valueOf(res[0].uri.getLastPathSegment());
		return newSyndicationId;
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
			db.delete("d_categorie_syndication", " syn_syndication_id = ? ", whereArgs);
			db.delete("d_publication_content", "pub_publication_id in (select _id from d_publication where syn_syndication_id = ? ) ", whereArgs);
			db.delete("d_publication", "syn_syndication_id = ? ", whereArgs);
			db.delete("d_syndication", "_id = ? ", whereArgs);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	public boolean isStillRecorded(String url) {
		SQLiteDatabase db  = RepositoryHelper.getInstance(context).getReadableDatabase();
		Cursor c = db.rawQuery(
				"select _id from d_syndication where syn_website_url = ? ",
				new String[] {url});

		int count = c.getCount();
		c.close();
		
		if (count > 0)
			return true;
		
		return false;
	}
}
