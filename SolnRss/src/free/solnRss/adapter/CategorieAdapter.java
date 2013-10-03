package free.solnRss.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import free.solnRss.R;

public class CategorieAdapter extends SimpleCursorAdapter {

	protected Cursor cursor;
	private Context context;
	private int layout;

	public CategorieAdapter(final Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {

		super(context, layout, c, from, to, flags);
		this.cursor = c;
		this.context = context;
		this.layout = layout;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CategorieItem categorieItem = null;

		if (convertView == null) {
			convertView = View.inflate(context, layout, null);
			categorieItem = new CategorieItem();

			categorieItem.setName((TextView) convertView
					.findViewById(R.id.categorie_name));
			categorieItem.setNumberOfUse((TextView) convertView
					.findViewById(R.id.categorie_number_of_use));
			convertView.setTag(categorieItem);

		} else {
			categorieItem = (CategorieItem) convertView.getTag();
		}

		getCursor().moveToPosition(position);


		categorieItem.getName().setText(Html.fromHtml("<u>" + getCursor().getString(
				getCursor().getColumnIndex("cat_name")) + "</u>"));
		Integer numberOfUse = getCursor().getInt(
				getCursor().getColumnIndex("number_of_use"));

		String use = new String();

		Resources r = context.getResources();

		if (numberOfUse == null || numberOfUse == 0) {
			use = r.getString(R.string.categorie_not_use);
		} else if (numberOfUse == 1) {
			use = r.getString(R.string.categorie_use_by_one);
		} else {
			use = r.getString(R.string.categorie_use_by_many, numberOfUse);
		}

		categorieItem.getNumberOfUse().setText(use);
	
		return convertView;
	}
}
