package free.solnRss.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import free.solnRss.R;

/**
 * 
 * @author jftomasi
 * 
 */
public class SyndicationAdapter extends SimpleCursorAdapter {

	private final int titleID = R.id.syndication_title;
	private final int numberOfClickID = R.id.syndication_number_of_click;
	private Context context;
	private int layout;
	private Typeface tf = null;
	//private Drawable pause;
	//private Drawable stealth;

	public SyndicationAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		this.context = context;
		this.layout = layout;
		tf = Typeface.createFromAsset(context.getAssets(), "fonts/MONOF55.TTF");
		//pause = context.getResources().getDrawable(R.drawable.ic_sleep);
		//stealth = context.getResources().getDrawable(R.drawable.ic_stealth);
		
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SyndicationItem item = null;
		getCursor().moveToPosition(position);

		// String title = getCursor().getString(1); //1 syn_name
		// Integer numberOfClick = getCursor().getInt(4); //4 syn_number_click
		// Integer isActive = getCursor().getInt(3); // 3 syn_is_active
		// Integer isDisplayOnMainTL = getCursor().getInt(5); //5
		// syn_display_on_timeline

		if (convertView == null) {

			convertView = View.inflate(context, layout, null);
			item = new SyndicationItem();

			item.setTitle((TextView) convertView.findViewById(titleID));
			item.setNumberOfClick((TextView) convertView.findViewById(numberOfClickID));

			convertView.setTag(item);

		} else {
			item = (SyndicationItem) convertView.getTag();
		}

		item.getTitle().setText(getCursor().getString(1));

		if (getCursor().getInt(3) != 0) {
			convertView.findViewWithTag("sleepImage").setVisibility(
					View.VISIBLE);
		} else {
			convertView.findViewWithTag("sleepImage").setVisibility(
					View.GONE);
		}

		item.getTitle().setTypeface(tf);
		
		String s = context.getResources().getString(R.string.syndication_number_of_click);

		item.getNumberOfClick().setText(
				String.format(s, String.valueOf(getCursor().getInt(4))));

		if (getCursor().getInt(5) != 0) {
			convertView.findViewWithTag("stealthImage").setVisibility(
					View.GONE);
		} else {
			convertView.findViewWithTag("stealthImage").setVisibility(
					View.VISIBLE);
		}
		
		item.getNumberOfClick().setTypeface(tf);
		return convertView;
	}
}
