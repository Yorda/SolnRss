package free.solnRss.repository;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.v4.content.CursorLoader;
import android.text.TextUtils;
import free.solnRss.provider.PublicationsProvider;

public class PublicationRepository {

	private Context context;
	
	public PublicationRepository(Context context) {
		this.context = context;
	}

	private StringBuilder selection = new StringBuilder();
	private List<String> args = new ArrayList<String>();

	
	/**
	 * Clean the publications list in table for a syndication
	 * @param id
	 */
	public void clean(Integer id) {

		 RepositoryHelper.getInstance(context).getWritableDatabase()
		.delete("d_publication", " syn_syndication_id = ? ", new String[] { id.toString() });

	}

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
		
		return new CursorLoader(context, PublicationsProvider.URI,
				PublicationsProvider.projection, selection.toString(),
				args.toArray(new String[args.size()]), null);
		
	}
}
