package free.solnRss.repository;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import free.solnRss.provider.SolnRssProvider;

public class CategoryRepository {

	public static final String categoryTable = CategoryTable.CATEGORY_TABLE;
	private final Uri uri = Uri.parse(SolnRssProvider.URI + "/category");
	
	private Context context;

	private StringBuilder selection = new StringBuilder();
	private List<String> args = new ArrayList<String>();
	
	public static final String categoryTableJoinToSyndication = 
		categoryTable + " c left join "	+ CategorySyndicationsTable.CATEGORY_SYNDICATION_TABLE + " cs on "
			+ " c." + CategoryTable.COLUMN_ID 
			+ " = cs." + CategorySyndicationsTable.COLUMN_CATEGORY_ID;
			
	String[] projection = {
			"c.*",
			"count("+ CategorySyndicationsTable.COLUMN_CATEGORY_ID
					+ ") as number_of_use " 
			};
	
	public CategoryRepository(Context context) {
		this.context = context;
	}
	
	public CursorLoader loadCategories(String filterText) {
		/*
		select  c.*, count(cs.cas_categorie_id) as number_of_use 
		from d_categorie c left join d_categorie_syndication cs on c._id = cs.cas_categorie_id 
		group by c.cat_name
		 */
		selection.setLength(0);
		args.clear();

		if (!TextUtils.isEmpty(filterText)) {
			selection.append(CategoryTable.COLUMN_NAME + " like ? ");
			args.add("%" + filterText.toString() + "%");
		}

		return new CursorLoader(context, uri, projection, selection.toString(),
				args.toArray(new String[args.size()]), null);
	}
	
	public static String orderBy(Context context) {
		String orderCategoryBy = "c.cat_name asc ";
		if (PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean("pref_sort_categories", true)) {
			orderCategoryBy = "number_of_use desc ";
		}
		return orderCategoryBy;
	}

	public static String groupBy() {
		return "c." + CategoryTable.COLUMN_NAME;
	}
	
	public void renameCategory(Integer selectedCategoryID, String newName) {
		ContentValues values = new ContentValues();
		values.put("cat_name",  newName);
		context.getContentResolver().update(uri, values,"_id = ? ",
				new String[] { selectedCategoryID.toString() });
	}
	
	
	public void removeSyndicationToCategorie(Integer syndicationId,
			Integer categorieId) {

		RepositoryHelper.getInstance(context).getWritableDatabase()
				.delete("d_categorie_syndication",
						" syn_syndication_id = ? and cas_categorie_id = ? ",
						new String[] { syndicationId.toString(),
								categorieId.toString() });
		
		context.getContentResolver().notifyChange(uri, null);
	}

	public void addSyndicationToCategorie(Integer syndicationId,
			Integer categorieId) {
		ContentValues values = new ContentValues();
		values.put("syn_syndication_id", syndicationId);
		values.put("cas_categorie_id", categorieId);
		RepositoryHelper.getInstance(context).getWritableDatabase()
				.insert("d_categorie_syndication", null, values);
		context.getContentResolver().notifyChange(uri, null);
	}

	public void addCategory(String newCatgorieName) {
		ContentValues values = new ContentValues();
		values.put(CategoryTable.COLUMN_NAME, newCatgorieName);
		context.getContentResolver().insert(uri, values);
	}

	public void deleteCategory(Integer categoryId) {
		ContentValues values = new ContentValues();
		values.put(CategoryTable.COLUMN_ID, categoryId);
		context.getContentResolver().delete(uri,
				CategoryTable.COLUMN_ID + " = ? ",
				new String[] { categoryId.toString() });
	}
	
	
	
	/*
	@Deprecated
	public Cursor fetchAllCategorie() {
		return RepositoryHelper	.getInstance(context).getReadableDatabase()
				.rawQuery("select c.*, count(cs.cas_categorie_id) as number_of_use "
								+ "from d_categorie c left join d_categorie_syndication cs on c._id = cs.cas_categorie_id "
								+ "group by c.cat_name order by number_of_use desc",
						null);
	}

	@Deprecated
	public void insert(String label) {
		ContentValues values = new ContentValues();
		values.put("cat_name", label);

		RepositoryHelper.getInstance(context).getWritableDatabase()
				.insert("d_categorie", null, values);
	}

	@Deprecated
	public void delete(Integer id) {

		RepositoryHelper.getInstance(context).getWritableDatabase()
				.delete("d_categorie_syndication", " cas_categorie_id = ? ",
						new String[] { id.toString() });

		RepositoryHelper.getInstance(context).getWritableDatabase()
				.delete("d_categorie", " _id = ? ",
						new String[] { id.toString() });

	}*/
}
