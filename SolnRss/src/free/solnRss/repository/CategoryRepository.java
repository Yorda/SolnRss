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

	public static final String	categoryTable					= CategoryTable.CATEGORY_TABLE;
	private final Uri			uri								= Uri.parse(SolnRssProvider.URI + "/category");

	private Context				context;

	private StringBuilder		selection						= new StringBuilder();
	private List<String>		args							= new ArrayList<String>();

	public static final String	categoryTableJoinToSyndication	= categoryTable + " c left join "
			+ CategorySyndicationsTable.CATEGORY_SYNDICATION_TABLE + " cs on " + " c."
			+ CategoryTable.COLUMN_ID + " = cs."
			+ CategorySyndicationsTable.COLUMN_CATEGORY_ID;

	String[]					projection						= { "c.*",
			"count(" + CategorySyndicationsTable.COLUMN_CATEGORY_ID + ") as number_of_use " };

	public CategoryRepository(final Context context) {
		this.context = context;
	}

	public CursorLoader loadCategories(final String filterText) {
		selection.setLength(0);
		args.clear();

		if (!TextUtils.isEmpty(filterText)) {
			selection.append(CategoryTable.COLUMN_NAME + " like ? ");
			args.add("%" + filterText.toString() + "%");
		}

		return new CursorLoader(context, uri, projection, selection.toString(), args.toArray(new String[args.size()]), null);
	}

	public static String orderBy(final Context context) {
		String orderCategoryBy = "c.cat_name asc ";
		if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_sort_categories", true)) {
			orderCategoryBy = "number_of_use desc ";
		}
		return orderCategoryBy;
	}

	public static String groupBy() {
		return "c." + CategoryTable.COLUMN_NAME;
	}

	public void renameCategory(final Integer selectedCategoryID, final String newName) {
		final ContentValues values = new ContentValues();
		values.put("cat_name", newName);
		context.getContentResolver().update(uri, values, "_id = ? ", new String[] { selectedCategoryID.toString() });
	}

	public void removeSyndicationToCategorie(final Integer syndicationId, final Integer categorieId) {

		RepositoryHelper
		.getInstance(context)
		.getWritableDatabase()
		.delete("d_categorie_syndication", " syn_syndication_id = ? and cas_categorie_id = ? ",
				new String[] { syndicationId.toString(), categorieId.toString() });

		context.getContentResolver().notifyChange(uri, null);
	}

	public void addSyndicationToCategorie(final Integer syndicationId, final Integer categorieId) {
		final ContentValues values = new ContentValues();
		values.put("syn_syndication_id", syndicationId);
		values.put("cas_categorie_id", categorieId);
		RepositoryHelper.getInstance(context).getWritableDatabase().insert("d_categorie_syndication", null, values);
		context.getContentResolver().notifyChange(uri, null);
	}

	public void addCategory(final String newCatgorieName) {
		final ContentValues values = new ContentValues();
		values.put(CategoryTable.COLUMN_NAME, newCatgorieName);
		context.getContentResolver().insert(uri, values);
	}

	public void deleteCategory(final Integer categoryId) {
		final ContentValues values = new ContentValues();
		values.put(CategoryTable.COLUMN_ID, categoryId);
		context.getContentResolver().delete(uri, CategoryTable.COLUMN_ID + " = ? ", new String[] { categoryId.toString() });
	}

}
