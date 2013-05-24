package free.solnRss.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FilterQueryProvider;
import android.widget.TextView;
import free.solnRss.R;
import free.solnRss.provider.SyndicationsProvider;
import free.solnRss.repository.SyndicationTable;

/**
 * 
 * @author jftomasi
 * 
 */
public class SyndicationAdapter extends SimpleCursorAdapter implements
		FilterQueryProvider {

	private final int titleID = R.id.syndication_title;
	private final int numberOfClickID = R.id.syndication_number_of_click;
	private Context context;
	private int layout;
	private Typeface tf = null;
	private Drawable pause;

	public SyndicationAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		this.context = context;
		this.layout = layout;
		tf = Typeface.createFromAsset(context.getAssets(), "fonts/MONOF55.TTF");
		pause = context.getResources().getDrawable(R.drawable.ic_pause);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SyndicationItem item = null;
		getCursor().moveToPosition(position);

		String title = getCursor().getString(
				getCursor().getColumnIndex("syn_name"));
		Integer numberOfClick = getCursor().getInt(
				getCursor().getColumnIndex("syn_number_click"));
		Integer isActive = getCursor().getInt(
				getCursor().getColumnIndex("syn_is_active"));

		if (convertView == null) {

			convertView = View.inflate(context, layout, null);
			item = new SyndicationItem();

			item.setTitle((TextView) convertView.findViewById(titleID));
			item.setNumberOfClick((TextView) convertView
					.findViewById(numberOfClickID));

			convertView.setTag(item);

		} else {
			item = (SyndicationItem) convertView.getTag();
		}

		item.getTitle().setText(title);
		if (isActive != 0) {
			item.getTitle().setCompoundDrawablesWithIntrinsicBounds(null, null,
					pause, null);
		} else {
			item.getTitle().setCompoundDrawablesWithIntrinsicBounds(null, null,
					null, null);
		}

		item.getTitle().setTypeface(tf);
		String s = context.getResources().getString(
				R.string.syndication_number_of_click);
		item.getNumberOfClick().setText(
				String.format(s, String.valueOf(numberOfClick)));
		item.getNumberOfClick().setTypeface(tf);

		return convertView;
	}

	@Override
	public Cursor runQuery(CharSequence constraint) {
		Cursor cursor = null;
		Uri uri = SyndicationsProvider.URI;

		String selection = null;
		String[] args = null;
		
		if (!TextUtils.isEmpty(constraint.toString())) {
			selection = SyndicationTable.COLUMN_NAME + " like ? ";
			args = new String[1];
			args[0] = "%" + constraint.toString() + "%";
		}

		cursor = context.getContentResolver().query(uri, 
				SyndicationsProvider.syndicationProjection, selection, args, null);

		return cursor;
	}
}
