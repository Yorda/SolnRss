package free.solnRss.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import free.solnRss.repository.CategorySyndicationsTable;
import free.solnRss.repository.Database;
import free.solnRss.repository.SyndicationTable;

public class SyndicationsProvider extends ContentProvider {
	private final static String 
		AUTHORITY        = "com.solnRss.provider.syndicationprovider",
		SYNDICATION_PATH = "syndications",
		MIME 			 = "vnd.android.cursor.item/vnd.com.soln.rss.provider.syndications";
	public final static Uri 
		URI = Uri.parse("content://" + AUTHORITY + "/" + SYNDICATION_PATH);

	private Database repository;
	
	private UriMatcher uriMatcher;
	private final int SYNDICATIONS               = 10;
	private final int SYNDICATION_ID             = 20;
	private final int ADD_CLICK_SYNDICATION_ID   = 30;
	private final int SYNDICATION_BY_CATEGORY_ID = 40;
	
	final static String syndicationTable = SyndicationTable.SYNDICATION_TABLE;
	public static String syndicationProjection[] = 
				new String[] {
			syndicationTable + "." + SyndicationTable.COLUMN_ID,
			syndicationTable + "." + SyndicationTable.COLUMN_NAME,
			syndicationTable + "." + SyndicationTable.COLUMN_URL,
			syndicationTable + "." + SyndicationTable.COLUMN_IS_ACTIVE,
			syndicationTable + "." + SyndicationTable.COLUMN_NUMBER_CLICK
		};
	
	final static String CategorySyndicationTable = CategorySyndicationsTable.CATEGORY_SYNDICATION_TABLE;
	public static String syndicationByCategoryProjection[] =
			new String[] {
		syndicationTable         + "." + SyndicationTable.COLUMN_ID,
		syndicationTable         + "." + SyndicationTable.COLUMN_NAME,
		CategorySyndicationTable + "." + CategorySyndicationsTable.COLUMN_CATEGORY_ID
	};
	
	@Override
	public boolean onCreate() {
		repository = new Database(getContext());
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, SYNDICATION_PATH, SYNDICATIONS);
		uriMatcher.addURI(AUTHORITY, SYNDICATION_PATH + "/#", SYNDICATION_ID);
		uriMatcher.addURI(AUTHORITY, SYNDICATION_PATH + "/click/#", ADD_CLICK_SYNDICATION_ID);
		uriMatcher.addURI(AUTHORITY, SYNDICATION_PATH + "/selectedCategoryId/#", SYNDICATION_BY_CATEGORY_ID);
		return true;
	}
	
	/**
	 select s._id, s.syn_name, cs.cas_categorie_id 
		from d_syndication s left join d_categorie_syndication cs on s._id = cs.syn_syndication_id 
		and cs.cas_categorie_id = ? 
		order by s.syn_number_click desc
	 */

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
				
			case SYNDICATION_BY_CATEGORY_ID:
			queryBuilder.setTables(SyndicationTable.SYNDICATION_TABLE
					+ " left join "
					+ CategorySyndicationsTable.CATEGORY_SYNDICATION_TABLE
					+ " on " + syndicationTable + "."
					+ SyndicationTable.COLUMN_ID + "="
					+ CategorySyndicationTable + "."
					+ CategorySyndicationsTable.COLUMN_SYNDICATION_ID
					+ " and "+ CategorySyndicationsTable.COLUMN_CATEGORY_ID 
					+ "=" + uri.getLastPathSegment());

			break;
				
			default: throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		
		if(TextUtils.isEmpty(sort)){
			sort = SyndicationTable.COLUMN_NUMBER_CLICK + " desc";
		}

		SQLiteDatabase db = repository.getReadableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection, args, null, null, sort);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, 
			String selection, String[] selectionArgs) {
		
		SQLiteDatabase db = repository.getWritableDatabase();
		int rowsUpdated = 0;
		int uriType = uriMatcher.match(uri);
		switch (uriType) {

			case ADD_CLICK_SYNDICATION_ID:
				String id = uri.getLastPathSegment();
				String[] args = new String[1];
				args[0] = id.toString();
				db.execSQL("update d_syndication set syn_number_click = (syn_number_click +1) where _id = ?", args);
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
		return MIME;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

}
