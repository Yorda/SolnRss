package free.solnRss.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import free.solnRss.R;
import free.solnRss.singleton.TypeFaceSingleton;
import free.solnRss.utility.Constants;

public class CategoryAdapter extends SimpleCursorAdapter {

	protected Cursor cursor;
	private Context context;
	private int layout;

	public CategoryAdapter(final Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {

		super(context, layout, c, from, to, flags);
		this.cursor = c;
		this.context = context;
		this.layout = layout;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CategoryItem item = null;
		Resources r = context.getResources();

		if (convertView == null) {
			convertView = View.inflate(context, layout, null);
			item = new CategoryItem();

			item.setName((TextView) convertView
					.findViewById(R.id.categorie_name));
			item.setNumberOfUse((TextView) convertView
					.findViewById(R.id.categorie_number_of_use));
			convertView.setTag(item);

		} else {
			item = (CategoryItem) convertView.getTag();
		}

		getCursor().moveToPosition(position);
		
		// getCursor().getColumnIndex("cat_name")
		item.getName().setText(Html.fromHtml("<u>" + getCursor().getString(1) + "</u>"));

		// getCursor().getColumnIndex("number_of_use")
		Integer numberOfUse = getCursor().getInt(2);
		String use = new String();
		
		if (numberOfUse == null || numberOfUse == 0) {
			use = r.getString(R.string.categorie_not_use);
		} else if (numberOfUse == 1) {
			use = r.getString(R.string.categorie_use_by_one);
		} else {
			use = r.getString(R.string.categorie_use_by_many, numberOfUse);
		}

		item.getNumberOfUse().setText(use);

		Typeface userTypeFace = TypeFaceSingleton.getInstance(context)
				.getUserTypeFace();
		int userFontSize = TypeFaceSingleton.getInstance(context)
				.getUserFontSize();
		if (userTypeFace != null) {
			item.getName().setTypeface(userTypeFace, Typeface.BOLD);
			item.getNumberOfUse().setTypeface(userTypeFace);
		}
		if (userFontSize != Constants.FONT_SIZE) {
			item.getName().setTextSize(userFontSize);
			item.getNumberOfUse().setTextSize(userFontSize);
		}
		return convertView;
	}
}
