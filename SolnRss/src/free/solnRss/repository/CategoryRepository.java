package free.solnRss.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class CategoryRepository extends Repository {

	public CategoryRepository(Context context) {
		super.context = context;
	}
	
	public Cursor fetchAllCategorie() {
		open(context);
		return sqLiteDatabase.rawQuery("select c.*, count(cs.cas_categorie_id) as number_of_use from d_categorie c left join d_categorie_syndication cs on c._id = cs.cas_categorie_id group by c.cat_name order by number_of_use desc", null);
		
		/*String[] columns = { "_id", "cat_name", };
		return sqLiteDatabase.query("d_categorie", columns, null, null, null,
				null, " cat_name desc ", null);*/
	}

	public void insert(String label) {
		open(context);
		ContentValues values = new ContentValues();
		values.put("cat_name", label);
		sqLiteDatabase.insert("d_categorie", null, values);
		close();
	}

	public void delete(Integer id) {
		open(context);
		sqLiteDatabase.delete("d_categorie_syndication",
				" cas_categorie_id = ? ", new String[] { id.toString() });

		sqLiteDatabase.delete("d_categorie", " _id = ? ",
				new String[] { id.toString() });
		close();
	}

	public void removeSyndicationToCategorie(Integer syndicationId, Integer categorieId) {
		open(context);
		sqLiteDatabase.delete("d_categorie_syndication",
				" syn_syndication_id = ? and cas_categorie_id = ? ", 
				new String[] {
						syndicationId.toString(), 
						categorieId.toString() 
					});
		close();
	}

	public void addSyndicationToCategorie(Integer syndicationId, Integer categorieId) {
		open(context);
		ContentValues values = new ContentValues();
		values.put("syn_syndication_id", syndicationId);
		values.put("cas_categorie_id", categorieId);
		sqLiteDatabase.insert("d_categorie_syndication", null, values);
		close();
	}
}
