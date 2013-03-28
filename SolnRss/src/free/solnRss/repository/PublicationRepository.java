package free.solnRss.repository;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import free.solnRss.model.Publication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class PublicationRepository extends Repository {
	final private DateFormat sdf = 
			new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",	Locale.FRENCH);

	public PublicationRepository(Context context) {
		super.context = context;
	}

	/**
	 * Set one publication as read
	 * @param id
	 */
	public void markClickedPublicationRead(Integer id) {
		open(context);
		
		// Set publication as read
		ContentValues values = new ContentValues();
		values.put("pub_already_read", 1);
		sqLiteDatabase.update("d_publication", values, " _id = ? ",	new String[] { id.toString() });

		// Add one to syndication's number of click
		String[] args = new String[1];
		args[0] = id.toString();

		sqLiteDatabase.execSQL(
" update d_syndication set syn_number_click = (syn_number_click +1) where _id = (select syn_syndication_id from d_publication where _id = ?)",
						args);
		
		close();
	}

	/**
	 * Set all publications in database as read
	 */
	public void markAllPublicationsAsRead() {
		open(context);
		ContentValues values = new ContentValues();
		values.put("pub_already_read", 1);
		sqLiteDatabase.update("d_publication", values, null, null);
		close();
	}
	
	/**
	 * Set all publication for one syndication as read
	 * @param syndicationId
	 */
	public void markSyndicationPublicationsAsRead(Integer syndicationId) {
		open(context);
		ContentValues values = new ContentValues();
		values.put("pub_already_read", 1);
		sqLiteDatabase.update("d_publication", values,
				" syn_syndication_id = ? ",
				new String[] { syndicationId.toString() });
		close();
	}
	
	/**
	 * 
	 * @param syndicationId
	 */
	public void markCategoryPublicationsAsRead(Integer categoryId) {
		open(context);

		String[] args = new String[1];
		args[0] = categoryId.toString();

		sqLiteDatabase.execSQL(
				" update d_publication set "
						+ " pub_already_read = 1 "
						+ " where syn_syndication_id in "
						+ "(select syn_syndication_id from d_categorie_syndication where cas_categorie_id = ?)",
						args);
		close();
	}
	
	/**
	 * Clean the publications list in table for a syndication
	 * @param id
	 */
	public void clean(Integer id) {
		open(context);
		sqLiteDatabase.delete(
			"d_publication", " syn_syndication_id = ? ", new String[] { id.toString() });
		close();
	}

	public Cursor fetchFilteredPublication(Integer syndicationId,
			String filter, boolean displayUnread) {
		open(context);
		
		List<String> arr = new ArrayList<String>();

		StringBuilder sb = new StringBuilder();
		sb.append("select ");
		sb.append("a._id, ");
		sb.append("a.pub_title, ");
		sb.append("a.pub_link, ");
		sb.append("a.pub_already_read, ");
		sb.append("a.syn_syndication_id, ");
		sb.append("a.pub_publication, ");
		sb.append("s.syn_name ");		
		sb.append("from d_syndication s left join d_publication a on s._id = a.syn_syndication_id ");
		sb.append(" where 1 = 1 ");
		
		if (syndicationId != null) {
			sb.append(" and a.syn_syndication_id = ? ");
			arr.add(syndicationId.toString());
		}

		if (filter != null && filter.trim().length() > 0) {
			sb.append("and a.pub_title like ? ");
			arr.add("%" + filter + "%");
		}

		if (!displayUnread) {
			sb.append(" and a.pub_already_read = 0 ");
		}
		
		sb.append("order by a.pub_publication_date desc");

		return sqLiteDatabase.rawQuery(sb.toString(),
				arr.toArray(new String[arr.size()]));
	}
	
	public Cursor fetchPublicationByCategorie(Integer categorieId,
			String filter, boolean displayUnread) {
		open(context);

		List<String> arr = new ArrayList<String>();

		StringBuilder sb = new StringBuilder();
		sb.append("select ");
		sb.append("a._id, ");
		sb.append("a.pub_title, ");
		sb.append("a.pub_link, ");
		sb.append("a.pub_already_read, ");
		sb.append("a.syn_syndication_id, ");
		sb.append("a.pub_publication, ");
		sb.append("s.syn_name ");
		sb.append("from d_syndication s left outer join d_publication a on s._id = a.syn_syndication_id ");
		sb.append(" where ");

		sb.append(" a.syn_syndication_id in (select syn_syndication_id from d_categorie_syndication where cas_categorie_id = ?) ");
		arr.add(categorieId.toString());

		if (filter != null && filter.trim().length() > 0) {
			sb.append("and a.pub_title like ? ");
			arr.add("%" + filter + "%");
		}

		if (!displayUnread) {
			sb.append(" and a.pub_already_read = 0 ");
		}

		sb.append("order by a.pub_publication_date desc");
		
		return sqLiteDatabase.rawQuery(sb.toString(),
				arr.toArray(new String[arr.size()]));
	}
	
	/**
	 * Get the publications 
	 * @return
	 */
	/*public Cursor fetchAllPublications(Integer syndicationId){
		open(context);
		
		String[] args = new String[] {};
		
		StringBuilder sb = new StringBuilder();
		sb.append("select ");
		sb.append("a._id, ");
		sb.append("a.pub_title, ");
		sb.append("a.pub_link, "); 
		sb.append("a.pub_already_read, ");
		sb.append("a.syn_syndication_id, "); 
		sb.append("s.syn_name ");
		sb.append("from ");
		sb.append("d_publication a, ");
		sb.append("d_syndication s ");
		sb.append("where ");
		sb.append("a.syn_syndication_id = s._id ");
		
		if (syndicationId != null) {
			sb.append("and a.syn_syndication_id = ? ");
			args = new String[] {syndicationId.toString() };
		}
		
		sb.append("order by a.pub_publication_date desc");
		
		return sqLiteDatabase.rawQuery(sb.toString(), args);
	}*/

	/**
	 * 
	 * @param publications 
	 * An publications list found in rss or atom xml file
	 * @param 
	 * id The id of syndication
	 * @param numberOfnewPublications 
	 * For know how many new publications founded
	 */
	public int refresh(List<Publication> publications, Integer id, Integer numberOfnewPublications) {	
		
		open(context);
		Cursor cursor;
		ContentValues values = new ContentValues();
		int num = 0;
		for (Publication publication : publications) {
			
			// If publications not exist add a new one
			String[] columns = { "_id", "pub_title" };
			cursor = sqLiteDatabase.query("d_publication", columns,
					"pub_title = ? and pub_link = ?",
					new String[] { publication.getTitle(), publication.getUrl() },
					null, null, null, null);
			
			if (cursor.getCount() < 1) {
				
				values.put("syn_syndication_id", id);
				values.put("pub_link", publication.getUrl());
				values.put("pub_title", publication.getTitle());
				values.put("pub_publication", publication.getDescription());
				values.put("pub_already_read", 0);
				values.put("pub_publication_date",sdf.format(publication.getPublicationDate() == null ? new Date() : publication.getPublicationDate()));
				
				sqLiteDatabase.insert("d_publication", null, values);
				values.clear();
				num++;
			}
		}
		close();
		return num;
	}
}
