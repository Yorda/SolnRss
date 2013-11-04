package free.solnRss.adapter;

import android.content.Context;
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

	public SyndicationAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		
		super(context, layout, c, from, to, flags);
		this.context = context;
		this.layout = layout;
		
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SyndicationItem item = null;
		getCursor().moveToPosition(position);

		if (convertView == null) {

			convertView = View.inflate(context, layout, null);
			item = new SyndicationItem();

			item.setTitle((TextView) convertView.findViewById(titleID));
			item.setNumberOfClick((TextView) convertView.findViewById(numberOfClickID));

			convertView.setTag(item);

		} else {
			item = (SyndicationItem) convertView.getTag();
		}

		item.getTitle().setText(Html.fromHtml("<u>"+getCursor().getString(1)+"</u>"));

		if (getCursor().getInt(3) != 0) {
			convertView.findViewWithTag("sleepImage")
					.setVisibility(View.VISIBLE);
		} else {
			convertView.findViewWithTag("sleepImage")
					.setVisibility(View.GONE);
		}

		int quantity =  getCursor().getInt(4);
		item.getNumberOfClick().setText(
				context.getResources().getQuantityString(R.plurals.syndication_number_of_click, quantity, quantity));

		
		if (getCursor().getInt(5) != 0) {
			convertView.findViewWithTag("stealthImage")
					.setVisibility(View.GONE);
		} else {
			convertView.findViewWithTag("stealthImage")
					.setVisibility(View.VISIBLE);
		}
		
		Typeface userTypeFace = TypeFaceSingleton.getInstance(context)
				.getUserTypeFace();
		int userFontSize = TypeFaceSingleton.getInstance(context)
				.getUserFontSize();
		
		if (userTypeFace != null) {
			item.getTitle().setTypeface(userTypeFace, Typeface.BOLD);
			item.getNumberOfClick().setTypeface(userTypeFace);
			
			if (getCursor().getInt(3) != 0)
				((TextView) convertView.findViewWithTag("sleepImage"))
						.setTypeface(userTypeFace);
			
			if (getCursor().getInt(5) == 0)
				((TextView) convertView.findViewWithTag("stealthImage"))
						.setTypeface(userTypeFace);
		}
		if (userFontSize != Constants.FONT_SIZE) {
			item.getTitle().setTextSize(userFontSize);
			item.getNumberOfClick().setTextSize(userFontSize);
			if (getCursor().getInt(3) != 0)
				((TextView) convertView.findViewWithTag("sleepImage")).setTextSize(userFontSize);
			if (getCursor().getInt(5) == 0)
				((TextView) convertView.findViewWithTag("stealthImage")).setTextSize(userFontSize);
		}
		
		return convertView;
	}
}
