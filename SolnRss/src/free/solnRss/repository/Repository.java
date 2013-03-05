package free.solnRss.repository;


import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public abstract class Repository {
	Context context;
	SQLiteDatabase sqLiteDatabase;
	Database database;

	String name = Database.DATABASE_NAME;
	int version = Database.VERSION;

	public void open() throws SQLException {
		database = new Database(context, name, null, version);
		sqLiteDatabase = database.getWritableDatabase();
	}

	public void open(Context context) throws SQLException {
		database = new Database(context, name, null, version);
		sqLiteDatabase = database.getWritableDatabase();
		sqLiteDatabase.execSQL("PRAGMA synchronous=OFF");
	}

	public void close() {
		sqLiteDatabase.close();
	}

}
