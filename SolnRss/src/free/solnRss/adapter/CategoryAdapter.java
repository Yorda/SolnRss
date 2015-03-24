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

	protected Cursor		cursor;
	private final Context	context;
	private final int		layout;

	public CategoryAdapter(final Context context, final int layout, final Cursor c, final String[] from, final int[] to, final int flags) {

		super(context, layout, c, from, to, flags);
		cursor = c;
		this.context = context;
		this.layout = layout;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		CategoryItem item = null;
		final Resources r = context.getResources();
		View v;

		if (convertView == null) {
			v = View.inflate(context, layout, null);
			item = new CategoryItem();

			item.setName((TextView) v.findViewById(R.id.categorie_name));
			item.setNumberOfUse((TextView) v.findViewById(R.id.categorie_number_of_use));
			v.setTag(item);

		} else {
			v = convertView;
			item = (CategoryItem) v.getTag();
		}

		getCursor().moveToPosition(position);

		// getCursor().getColumnIndex("cat_name")
		item.getName().setText(Html.fromHtml("<u>" + getCursor().getString(1) + "</u>"));

		// getCursor().getColumnIndex("number_of_use")
		final Integer numberOfUse = getCursor().getInt(2);
		String use = new String();

		if (numberOfUse == null || numberOfUse == 0) {
			use = r.getString(R.string.categorie_not_use);
		} else if (numberOfUse == 1) {
			use = r.getString(R.string.categorie_use_by_one);
		} else {
			use = r.getString(R.string.categorie_use_by_many, numberOfUse);
		}

		item.getNumberOfUse().setText(use);

		final Typeface userTypeFace = TypeFaceSingleton.getInstance(context).getUserTypeFace();
		final int userFontSize = TypeFaceSingleton.getInstance(context).getUserFontSize();

		if (userTypeFace != null) {
			item.getName().setTypeface(userTypeFace, Typeface.BOLD);
			item.getNumberOfUse().setTypeface(userTypeFace);
		}
		if (userFontSize != Constants.FONT_SIZE) {
			item.getName().setTextSize(userFontSize);
			item.getNumberOfUse().setTextSize(userFontSize);
		}
		return v;
	}
}
