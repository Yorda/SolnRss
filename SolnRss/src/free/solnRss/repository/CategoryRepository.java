package free.solnRss.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class CategoryRepository {

	private Context context;

	public CategoryRepository(Context context) {
		this.context = context;
	}

	public Cursor fetchAllCategorie() {
		return RepositoryHelper	.getInstance(context).getReadableDatabase()
				.rawQuery("select c.*, count(cs.cas_categorie_id) as number_of_use "
								+ "from d_categorie c left join d_categorie_syndication cs on c._id = cs.cas_categorie_id "
								+ "group by c.cat_name order by number_of_use desc",
						null);
	}

	public void insert(String label) {
		ContentValues values = new ContentValues();
		values.put("cat_name", label);

		RepositoryHelper.getInstance(context).getWritableDatabase()
				.insert("d_categorie", null, values);
	}

	public void delete(Integer id) {

		RepositoryHelper.getInstance(context).getWritableDatabase()
				.delete("d_categorie_syndication", " cas_categorie_id = ? ",
						new String[] { id.toString() });

		RepositoryHelper.getInstance(context).getWritableDatabase()
				.delete("d_categorie", " _id = ? ",
						new String[] { id.toString() });

	}

	public void removeSyndicationToCategorie(Integer syndicationId,
			Integer categorieId) {

		RepositoryHelper.getInstance(context).getWritableDatabase()
				.delete("d_categorie_syndication",
						" syn_syndication_id = ? and cas_categorie_id = ? ",
						new String[] { syndicationId.toString(),
								categorieId.toString() });

	}

	public void addSyndicationToCategorie(Integer syndicationId,
			Integer categorieId) {

		ContentValues values = new ContentValues();
		values.put("syn_syndication_id", syndicationId);
		values.put("cas_categorie_id", categorieId);
		RepositoryHelper.getInstance(context).getWritableDatabase()
				.insert("d_categorie_syndication", null, values);

	}
}
