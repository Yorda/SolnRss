package free.solnRss.provider;



import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.preference.PreferenceManager;
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
	private final static String syndicationTable = SyndicationTable.SYNDICATION_TABLE;
	private final static String publicationTable = PublicationTable.PUBLICATION_TABLE;
	
	private UriMatcher uriMatcher;
	// Used for the UriMacher
	private final int PUBLICATIONS     = 10;
	private final int SYNDICATION_ID   = 20;
	private final int PUBLICATION_ID   = 30;
	private final int CATEGORY_ID      = 40;
	private final int PUBLICATION_IN_SYNDICATION = 50;

	final public static String projection[] = new String[] {
			publicationTable + "." + PublicationTable.COLUMN_ID,
			publicationTable + "." + PublicationTable.COLUMN_TITLE, 
			publicationTable + "." + PublicationTable.COLUMN_LINK,
			publicationTable + "." + PublicationTable.COLUMN_ALREADY_READ, 
			syndicationTable + "." + SyndicationTable.COLUMN_NAME,
			publicationTable + "." + PublicationTable.COLUMN_PUBLICATION,
			publicationTable + "." + PublicationTable.COLUMN_SYNDICATION_ID };
	
	@Override
	public boolean onCreate() {
		repository = new Database(getContext());
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, PUBLICATION_PATH, PUBLICATIONS);
		uriMatcher.addURI(AUTHORITY, PUBLICATION_PATH + "/#", SYNDICATION_ID);
		uriMatcher.addURI(AUTHORITY, PUBLICATION_PATH + "/publicationId/#", PUBLICATION_ID);
		uriMatcher.addURI(AUTHORITY, PUBLICATION_PATH + "/categoryId/#", CATEGORY_ID);
		uriMatcher.addURI(AUTHORITY, PUBLICATION_PATH + "/publicationInSyndication/#", PUBLICATION_IN_SYNDICATION);
		return true;
	}
	
	private final String tables = publicationTable + " LEFT JOIN "
			+ syndicationTable + " ON " + syndicationTable + "."
			+ SyndicationTable.COLUMN_ID + " = " + publicationTable + "."
			+ PublicationTable.COLUMN_SYNDICATION_ID;
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] args, String sort) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		
		// Set the table
		queryBuilder.setTables(tables);

		StringBuilder where = new StringBuilder();
		if (!displayUnreadPublications()) {
			where.append(PublicationTable.COLUMN_ALREADY_READ);
			where.append(" = 0 ");
		}
		
		switch (uriMatcher.match(uri)) {
			case PUBLICATIONS:
				// For all publications filter who are authorized to be displayed on main time line
				where = addWhereClauses(where, SyndicationTable.COLUMN_DISPLAY_ON_TIMELINE + "= 1 ");
			break;
				
			case PUBLICATION_IN_SYNDICATION:
				// No filter read / unread
				where = new StringBuilder();
			break;
				
			case SYNDICATION_ID:
				String syndicationId =  uri.getLastPathSegment();
				where = addWhereClauses(where, PublicationTable.COLUMN_SYNDICATION_ID + "=" + syndicationId);
			break;
			
			case CATEGORY_ID:
				String categoryId =  uri.getLastPathSegment();
				where = addWhereClauses(where, 
						PublicationTable.COLUMN_SYNDICATION_ID
						+ " in (select syn_syndication_id from d_categorie_syndication where cas_categorie_id = " 
						+ categoryId + ")");
			break;
			
			default: throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		if (where.length() != 0) {
			queryBuilder.appendWhere(where.toString());
		}
		
		SQLiteDatabase db = repository.getReadableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection,
				args, null, null,
				PublicationTable.COLUMN_PUBLICATION_DATE + " desc");
		
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	/*
	 * Add a where clause to string builder
	 */
	private StringBuilder addWhereClauses(StringBuilder where, String clause) {
		if (where.length() != 0) {
			where.append(" and ");
		}
		where.append(clause);
		return where;
	}
	
	private boolean displayUnreadPublications() {
		return PreferenceManager.getDefaultSharedPreferences(getContext())
					.getBoolean("pref_display_unread", true);
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