package free.solnRss.repository;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import free.solnRss.provider.SolnRssProvider;

public class PublicationRepository {

	public  static final String publicationTable = PublicationTable.PUBLICATION_TABLE;
	private final Uri uri = Uri.parse(SolnRssProvider.URI + "/publication");
	
	private Context context;
	private StringBuilder selection = new StringBuilder();
	private List<String> args = new ArrayList<String>();

	public final String projection[] = new String[] {
			publicationTable + "." + PublicationTable.COLUMN_ID,
			publicationTable + "." + PublicationTable.COLUMN_TITLE,
			publicationTable + "." + PublicationTable.COLUMN_LINK,
			publicationTable + "." + PublicationTable.COLUMN_ALREADY_READ,
			SyndicationTable.SYNDICATION_TABLE + "." + SyndicationTable.COLUMN_NAME,
			publicationTable + "." + PublicationTable.COLUMN_PUBLICATION,
			publicationTable + "." + PublicationTable.COLUMN_SYNDICATION_ID,
			SyndicationTable.SYNDICATION_TABLE + "." + SyndicationTable.COLUMN_NUMBER_CLICK };
	
	
	public static final String publicationTableJoinToSyndication = 
		publicationTable + " LEFT JOIN "
			+ SyndicationRepository.syndicationTable + " ON " + SyndicationRepository.syndicationTable + "."
			+ SyndicationTable.COLUMN_ID + " = " + publicationTable + "."+ PublicationTable.COLUMN_SYNDICATION_ID;
	
	public PublicationRepository(Context context) {
		this.context = context;
	}
	
	/**
	 * Clean the publications list in table for a syndication
	 * @param id
	 */
	public CursorLoader loadPublications(String filterText,
			Integer selectedSyndicationID, Integer selectedCategoryID,
			Boolean displayAlreadyRead) {
		
		selection.setLength(0);
		selection.append(" 1 = 1 ") ;
		args.clear();
		
		if (!TextUtils.isEmpty(filterText)) {
			
			selection.append(" and ");
			selection.append( PublicationTable.COLUMN_TITLE);
			selection.append(" like ? ");
			args.add("%" + filterText + "%");
		}
		
		// Display on time line
		if (selectedSyndicationID == null && selectedCategoryID == null) {
			selection.append(" and ");
			selection.append(SyndicationTable.COLUMN_DISPLAY_ON_TIMELINE);
			selection.append(" = 1 ");
		}

		else if (selectedSyndicationID != null) {
			selection.append(" and ");
			selection.append( PublicationTable.COLUMN_SYNDICATION_ID);
			selection.append(" = ? ");
			
			args.add(selectedSyndicationID.toString());
		}

		else if (selectedCategoryID != null) {
			selection.append(" and ");
			selection.append( PublicationTable.COLUMN_SYNDICATION_ID);
			selection.append(" in (select syn_syndication_id from d_categorie_syndication where cas_categorie_id = ?) ");
			args.add(selectedCategoryID.toString());
		}

		if (!displayAlreadyRead) {	
			selection.append(" and ");
			selection.append( PublicationTable.COLUMN_ALREADY_READ);
			selection.append(" = 0 ");
		}
		
		return new CursorLoader(context, uri, projection, selection.toString(),
				args.toArray(new String[args.size()]), null);
		
	}
	
	public void markPublicationAsRead(int publicationId) {
		ContentValues values = new ContentValues();
		values.put(PublicationTable.COLUMN_ALREADY_READ, "1");
		String where = PublicationTable.COLUMN_ID + " = ? ";
		String args[] = { String.valueOf(publicationId) };
		context.getContentResolver().update(uri, values, where, args);
	}
	
	public void markCategoryPublicationsAsRead(Integer categoryId){
		ContentValues values = new ContentValues();
		values.put(PublicationTable.COLUMN_ALREADY_READ, "1");
		String selection = " syn_syndication_id in (select syn_syndication_id from d_categorie_syndication where cas_categorie_id = ?) ";
		String[] args = {categoryId.toString()};
		context.getContentResolver().update(uri, values, selection, args);
	}
	
	public void markSyndicationPublicationsAsRead(Integer syndicationId) {
		ContentValues values = new ContentValues();
		values.put(PublicationTable.COLUMN_ALREADY_READ, "1");
		String selection = " syn_syndication_id  = ?  ";
		String[] args = { syndicationId.toString() };
		context.getContentResolver().update(uri, values, selection, args);
	}
	
	public void markAllPublicationsAsRead() {
		ContentValues values = new ContentValues();
		values.put(PublicationTable.COLUMN_ALREADY_READ, "1");
		context.getContentResolver().update(uri, values, null, null);
	}
	
	public void deletePublications(Integer id) {
		context.getContentResolver().delete(uri, " syn_syndication_id = ? ",
				new String[] { id.toString() });
	}
	
	public boolean isPublicationAlreadyRecorded(Integer syndicationId,
			String title, String url) {

		selection.setLength(0);
		args.clear();

		selection.append(PublicationTable.COLUMN_SYNDICATION_ID);
		selection.append(" = ? ");
		args.add(syndicationId.toString());

		selection.append(" AND ");

		selection.append(publicationTable + "."
				+ PublicationTable.COLUMN_TITLE);
		selection.append(" = ? ");
		args.add(title);

		selection.append(" AND ");

		selection.append(publicationTable + "."
				+ PublicationTable.COLUMN_LINK);
		selection.append(" = ? ");
		args.add(url);

		Cursor cursor = context.getContentResolver().query(
				uri,
				new String[] { 
						PublicationTable.PUBLICATION_TABLE + "." + PublicationTable.COLUMN_ID 
					}, 
				selection.toString(),
				args.toArray(new String[args.size()]), null);

		boolean isAlreadyRecorded = true;
		if (cursor.getCount() < 1) {
			isAlreadyRecorded = false;
		}
		cursor.close();
		return isAlreadyRecorded;
	}
	
	public static String orderBy(Context context) {
		return PublicationTable.COLUMN_PUBLICATION_DATE + " desc";
	}
	
	public static String publicationsQueryLimit(Context context) {
		int max = PreferenceManager.getDefaultSharedPreferences(context)
				.getInt("pref_max_publication_item", 100);
		return Integer.valueOf(max).toString();
	}

	public int insertNewPublications(List<ContentValues> publications) {
		return context.getContentResolver().bulkInsert(uri,
				publications.toArray(new ContentValues[publications.size()]));
	}

}
