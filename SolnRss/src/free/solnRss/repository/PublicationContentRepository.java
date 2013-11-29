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
	private Uri uri = Uri.parse(SolnRssProvider.URI + "/publicationContent");
	
	private Context context;
	private StringBuilder selection = new StringBuilder();
	private List<String> args = new ArrayList<String>();

	public final String projection[] = new String[] {
		publicationContentTable + "." + PublicationContentTable.COLUMN_ID,
		publicationContentTable + "." + PublicationContentTable.COLUMN_LINK,
		publicationContentTable + "." + PublicationContentTable.COLUMN_PUBLICATION,
		publicationContentTable + "." + PublicationContentTable.COLUMN_PUBLICATION_ID 
	};
	
	public String[] retrievePublicationContent(Integer publicationId) {

		selection.setLength(0);
		args.clear();

		selection.append(PublicationContentTable.COLUMN_PUBLICATION_ID);
		selection.append(" = ? ");

		args.add(publicationId.toString());

		Cursor c = context.getContentResolver().query(uri, projection,
				selection.toString(), args.toArray(new String[args.size()]), null);
		c.moveToFirst();

		return new String[] { c.getString(1), c.getString(2) };
	}
}
