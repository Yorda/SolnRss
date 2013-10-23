package free.solnRss.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.preference.PreferenceManager;
import free.solnRss.repository.CategoryTable;
import free.solnRss.repository.PublicationRepository;
import free.solnRss.repository.PublicationTable;
import free.solnRss.repository.RepositoryHelper;
import free.solnRss.repository.SyndicationTable;

public class SolnRssProvider extends ContentProvider {

	private final static String AUTHORITY = "com.solnRss.provider.solnRssProvider";
	private final static String PATH = "soln.r";
	public final static Uri URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);

	private UriMatcher uriMatcher;
	private final int PUBLICATION = 10, CATEGORY = 20, SYNDICATION = 30;

	@Override
	public boolean onCreate() {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, PATH + "/publication", PUBLICATION);
		uriMatcher.addURI(AUTHORITY, PATH + "/category", CATEGORY);
		uriMatcher.addURI(AUTHORITY, PATH + "/syndication", SYNDICATION);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = RepositoryHelper.getInstance(getContext())
				.getReadableDatabase();
		Cursor cursor = null;
		//db.execSQL("PRAGMA synchronous=OFF");
		
		switch (uriMatcher.match(uri)) {
		case PUBLICATION:
			cursor = db.query(PublicationsProvider.tables, projection,
					selection, selectionArgs, null, null,
					PublicationTable.COLUMN_PUBLICATION_DATE + " desc", 
					PublicationRepository.publicationsQueryLimit(getContext()));
			break;
			
		case CATEGORY:
			break;
			
		case SYNDICATION:
			String orderBy = null;
			if (PreferenceManager.getDefaultSharedPreferences(getContext())
					.getBoolean("pref_sort_syndications", true)) {
				orderBy = SyndicationTable.COLUMN_NUMBER_CLICK + " desc";
			} else {
				orderBy = SyndicationTable.COLUMN_NAME + " asc";
			}
			cursor = db.query(SyndicationTable.SYNDICATION_TABLE, projection,
					selection, selectionArgs, null, null, orderBy);
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
