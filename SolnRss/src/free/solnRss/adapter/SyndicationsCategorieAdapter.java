package free.solnRss.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import free.solnRss.R;

public class SyndicationsCategorieAdapter extends SimpleCursorAdapter {

	private Context context;
	private int layout;
	private Typeface tf = null;

	public SyndicationsCategorieAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		this.context = context;
		this.layout = layout;
		tf = Typeface.createFromAsset(context.getAssets(), "fonts/MONOF55.TTF");
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SyndicationsCategorieItem item = null;

		if (convertView == null) {
			convertView = View.inflate(context, layout, null);
			item = new SyndicationsCategorieItem();

			item.setName((TextView) convertView
					.findViewById(R.id.syndication_to_categorie_name));
			item.setCheck((CheckBox) convertView
					.findViewById(R.id.syndication_to_categorie_check));
			convertView.setTag(item);

		} else {
			item = (SyndicationsCategorieItem) convertView.getTag();
		}

		getCursor().moveToPosition(position);

		final Integer syndicationId = getCursor().getInt(
				getCursor().getColumnIndex("_id"));

		final String name = getCursor().getString(
				getCursor().getColumnIndex("syn_name"));

		final Integer categorieId = getCursor().getInt(
				getCursor().getColumnIndex("cas_categorie_id"));

		item.getName().setText(name);
		item.getName().setTypeface(tf);
		item.getCheck().setTag(syndicationId);

		// Log.e(SyndicationsCategorieAdapter.this.getClass().getName(), name +
		// " Categorie id is " + categorieId);
		if (categorieId == null || categorieId == 0) {
			item.getCheck().setChecked(false);
		} else {
			item.getCheck().setChecked(true);
		}

		return convertView;
	}
}
