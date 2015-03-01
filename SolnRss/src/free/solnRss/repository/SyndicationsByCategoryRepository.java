package free.solnRss.repository;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import free.solnRss.provider.SolnRssProvider;

public class SyndicationsByCategoryRepository {

	final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",	Locale.FRENCH);
	private Context context;

	private StringBuilder selection = new StringBuilder();
	private List<String> args = new ArrayList<String>();
	
	public SyndicationsByCategoryRepository(Context context) {
		this.context = context;
	}

	public static final String syndicationTable = SyndicationTable.SYNDICATION_TABLE;
	public static final String categorySyndicationTable = CategorySyndicationsTable.CATEGORY_SYNDICATION_TABLE;
	
	public static final String syndicationsByCategoryTable = syndicationTable
			+ " left join " + categorySyndicationTable + " on "
			+ syndicationTable + "." + SyndicationTable.COLUMN_ID + "="
			+ categorySyndicationTable + "."
			+ CategorySyndicationsTable.COLUMN_SYNDICATION_ID + " and "
			+ CategorySyndicationsTable.COLUMN_CATEGORY_ID + "=";
	
	public final static String syndicationByCategoryProjection[] = new String[] {
		syndicationTable + "." + SyndicationTable.COLUMN_ID,
		syndicationTable + "." + SyndicationTable.COLUMN_NAME,
		CategorySyndicationsTable.CATEGORY_SYNDICATION_TABLE + "."
				+ CategorySyndicationsTable.COLUMN_CATEGORY_ID };
	
	public CursorLoader loadSyndicationsByCategory(Integer selectedCategorieID,
			String filterText) {
		selection.setLength(0);
		args.clear();

		Uri uri = Uri.parse(SolnRssProvider.URI + "/syndicationsByCategory/"
				+ selectedCategorieID);
		
		if (!TextUtils.isEmpty(filterText)) {
			selection.append(SyndicationTable.COLUMN_NAME);
			selection.append(" like ? ");
			args.add("%" + filterText + "%");
		}
		
		return new CursorLoader(context, uri, syndicationByCategoryProjection,
				selection.toString(), args.toArray(new String[args.size()]),
				null);
	}
	
	public Cursor reloadSyndicationsByCategory(Integer selectedCategorieID,
			String filterText) {
		
		selection.setLength(0);
		args.clear();

		Uri uri = Uri.parse(SolnRssProvider.URI + "/syndicationsByCategory/"
				+ selectedCategorieID);
		
		if (!TextUtils.isEmpty(filterText)) {
			selection.append(SyndicationTable.COLUMN_NAME);
			selection.append(" like ? ");
			args.add("%" + filterText + "%");
		}

		return context.getContentResolver().query(uri,
				syndicationByCategoryProjection, selection.toString(),
				args.toArray(new String[args.size()]), null);

	}
	
	@Deprecated
	public Cursor syndicationCategorie(Integer categorieId) {

		List<String> arr = new ArrayList<String>();

		StringBuilder sb = new StringBuilder();
		
		sb.append("select ");
		sb.append("s._id, s.syn_name, ");
		sb.append("cs.cas_categorie_id ");
		sb.append("from d_syndication s left join d_categorie_syndication cs on s._id = cs.syn_syndication_id ");
		sb.append("and cs.cas_categorie_id = ? order by s.syn_number_click desc ");

		arr.add(categorieId.toString());
		
		return RepositoryHelper.getInstance(context).getReadableDatabase().rawQuery(sb.toString(),
				arr.toArray(new String[arr.size()]));
	}
}
