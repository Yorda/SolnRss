package free.solnRss.repository;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.SparseArray;
import free.solnRss.provider.SolnRssProvider;


public class RssRepository {

	private Context	context;

	public RssRepository(final Context context) {
		this.context = context;
	}

	public static final String	rssTable		= RssTable.RSS_TABLE;

	Uri							uri				= Uri.parse(SolnRssProvider.URI + "/rss");

	private StringBuilder		selection		= new StringBuilder();
	private List<String>		args			= new ArrayList<String>();

	public final String			rssProjection[]	= new String[] { RssTable.COLUMN_ID, RssTable.COLUMN_TITLE, RssTable.COLUMN_URL, };

	SparseArray<String>			lastRssFound	= new SparseArray<String>();

	public Cursor loadLastRssFound(final Integer syndicationId) {
		selection.setLength(0);
		selection.append(RssTable.COLUMN_SYNDICATION_ID + " = ? ");

		args.clear();
		args.add(syndicationId.toString());

		lastRssFound.clear();
		return context.getContentResolver().query(uri, rssProjection, selection.toString(), args.toArray(new String[args.size()]), null);
	}

	public boolean isAlreadyRetrieved(final String title, final String url) {

		selection.setLength(0);
		selection.append(RssTable.COLUMN_TITLE + " = ? ");

		//selection.append(" AND ");
		//selection.append(RssTable.COLUMN_URL + " = ? ");

		args.clear();
		args.add(title);
		//args.add(url);

		final Cursor cursor = context.getContentResolver().query(uri, new String[] { RssTable.COLUMN_ID }, selection.toString(),
				args.toArray(new String[args.size()]), null);

		boolean isAlreadyExist = false;
		if (cursor.getCount() > 0) {
			isAlreadyExist = true;
		}
		cursor.close();
		return isAlreadyExist;
	}
}
