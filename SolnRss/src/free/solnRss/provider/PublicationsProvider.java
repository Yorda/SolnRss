package free.solnRss.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import free.solnRss.repository.Database;

public class PublicationsProvider extends ContentProvider {

	private Database database;
	
	private final static String AUTHORITY =  "com.solnRss.provider.publicationprovider";
	private final static String PUBLICATION_PATH = "publications";
	public final static Uri URI = Uri.parse("content://" + AUTHORITY + "/" + PUBLICATION_PATH);
	 
	public final String CONTENT_TYPE      = ContentResolver.CURSOR_DIR_BASE_TYPE  + "/publications";
	public final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/publication";

	private UriMatcher uriMatcher;

	// Used for the UriMacher
	private final int PUBLICATIONS = 10;
	private final int SYNDICATION_ID = 20;

	@Override
	public boolean onCreate() {
		database = new Database(getContext());
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, PUBLICATION_PATH, PUBLICATIONS);
		uriMatcher.addURI(AUTHORITY, PUBLICATION_PATH + "/#", SYNDICATION_ID);
		return true;
	}
	
	public static final String CONTENT_PROVIDER_MIME = "vnd.android.cursor.item/vnd.com.soln.rss.provider.publications";

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		// Uisng SQLiteQueryBuilder instead of query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		// Set the table
		queryBuilder.setTables(SyndicationTable.SYNDICATION_TABLE
				+ " LEFT JOIN " + PublicationTable.PUBLICATION_TABLE 
				+ " ON "+ SyndicationTable.SYNDICATION_TABLE  + "." + SyndicationTable.COLUMN_ID 
				+ " = " + PublicationTable.PUBLICATION_TABLE  + "." + PublicationTable.COLUMN_SYNDICATION_ID);
		
		int uriType = uriMatcher.match(uri);

		switch (uriType) {
		case PUBLICATIONS:
			break;

		case SYNDICATION_ID:
			// Adding the syndication ID to the original query
			queryBuilder.appendWhere(PublicationTable.COLUMN_SYNDICATION_ID	+ "=" + uri.getLastPathSegment());
			break;
			
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		SQLiteDatabase db = database.getReadableDatabase();
		
		Cursor cursor = 
			queryBuilder.query(db, projection, selection, selectionArgs, null, null,
					PublicationTable.COLUMN_PUBLICATION_DATE + " desc");

		return cursor;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		return CONTENT_PROVIDER_MIME;
	}
}
