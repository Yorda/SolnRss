package free.solnRss.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import free.solnRss.R;

public class CategorieAdapter extends SimpleCursorAdapter {

	private Typeface tf = null;
	protected Cursor cursor;
	private Context context;
	private int layout;

	public CategorieAdapter(final Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {

		super(context, layout, c, from, to, flags);
		this.cursor = c;
		this.context = context;
		this.layout = layout;
		tf = Typeface.createFromAsset(context.getAssets(), "fonts/MONOF55.TTF");
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CategorieItem categorieItem = null;

		if (convertView == null) {
			convertView = View.inflate(context, layout, null);
			categorieItem = new CategorieItem();

			categorieItem.setName((TextView) convertView.findViewById(R.id.categorie_name));
			convertView.setTag(categorieItem);

		} else {
			categorieItem = (CategorieItem) convertView.getTag();
		}

		getCursor().moveToPosition(position);

		String name = getCursor().getString(
				getCursor().getColumnIndex("cat_name"));

		categorieItem.getName().setText(name);
		categorieItem.getName().setTypeface(tf, Typeface.NORMAL);

		return convertView;
	}

}
