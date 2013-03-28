package free.solnRss.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import free.solnRss.repository.Database;
import free.solnRss.repository.SyndicationTable;

public class SyndicationsProvider extends ContentProvider {
	private final static String 
			AUTHORITY        = "com.solnRss.provider.syndicationprovider",
			SYNDICATION_PATH = "syndications",
			MIME 			 = "vnd.android.cursor.item/vnd.com.soln.rss.provider.syndications";
	public final static Uri 
			URI = Uri.parse("content://" + AUTHORITY + "/" + SYNDICATION_PATH);

	private UriMatcher uriMatcher;
	private final int SYNDICATIONS = 10;
	private final int SYNDICATION_ID = 20;

	private Database repository;
	
	@Override
	public boolean onCreate() {
		repository = new Database(getContext());
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, SYNDICATION_PATH, SYNDICATIONS);
		uriMatcher.addURI(AUTHORITY, SYNDICATION_PATH + "/#", SYNDICATION_ID);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, 
			String selection, String[] args, String sort) {

		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(SyndicationTable.SYNDICATION_TABLE);

		switch (uriMatcher.match(uri)) {
		case SYNDICATIONS:
			break;

		case SYNDICATION_ID:
			queryBuilder.appendWhere(SyndicationTable.COLUMN_ID + "=" + uri.getLastPathSegment());
			break;

		default: throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		SQLiteDatabase db = repository.getReadableDatabase();

		if(TextUtils.isEmpty(sort)){
			sort = SyndicationTable.COLUMN_NUMBER_CLICK + " desc";
		}

		Cursor cursor = queryBuilder.query(db, projection, selection, args, null, null, sort);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, 
			String selection, String[] selectionArgs) {
		return 0;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		return MIME;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

}
