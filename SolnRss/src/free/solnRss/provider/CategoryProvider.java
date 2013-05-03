package free.solnRss.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.preference.PreferenceManager;
import free.solnRss.repository.CategoryTable;
import free.solnRss.repository.Database;

public class CategoryProvider extends ContentProvider {
	private final static String AUTHORITY = "com.solnRss.provider.categoryprovider",
			CATEGORY_PATH = "categorys",
			MIME = "vnd.android.cursor.item/vnd.com.soln.rss.provider.categorys";
	public final static Uri URI = Uri.parse("content://" + AUTHORITY + "/"
			+ CATEGORY_PATH);

	private UriMatcher uriMatcher;
	private final int CATEGORIES  = 10;
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

		SQLiteQueryBuilder queryBuilder = null;
		Cursor cursor = null;
		SQLiteDatabase db = repository.getReadableDatabase();
		
		switch (uriMatcher.match(uri)) {
		case CATEGORIES:
			
			sort = "order by c.cat_name asc";
			
			if(sortByMostUsed()){
				sort = " order by number_of_use desc";
			}
			
			cursor = db.rawQuery("select c.*, count(cs.cas_categorie_id) as number_of_use " +
					"from d_categorie c left join d_categorie_syndication cs on c._id = cs.cas_categorie_id " +
					"group by c.cat_name " +
					sort , null);
			break;

		case CATEGORY_ID:
			queryBuilder = new SQLiteQueryBuilder();
			queryBuilder.setTables(CategoryTable.CATEGORY_TABLE);
			queryBuilder.appendWhere(CategoryTable.COLUMN_ID + "=" + uri.getLastPathSegment());
			cursor = queryBuilder.query(db, projection, selection, args, null, null, sort);
			break;

		default: throw new IllegalArgumentException("Unknown URI: " + uri);
		
		}
		
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}
	
	public boolean sortByMostUsed() {
		return PreferenceManager.getDefaultSharedPreferences(getContext())
				.getBoolean("pref_sort_categories", true);
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
