package free.solnRss.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;
import free.solnRss.repository.Database;

public class PublicationsContentProvider extends ContentProvider {

	public static final Uri CONTENT_URI = Uri.parse("content://free.solnRss.provider.publicationcontentprovider");
	private Database database;
	
	@Override
	public boolean onCreate() {
		database = new Database(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		long id = getId(uri);
		SQLiteDatabase db = database.getReadableDatabase();
		
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables("");
		
		if (id < 0) {
			return db
					.query("TutosAndroidProvider.CONTENT_PROVIDER_TABLE_NAME",
							projection, selection, selectionArgs, null, null,
							sortOrder);
		} else {
			return db.query("TutosAndroidProvider.CONTENT_PROVIDER_TABLE_NAME",
					projection, "Cours.COURS_ID" + "=" + id, null, null, null,
					null);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = database.getWritableDatabase();
		try {
			long id = db.insertOrThrow(
					"TutosAndroidProvider.CONTENT_PROVIDER_TABLE_NAME", null,
					values);

			if (id == -1) {
				throw new RuntimeException(String.format(
						"%s : Failed to insert [%s] for unknown reasons.",
						"TutosAndroidProvider", values, uri));
			} else {
				return ContentUris.withAppendedId(uri, id);
			}

		} finally {
			db.close();
		}
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
		return null;
	}
	
	private long getId(Uri uri) {
		long id = -1;
		String lastPathSegment = uri.getLastPathSegment();
		
		if (lastPathSegment != null) {
			try {
				id = Long.parseLong(lastPathSegment);
			} catch (NumberFormatException e) {
				Log.e("TutosAndroidProvider", "Number Format Exception : " + e);
			}
		}
		
		return id;
	}
}
