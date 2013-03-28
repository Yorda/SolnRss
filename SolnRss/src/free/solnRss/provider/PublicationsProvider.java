package free.solnRss.provider;



import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import free.solnRss.repository.Database;
import free.solnRss.repository.PublicationTable;
import free.solnRss.repository.SyndicationTable;

public class PublicationsProvider extends ContentProvider {
	public static final String CONTENT_PROVIDER_MIME = 
			"vnd.android.cursor.item/vnd.com.soln.rss.provider.publications",
								AUTHORITY = 
			"com.solnRss.provider.publicationprovider",
								PUBLICATION_PATH = "publications";
	public  final static Uri URI = 
			Uri.parse("content://" + AUTHORITY + "/" + PUBLICATION_PATH);
	
	private Database repository;
	private final String syndicationTable = SyndicationTable.SYNDICATION_TABLE;
	private final String publicationTable = PublicationTable.PUBLICATION_TABLE;
	
	private UriMatcher uriMatcher;
	// Used for the UriMacher
	private final int PUBLICATIONS = 10;
	private final int SYNDICATION_ID = 20;
	private final int PUBLICATION_ID = 30;

	@Override
	public boolean onCreate() {
		repository = new Database(getContext());
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, PUBLICATION_PATH, PUBLICATIONS);
		uriMatcher.addURI(AUTHORITY, PUBLICATION_PATH + "/#", SYNDICATION_ID);
		uriMatcher.addURI(AUTHORITY, PUBLICATION_PATH + "/publicationId/#", PUBLICATION_ID);
		return true;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] args, String sort) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		
		// Set the table
		queryBuilder.setTables(
				publicationTable + " LEFT JOIN " + syndicationTable   
			+ " ON "+ syndicationTable  + "." + SyndicationTable.COLUMN_ID 
			+ " = " + publicationTable  + "." + PublicationTable.COLUMN_SYNDICATION_ID);

		switch (uriMatcher.match(uri)) {
			case PUBLICATIONS:
				queryBuilder.appendWhere(PublicationTable.COLUMN_ALREADY_READ + " = 0 ");
			break;
				
			case SYNDICATION_ID:
				String requestedId =  uri.getLastPathSegment();
				queryBuilder.appendWhere(PublicationTable.COLUMN_SYNDICATION_ID + "=" + requestedId);
			break;
			
			default: throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		SQLiteDatabase db = repository.getReadableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection,
				args, null, null,
				PublicationTable.COLUMN_PUBLICATION_DATE + " desc");
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		
		return cursor;
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		return super.bulkInsert(uri, values);
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		
		SQLiteDatabase db = repository.getWritableDatabase();
	    long id = 0;
	    switch (uriMatcher.match(uri)) {
			case PUBLICATIONS:
				id = db.insert(PublicationTable.PUBLICATION_TABLE, null, values);
			break;
		
			default: throw new IllegalArgumentException("Unknown URI: " + uri);
	    }
	    // Not refresh after recorded new publication
	    // getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(PUBLICATION_PATH + "/publicationId/" + id);
	}
	

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = repository.getWritableDatabase();
		int rowsUpdated = 0;
		int uriType = uriMatcher.match(uri);
		switch (uriType) {

			case PUBLICATIONS:
			break;
	
			case PUBLICATION_ID:
				String id = uri.getLastPathSegment();
				rowsUpdated = db.update(
						PublicationTable.PUBLICATION_TABLE, values,
						PublicationTable.COLUMN_ID + "=" + id, null);
			break;
	
			default: throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
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