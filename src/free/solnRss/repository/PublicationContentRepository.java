package free.solnRss.repository;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import free.solnRss.provider.SolnRssProvider;

public class PublicationContentRepository {

	public PublicationContentRepository(Context context) {
		this.context = context;
	}
	
	public static final String publicationContentTable = PublicationContentTable.PUBLICATION_CONTENT_TABLE;
	public static  Uri uri = Uri.parse(SolnRssProvider.URI + "/publicationContent");
	
	private Context context;
	private StringBuilder selection = new StringBuilder();
	private List<String> args = new ArrayList<String>();

	public final String projection[] = new String[] {
		publicationContentTable + "." + PublicationContentTable.COLUMN_ID,
		publicationContentTable + "." + PublicationContentTable.COLUMN_LINK,
		publicationContentTable + "." + PublicationContentTable.COLUMN_PUBLICATION,
		publicationContentTable + "." + PublicationContentTable.COLUMN_PUBLICATION_ID 
	};
	
	public static String dropPublicationTableSqlReq(String tableKey) {
		return "drop table d_publication_content_" + tableKey ;
	}
	
	public static String newPublicationTableSqlReq(String tableKey) {
		return "create table d_publication_content_" + tableKey
				+ " (\r\n"
				+ "	_id INTEGER PRIMARY KEY autoincrement,\r\n"
				+ "	pct_link text NOT NULL,\r\n"
				+ "	pct_publication text,\r\n"
				+ "	pub_publication_id INTEGER NOT NULL,\r\n"
				+ "	FOREIGN KEY(pub_publication_id) REFERENCES d_publication( _id)\r\n"
				+ "); \r\n";
	}
	
	public String[] retrievePublicationContent(Integer syndicationId, Integer publicationId) {

		selection.setLength(0);
		args.clear();

		selection.append(PublicationContentTable.COLUMN_PUBLICATION_ID);
		selection.append(" = ? ");

		args.add(publicationId.toString());

		Uri uri = Uri.parse(SolnRssProvider.URI + "/publicationContent").buildUpon()
				.appendQueryParameter("tableKey", syndicationId.toString()).build();
		 
		Cursor c = context.getContentResolver().query(uri, projection(syndicationId),
				selection.toString(), args.toArray(new String[args.size()]), null);
		c.moveToFirst();

		return new String[] { c.getString(1), c.getString(2) };
	}
	
	private String[] projection(Integer syndicationId) {
		String key = String.valueOf(syndicationId);
		return new String[] {
				publicationContentTable + "_" +key + "." + PublicationContentTable.COLUMN_ID,
				publicationContentTable + "_" +key + "." + PublicationContentTable.COLUMN_LINK,
				publicationContentTable + "_" +key + "." + PublicationContentTable.COLUMN_PUBLICATION,
				publicationContentTable + "_" +key + "." + PublicationContentTable.COLUMN_PUBLICATION_ID 
			};
	}
}
