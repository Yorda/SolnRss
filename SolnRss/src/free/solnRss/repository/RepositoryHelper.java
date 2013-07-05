package free.solnRss.repository;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class RepositoryHelper extends SQLiteOpenHelper {

	public static int VERSION = 9;
	public static String DATABASE_NAME = "SOLNRSS.db";

	private static RepositoryHelper instance;

	public RepositoryHelper(Context context) {
		super(context, DATABASE_NAME, null, VERSION);
	}

	/*
	 * In order to avoid database locked exception open only one connection for all application
	 */
	public static synchronized RepositoryHelper getInstance(Context context) {
		if (instance == null)
			instance = new RepositoryHelper(context);
		return instance;
	}

	public RepositoryHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
