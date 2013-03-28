package free.solnRss.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import free.solnRss.repository.CategoryTable;
import free.solnRss.repository.Database;

public class CategoryProvider extends ContentProvider {
	private final static String 
			AUTHORITY        = "com.solnRss.provider.categoryprovider",
			CATEGORY_PATH = "categorys",
			MIME 			 = "vnd.android.cursor.item/vnd.com.soln.rss.provider.categorys";
	public final static Uri 
			URI = Uri.parse("content://" + AUTHORITY + "/" + CATEGORY_PATH);

	private UriMatcher uriMatcher;
	private final int CATEGORIES = 10;
	private final int CATEGORY_ID = 20;

	private Database repository;
	
	@Override
	public boolean onCreate() {
		repository = new Database(getContext());
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, CATEGORY_PATH, CATEGORIES);
		uriMatcher.addURI(AUTHORITY, CATEGORY_PATH + "/#", CATEGORY_ID);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, 
			String selection, String[] args, String sort) {

		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(CategoryTable.CATEGORY_TABLE);

		switch (uriMatcher.match(uri)) {
		case CATEGORIES:
			break;

		case CATEGORY_ID:
			queryBuilder.appendWhere(CategoryTable.COLUMN_ID + "=" + uri.getLastPathSegment());
			break;

		default: throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		SQLiteDatabase db = repository.getReadableDatabase();
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
