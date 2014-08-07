package free.solnRss.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import free.solnRss.repository.CategoryRepository;
import free.solnRss.repository.CategoryTable;
import free.solnRss.repository.PublicationContentRepository;
import free.solnRss.repository.PublicationContentTable;
import free.solnRss.repository.PublicationRepository;
import free.solnRss.repository.PublicationTable;
import free.solnRss.repository.RepositoryHelper;
import free.solnRss.repository.RssTable;
import free.solnRss.repository.SyndicationRepository;
import free.solnRss.repository.SyndicationTable;
import free.solnRss.repository.SyndicationsByCategoryRepository;

public class SolnRssProvider extends ContentProvider {

	public final static String AUTHORITY = "com.solnRss.provider.solnRssProvider";
	private final static String PATH = "soln.r";
	public final static Uri URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);

	private UriMatcher uriMatcher;
	private final int PUBLICATION = 10, CATEGORY = 20, SYNDICATION = 30,
			SYNDICATIONS_BY_CATEGORY = 40, RSS = 50, CATEGORY_NAME = 60,
			PUBLICATION_CONTENT = 70, PUBLICATION_CONTENT_DB = 80;

	@Override
	public boolean onCreate() {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, PATH + "/publication", PUBLICATION);
		uriMatcher.addURI(AUTHORITY, PATH + "/category", CATEGORY);
		uriMatcher.addURI(AUTHORITY, PATH + "/syndication", SYNDICATION);
		uriMatcher.addURI(AUTHORITY, PATH + "/syndicationsByCategory/#", SYNDICATIONS_BY_CATEGORY);
		uriMatcher.addURI(AUTHORITY, PATH + "/rss", RSS);
		uriMatcher.addURI(AUTHORITY, PATH + "/category_name", CATEGORY_NAME);
		uriMatcher.addURI(AUTHORITY, PATH + "/publicationContent", PUBLICATION_CONTENT);
		uriMatcher.addURI(AUTHORITY, PATH + "/publicationContentUpdateDB", PUBLICATION_CONTENT_DB);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = RepositoryHelper.getInstance(getContext())
				.getReadableDatabase();
		Cursor cursor = null;
		
		switch (uriMatcher.match(uri)) {
		case PUBLICATION:
			
			cursor = db.query(PublicationRepository.publicationTableJoinToSyndication, projection,
					selection, selectionArgs, null, null, PublicationRepository.orderBy(getContext()), null);
					//PublicationRepository.publicationsQueryLimit(uri.getQueryParameter("page"),getContext()));
			break;
			
		case CATEGORY:
			cursor = db.query(CategoryRepository.categoryTableJoinToSyndication, projection, selection, selectionArgs, 
					CategoryRepository.groupBy(), null, CategoryRepository.orderBy(getContext()));
			break;
			
		case SYNDICATION:
			cursor = db.query(SyndicationTable.SYNDICATION_TABLE, projection,
					selection, selectionArgs, null, null, SyndicationRepository.orderBy(getContext()));
			break;
			
		case SYNDICATIONS_BY_CATEGORY:
			cursor = db.query(SyndicationsByCategoryRepository.syndicationsByCategoryTable + uri.getLastPathSegment(), projection,
					selection, selectionArgs, null, null, SyndicationRepository.orderBy(getContext()));
			break;

		case RSS:
			cursor = db.query(RssTable.RSS_TABLE, projection,
					selection, selectionArgs, null, null, " _id desc ");
			break;
			
		case CATEGORY_NAME:
			cursor = db.query(CategoryTable.CATEGORY_TABLE, projection, selection, selectionArgs, 
					null, null, null);
			break;
			
		case PUBLICATION_CONTENT:
			String tableKey = uri.getQueryParameter("tableKey");
			cursor = db.query(PublicationContentTable.PUBLICATION_CONTENT_TABLE
					+ "_" + tableKey, projection, selection, selectionArgs,
					null, null, null);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int rowsUpdated = 0;
		SQLiteDatabase db = RepositoryHelper.getInstance(getContext())
				.getWritableDatabase();
		
		switch (uriMatcher.match(uri)) {
		case PUBLICATION:
			rowsUpdated = db.update(PublicationTable.PUBLICATION_TABLE, values,
					selection, selectionArgs);
			break;

		case CATEGORY:
			rowsUpdated = db.update(CategoryTable.CATEGORY_TABLE, values,
					selection, selectionArgs);
			break;

		case SYNDICATION:
			rowsUpdated = db.update(SyndicationTable.SYNDICATION_TABLE, values,
					selection, selectionArgs);
			break;
			
		case RSS:
			rowsUpdated = db.update(RssTable.RSS_TABLE, values,
					selection, selectionArgs);
			break;
			
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long id = 0;
		SQLiteDatabase db = RepositoryHelper.getInstance(getContext())
				.getWritableDatabase();
		
		switch (uriMatcher.match(uri)) {
		case PUBLICATION:
			id = db.insert(PublicationTable.PUBLICATION_TABLE, null, values);
			// Not refresh after recorded new publication
			break;

		case CATEGORY:
			id = db.insert(CategoryTable.CATEGORY_TABLE, null, values);
			getContext().getContentResolver().notifyChange(uri, null);
			break;

		case SYNDICATION:
			id = db.insert(SyndicationTable.SYNDICATION_TABLE, null, values);
			getContext().getContentResolver().notifyChange(uri, null);
			break;
			
		case RSS:
			id = db.insert(RssTable.RSS_TABLE, null, values);
			getContext().getContentResolver().notifyChange(uri, null);
			break;

		case PUBLICATION_CONTENT:
			String tableKey = values.getAsString("syndicationId");
			values.remove("syndicationId");
			
			id = db.insert(PublicationContentTable.PUBLICATION_CONTENT_TABLE + "_" + tableKey, null, values);
			getContext().getContentResolver().notifyChange(uri, null);

			break;
			
		case PUBLICATION_CONTENT_DB:
			db.execSQL(PublicationContentRepository.newPublicationTableSqlReq(values.getAsString("syn_syndication_id")));
			break;
			
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		return Uri.parse(PATH + "/" + id);
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		return super.bulkInsert(uri, values);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {

		int rowsDeleted = 0;
		SQLiteDatabase db = RepositoryHelper.getInstance(getContext())
				.getWritableDatabase();
		switch (uriMatcher.match(uri)) {
		case PUBLICATION:
			rowsDeleted = db.delete(PublicationTable.PUBLICATION_TABLE,
					selection, selectionArgs);
			break;

		case CATEGORY:
			rowsDeleted = db.delete(CategoryTable.CATEGORY_TABLE, selection,
					selectionArgs);
			break;

		case SYNDICATION:
			rowsDeleted = db.delete(SyndicationTable.SYNDICATION_TABLE,
					selection, selectionArgs);
			break;

		case RSS:
			rowsDeleted = db.delete(RssTable.RSS_TABLE,
					selection, selectionArgs);
			break;
			
		case PUBLICATION_CONTENT:
			String tableKey = uri.getQueryParameter("tableKey");
			rowsDeleted = db.delete(PublicationContentTable.PUBLICATION_CONTENT_TABLE  + "_" + tableKey,
					selection, selectionArgs);
			break;
			
		case PUBLICATION_CONTENT_DB:
			db.execSQL(PublicationContentRepository.dropPublicationTableSqlReq(null));
			break;
			
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}
}
