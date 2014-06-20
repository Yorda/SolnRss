package free.solnRss.repository;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
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
			SyndicationTable.SYNDICATION_TABLE + "." + SyndicationTable.COLUMN_NUMBER_CLICK,
			publicationTable + "." + PublicationTable.COLUMN_FAVORITE
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
		
		 context.getContentResolver().applyBatch(SolnRssProvider.AUTHORITY, operations);
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

		return new CursorLoader(context, uri, projection, selection.toString(),
				args.toArray(new String[args.size()]), null);
		
	}
	

	public Loader<Cursor> loadFavoritePublications(String filterText) {
		
		selection.setLength(0);
		selection.append(" 1 = 1 ") ;
		args.clear();
		
		selection.append(" and ");
		selection.append(PublicationTable.COLUMN_FAVORITE);
		selection.append(" = 1 ");
		
		if (!TextUtils.isEmpty(filterText)) {

			selection.append(" and ");
			selection.append(PublicationTable.COLUMN_TITLE);
			selection.append(" like ? ");
			args.add("%" + filterText + "%");
		}
		
		return new CursorLoader(context, uri, projection, selection.toString(),
				args.toArray(new String[args.size()]), null);
	}
	
	public void markOnePublicationAsFavorite(Integer publicationId, Integer isFavorite) {
		ContentValues values = new ContentValues();
		String value = isFavorite.compareTo(Integer.valueOf(1)) == 0 ? "0": "1";
		values.put(PublicationTable.COLUMN_FAVORITE, value);
		String where = PublicationTable.COLUMN_ID + " = ? ";
		context.getContentResolver().update(uri, values, where, new String[] { String.valueOf(publicationId) });
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
		
		Uri publicationContentUri = PublicationContentRepository.uri.buildUpon()
				.appendQueryParameter("tableKey", syndicationId.toString()).build();
		
		operations.add(ContentProviderOperation.newDelete(publicationContentUri).build());
		
		context.getContentResolver().applyBatch(SolnRssProvider.AUTHORITY, operations);
	}
	
	public void deleteAllPublication() throws Exception {
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

		operations.add(ContentProviderOperation.newDelete(uri).build());

		//--
		// Delete all publication content in all table
		// Get all syndication id
		Cursor cursor = context.getContentResolver().query(SyndicationRepository.uri,
				new String[] { SyndicationTable.COLUMN_ID }, null, null, null);
		
		cursor.moveToFirst();
		// Add an operation for delete publication content
		if (cursor.getCount() > 0) {
			do {
				
				Uri publicationContentUri = PublicationContentRepository.uri.buildUpon()
						.appendQueryParameter("tableKey", String.valueOf(cursor.getInt(0))).build();
				
				operations.add(ContentProviderOperation.newDelete(publicationContentUri).build());
					
			} while (cursor.moveToNext());
		}
		//--
		
		context.getContentResolver().applyBatch(SolnRssProvider.AUTHORITY, operations);
	}

	public static String orderBy(Context context) {
		return PublicationTable.COLUMN_PUBLICATION_DATE + " desc";
	}
	
	public void removeTooOLdPublications() throws Exception {
		
		final int max = maxPublicationToKeep();
		if (max == -1) {
			return;
		}
		
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		
		//--
		// Get all syndication id
		Cursor cursor = context.getContentResolver().query(SyndicationRepository.uri,
				new String[] { SyndicationTable.COLUMN_ID }, null, null, null);
		
		cursor.moveToFirst();
		Integer syndicationId = null;
		Uri publicationContentUri = null;
		
		// Add an operation for delete publication content
		if (cursor.getCount() > 0) {
			
			do {
				
				syndicationId = cursor.getInt(0);
				
				selection.setLength(0);
				selection.append(" _id in " + 
						" (select p._id FROM d_publication p where  " + 
						" p.syn_syndication_id = ? " + 
						" and (p.pub_favorite is null or p.pub_favorite = 0) " + 
						" order by p._id desc LIMIT -1 OFFSET " + max + " )");
	
				operations.add(ContentProviderOperation.newDelete(uri)
						.withSelection(selection.toString(), new String[] { syndicationId.toString() })
						.build());
				
				// Delete publication content
				publicationContentUri = PublicationContentRepository.uri.buildUpon()
						.appendQueryParameter("tableKey", String.valueOf(syndicationId)).build();
				
				selection.setLength(0);
				selection.append(" _id in (select pc._id FROM d_publication_content_"
						+ syndicationId.toString()
						+ " pc left join "
						+ " d_publication p on pc.pub_publication_id = p._id "
						+ " where (p.pub_favorite is null or p.pub_favorite = 0) order by pc._id desc LIMIT -1 OFFSET "
						+ max + " )");
				
				operations.add(ContentProviderOperation.newDelete(publicationContentUri)
						.withSelection(selection.toString(), null).build());
				
			} while (cursor.moveToNext());
		}
		//--
		context.getContentResolver().applyBatch(SolnRssProvider.AUTHORITY, operations);	
	}
	
	protected final String tag = PublicationRepository.class.getName();
	
	private int maxPublicationToKeep() {
		return Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(context)
				.getString("pref_maxPublicationsBySyndicationToKeep", "100"));
	}

	public void deletePublication(Integer publicationId, Integer syndicationId) throws Exception {
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		
		operations.add(ContentProviderOperation.newDelete(uri)
				.withSelection("_id =  ? ",	new String[] { publicationId.toString() })
				.build());
		
		final Uri publicationContentUri = PublicationContentRepository.uri.buildUpon()
				.appendQueryParameter("tableKey", String.valueOf(syndicationId)).build();
		
		operations.add(ContentProviderOperation.newDelete(publicationContentUri)
				.withSelection(" pub_publication_id = ? ", new String[] { publicationId.toString() } )
				.build());
		
		context.getContentResolver().applyBatch(SolnRssProvider.AUTHORITY, operations);	
	}
}
