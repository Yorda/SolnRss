package free.solnRss.repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Database extends SQLiteOpenHelper {

	public static int VERSION = 6;
	public static String DATABASE_NAME = "SOLNRSS.db";
	
	private String SCHEMA_NAME = "schema.sql";
	private Context context;

	public Database(Context context) {
		super(context, DATABASE_NAME, null, VERSION);
	}
	
	public Database(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		setDatabase(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < newVersion) {
			if (newVersion == 2) {
				String req = "alter table d_publication add pub_publication text ";
				db.execSQL(req);
			}
			if (newVersion == 3) {
				System.err.println("UPDATE DB FOR V 3");
				String req = "create table d_categorie (\r\n" + 
						"	_id INTEGER PRIMARY KEY autoincrement,\r\n" + 
						"	cat_name text NOT NULL\r\n" + 
						"); ";
				
				req += "create table d_categorie_syndication (\r\n" + 
						"	_id INTEGER PRIMARY KEY autoincrement,\r\n" + 
						"	FOREIGN KEY(cas_categorie_id) REFERENCES d_categorie( _id),\r\n" + 
						"	FOREIGN KEY(cas_syndication_id) REFERENCES d_syndication( _id)\r\n" + 
						");";
				db.execSQL(req);
			}
		}
		if (newVersion == 4) {
			System.err.println("UPDATE DB FOR V 4");
			String req = "create table d_categorie_syndication (\r\n" + 
					"	_id INTEGER PRIMARY KEY autoincrement,\r\n" + 
					"	cas_categorie_id INTEGER NOT NULL,\r\n" + 
					"	cas_syndication_id INTEGER NOT NULL,\r\n" + 
					"	FOREIGN KEY(cas_categorie_id) REFERENCES d_categorie( _id),\r\n" + 
					"	FOREIGN KEY(cas_syndication_id) REFERENCES d_syndication( _id)\r\n" + 
					");";
			db.execSQL(req);
		}
		if (newVersion == 6) {
			System.err.println("UPDATE DB FOR V 5");
			
			String req = "ALTER TABLE d_categorie_syndication RENAME TO tmp_d_categorie_syndication; ";
			
			db.execSQL(req);
			
			req = "create table d_categorie_syndication (\r\n" + 
					"	_id INTEGER PRIMARY KEY autoincrement,\r\n" + 
					"	cas_categorie_id INTEGER NOT NULL,\r\n" + 
					"	syn_syndication_id INTEGER NOT NULL,\r\n" + 
					"	FOREIGN KEY(cas_categorie_id) REFERENCES d_categorie( _id),\r\n" + 
					"	FOREIGN KEY(syn_syndication_id) REFERENCES d_syndication( _id)\r\n" + 
					");";
			
			db.execSQL(req);

			req = "INSERT INTO d_categorie_syndication(_id, cas_categorie_id, syn_syndication_id) "
					+ " SELECT _id, cas_categorie_id,  cas_syndication_id "
					+ " from tmp_d_categorie_syndication; ";
			
			db.execSQL(req);

		}
	}

	private void setDatabase(SQLiteDatabase db) {
		try {
			InputStream is = 
					context.getResources().getAssets().open(SCHEMA_NAME);
			String[] statements = parseSqlFile(is);

			for (String statement : statements) {
				db.execSQL(statement);
			}
			
		} catch (IOException e) {
			Log.e(this.getClass().getName(), "Unable to create the database ");
			e.printStackTrace();
		} catch (Exception e) {
			Log.e(this.getClass().getName(), "Unable to create the database ");
			e.printStackTrace();
		}
	}

	public static String[] parseSqlFile(InputStream sqlFile) throws IOException {
		return parseSqlFile(new BufferedReader(new InputStreamReader(sqlFile)));
	}

	public static String[] parseSqlFile(BufferedReader sqlFile)
			throws IOException {

		String line;
		StringBuilder sql = new StringBuilder();
		String multiLineComment = null;

		while ((line = sqlFile.readLine()) != null) {
			line = line.trim();

			// Check for start of multi-line comment
			if (multiLineComment == null) {
				// Check for first multi-line comment type
				if (line.startsWith("/*")) {
					if (!line.endsWith("}")) {
						multiLineComment = "/*";
					}
					// Check for second multi-line comment type
				} else if (line.startsWith("{")) {
					if (!line.endsWith("}")) {
						multiLineComment = "{";
					}
					// Append line if line is not empty or a single line comment
				} else if (!line.startsWith("--") && !line.equals("")) {
					sql.append(line);
				} // Check for matching end comment
			} else if (multiLineComment.equals("/*")) {
				if (line.endsWith("*/")) {
					multiLineComment = null;
				}
				// Check for matching end comment
			} else if (multiLineComment.equals("{")) {
				if (line.endsWith("}")) {
					multiLineComment = null;
				}
			}
		}
		sqlFile.close();
		return sql.toString().split(";");
	}

	//Add default site
			/*String sql = "insert into d_syndication values(1,'Gizmodo', " +
					"'http://www.gizmodo.fr/feed','http://www.gizmodo.fr', 0,0, '2012-12-07 13:42:21', '2012-11-30 10:22:58'  )";
			
			db.execSQL(sql);
			
			sql = "insert into d_syndication values(2,'Planete libre', " +
					"'http://www.planet-libre.org/feed.php?type=atom','http://www.planet-libre.org', 0,0, '2012-12-07 15:42:21', '2012-11-30 11:22:58'  )";
			
			db.execSQL(sql);*/
}