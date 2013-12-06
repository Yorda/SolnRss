package free.solnRss.repository;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentProviderOperation;
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
	private Uri uri = Uri.parse(SolnRssProvider.URI + "/publication");
	
	private Context context;
	private StringBuilder selection = new StringBuilder();
	private List<String> args = new ArrayList<String>();

	public final String projection[] = new String[] {
			publicationTable + "." + PublicationTable.COLUMN_ID,
			publicationTable + "." + PublicationTable.COLUMN_TITLE,
			publicationTable + "." + PublicationTable.COLUMN_ALREADY_READ, 
			SyndicationTable.SYNDICATION_TABLE + "." + SyndicationTable.COLUMN_NAME,
			publicationTable + "." + PublicationTable.COLUMN_SYNDICATION_ID,
			SyndicationTable.SYNDICATION_TABLE + "." + SyndicationTable.COLUMN_NUMBER_CLICK
		};
	
	
	public static final String publicationTableJoinToSyndication = 
		publicationTable + " LEFT JOIN "
			+ SyndicationRepository.syndicationTable + " ON " + SyndicationRepository.syndicationTable + "."
			+ SyndicationTable.COLUMN_ID + " = " + publicationTable + "."+ PublicationTable.COLUMN_SYNDICATION_ID;
	
	public PublicationRepository(Context context) {
		this.context = context;
	}
	
	public void markOnePublicationAsReadByUser(Integer publicationId,
			Integer syndicationId, Integer numberOfClick) throws Exception {

		ContentValues values = new ContentValues();
		values.put(PublicationTable.COLUMN_ALREADY_READ, "1");
		
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		operations.add(ContentProviderOperation.newUpdate(uri).withValues(values)
				.withSelection(PublicationTable.COLUMN_ID + " = ? " , new String[] { publicationId.toString() })
				.withYieldAllowed(true).build());
		
		values = new ContentValues();
		values.put("syn_number_click", numberOfClick + 1);
		
		operations.add(ContentProviderOperation.newUpdate(Uri.parse(SolnRssProvider.URI + "/syndication"))
				.withValues(values)
				.withSelection(SyndicationTable.COLUMN_ID + " = ? " , new String[] { syndicationId.toString() })
				.withYieldAllowed(true).build());
		
		 context.getContentResolver().applyBatch(
				SolnRssProvider.AUTHORITY, operations);
		 
		 // context.getContentResolver().notifyChange(uri, null);
		 // context.getContentResolver().notifyChange(Uri.parse(SolnRssProvider.URI + "/syndication"), null);
	}
	
	public Cursor loadMorePublications(String filterText,
			Integer selectedSyndicationID, Integer selectedCategoryID, String lastUpdateDate,
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
		if (selectedSyndicationID == null && selectedCategoryID == null && lastUpdateDate == null) {
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

		else if (lastUpdateDate != null) {
			selection.append(" and ");
			selection.append( PublicationTable.COLUMN_PUBLICATION_DATE);
			selection.append(" >= ? ");
			
			args.add(lastUpdateDate);
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

		uri = uri.buildUpon().appendQueryParameter("page", "2").build();
		
		return context.getContentResolver().query(uri, projection,
				selection.toString(), args.toArray(new String[args.size()]),null);
	}
	
	/**
	 * Clean the publications list in table for a syndication
	 * @param id
	 */
	public CursorLoader loadPublications(String filterText,
			Integer selectedSyndicationID, Integer selectedCategoryID, String lastUpdateDate,
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
		if (selectedSyndicationID == null && selectedCategoryID == null && lastUpdateDate == null) {
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

		else if (lastUpdateDate != null) {
			selection.append(" and ");
			selection.append( PublicationTable.COLUMN_PUBLICATION_DATE);
			selection.append(" >= ? ");
			
			args.add(lastUpdateDate);
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

		//uri = uri.buildUpon().appendQueryParameter("limit", "5").build();
		
		return new CursorLoader(context, uri, projection, selection.toString(),
				args.toArray(new String[args.size()]), null);
		
	}
	
	public void markPublicationAsRead(int publicationId) {
		ContentValues values = new ContentValues();
		values.put(PublicationTable.COLUMN_ALREADY_READ, "1");
		String where = PublicationTable.COLUMN_ID + " = ? ";
		context.getContentResolver().update(uri, values, where, new String[] { String.valueOf(publicationId) });
	}
	
	public void markCategoryPublicationsAsRead(Integer categoryId){
		ContentValues values = new ContentValues();
		values.put(PublicationTable.COLUMN_ALREADY_READ, "1");
		String selection = " syn_syndication_id in (select syn_syndication_id from d_categorie_syndication where cas_categorie_id = ?) ";
		context.getContentResolver().update(uri, values, selection, new String[] {categoryId.toString()});
	}
	
	public void marklastPublicationFoundAsRead(String dateNewPublicationsFound) {
		ContentValues values = new ContentValues();
		values.put(PublicationTable.COLUMN_ALREADY_READ, "1");
		String where = PublicationTable.COLUMN_PUBLICATION_DATE + " >= ? ";
		context.getContentResolver().update(uri, values, where, new String[] { String.valueOf(dateNewPublicationsFound) });
	}
	
	public void markSyndicationPublicationsAsRead(Integer syndicationId) {
		ContentValues values = new ContentValues();
		values.put(PublicationTable.COLUMN_ALREADY_READ, "1");
		String selection = " syn_syndication_id  = ?  ";
		context.getContentResolver().update(uri, values, selection, new String[] { syndicationId.toString() });
	}
	
	public void markAllPublicationsAsRead() {
		ContentValues values = new ContentValues();
		values.put(PublicationTable.COLUMN_ALREADY_READ, "1");
		context.getContentResolver().update(uri, values, null, null);
	}
	
	public void deletePublications(Integer syndicationId) throws Exception {
		
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

		operations.add(ContentProviderOperation.newDelete(uri)
				.withSelection("syn_syndication_id = ? ", new String[] { syndicationId.toString() }).build());
		
		operations.add(ContentProviderOperation.newDelete(PublicationContentRepository.uri)
				.withSelection("pub_publication_id in (select _id from d_publication where syn_syndication_id = ? )", 
						new String[] { syndicationId.toString() }).build());
		
		context.getContentResolver().applyBatch(SolnRssProvider.AUTHORITY, operations);
		 
		//context.getContentResolver().delete(uri, " syn_syndication_id = ? ",
		//		new String[] { syndicationId.toString() });
	}

	public void deleteAllPublication() throws Exception {
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

		operations.add(ContentProviderOperation.newDelete(uri).build());

		operations.add(ContentProviderOperation.newDelete(PublicationContentRepository.uri).build());

		context.getContentResolver().applyBatch(SolnRssProvider.AUTHORITY, operations);
	}

	public static String orderBy(Context context) {
		return PublicationTable.COLUMN_PUBLICATION_DATE + " desc";
	}
	
	public static String publicationsQueryLimit(String parameterPage, Context context) {
		
		int max = PreferenceManager.getDefaultSharedPreferences(context)
				.getInt("pref_max_publication_item", 100);
				
		String maxItemInPage = Integer.valueOf(max).toString() ;
		
		Integer page = 1;
		if (parameterPage != null) {
			page = Integer.valueOf(parameterPage);
		}
		
		if (page != 1) {
			int offset = max * page;
			maxItemInPage = maxItemInPage.concat(" offset ").concat(Integer.valueOf(offset).toString());
		}
		
		
		return Integer.valueOf(max).toString();
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
