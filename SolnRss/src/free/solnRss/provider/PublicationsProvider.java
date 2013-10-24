package free.solnRss.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.preference.PreferenceManager;
import free.solnRss.repository.PublicationTable;
import free.solnRss.repository.RepositoryHelper;
import free.solnRss.repository.SyndicationTable;

@Deprecated
public class PublicationsProvider extends ContentProvider {
	public static final String CONTENT_PROVIDER_MIME = 
			"vnd.android.cursor.item/vnd.com.soln.rss.provider.publications",
								AUTHORITY = 
			"com.solnRss.provider.publicationprovider",
								PUBLICATION_PATH = "publications";
	public  final static Uri URI = 
			Uri.parse("content://" + AUTHORITY + "/" + PUBLICATION_PATH);
	
	private final static String syndicationTable = SyndicationTable.SYNDICATION_TABLE;
	private final static String publicationTable = PublicationTable.PUBLICATION_TABLE;
	
	private UriMatcher uriMatcher;
	// Used for the UriMacher
	private final int PUBLICATIONS     = 10;
	
	final public static String projection[] = new String[] {
			publicationTable + "." + PublicationTable.COLUMN_ID,
			publicationTable + "." + PublicationTable.COLUMN_TITLE, 
			publicationTable + "." + PublicationTable.COLUMN_LINK,
			publicationTable + "." + PublicationTable.COLUMN_ALREADY_READ, 
			syndicationTable + "." + SyndicationTable.COLUMN_NAME,
			publicationTable + "." + PublicationTable.COLUMN_PUBLICATION,
			publicationTable + "." + PublicationTable.COLUMN_SYNDICATION_ID };
			//publicationTable + "." + PublicationTable.COLUMN_PUBLICATION_DATE};
	
	@Override
	public boolean onCreate() {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, PUBLICATION_PATH, PUBLICATIONS);
		queryBuilder.setTables(tables);
		return true;
	}
	
	SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
	
	public static final String tables = publicationTable + " LEFT JOIN "
			+ syndicationTable + " ON " + syndicationTable + "."
			+ SyndicationTable.COLUMN_ID + " = " + publicationTable + "."
			+ PublicationTable.COLUMN_SYNDICATION_ID;

	
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] args, String sort) {
		SQLiteDatabase db = RepositoryHelper.getInstance(getContext()).getReadableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection,
				args, null, null,
				PublicationTable.COLUMN_PUBLICATION_DATE + " desc",  maxItemsInList());
		
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		
	    long id = 0;
	    switch (uriMatcher.match(uri)) {
			case PUBLICATIONS:
				SQLiteDatabase db = RepositoryHelper.getInstance(getContext()).getWritableDatabase();
				id = db.insert(PublicationTable.PUBLICATION_TABLE, null, values);
			break;
		
			default: throw new IllegalArgumentException("Unknown URI: " + uri);
	    }
	    // Not refresh after recorded new publication
	    // getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(PUBLICATION_PATH + "/publicationId/" + id);
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		
		int rowsUpdated = 0;
		SQLiteDatabase db = RepositoryHelper.getInstance(getContext()).getWritableDatabase();
		rowsUpdated = db.update(PublicationTable.PUBLICATION_TABLE, values, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = RepositoryHelper.getInstance(getContext()).getWritableDatabase();
		db.delete(PublicationTable.PUBLICATION_TABLE, null, null);
		return 0;
	}
	
	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		return super.bulkInsert(uri, values);
	}
	
	private String maxItemsInList() {
		int max = PreferenceManager.getDefaultSharedPreferences(getContext())
				.getInt("pref_max_publication_item", 100);
		return Integer.valueOf(max).toString();
	}
	
	@Override
	public String getType(Uri uri) {
		return CONTENT_PROVIDER_MIME;
	}
}