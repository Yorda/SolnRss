package free.solnRss.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import free.solnRss.R;
import free.solnRss.singleton.TypeFaceSingleton;
import free.solnRss.utility.Constants;

public class PublicationAdapter extends SimpleCursorAdapter {

	protected Cursor cursor;
	private Context context;
	private int layout;

	public PublicationAdapter(final Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {

		super(context, layout, c, from, to, flags);
		this.cursor = c;
		this.context = context;
		this.layout = layout;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		getCursor().moveToPosition(position);
		PublicationItem item = null;
		
		if (convertView == null) {
			convertView = View.inflate(context, layout, null);
			item = new PublicationItem();

			// Title of syndication
			item.setName((TextView) convertView.findViewById(R.id.name));

			// Title of publication
			item.setTitle((TextView) convertView.findViewById(R.id.title));

			// PNG for already read
			item.setAlreadyRead((ImageView) convertView
					.findViewById(R.id.check_already_read_pict));

			convertView.setTag(item);

		} else {
			item = (PublicationItem) convertView.getTag();
		}
		
		String title = getCursor().getString(1); // pub_title
		String name = getCursor().getString(3); // syn_name
		Integer isRead = getCursor().getInt(2); // pub_already_read
		
		item.getTitle().setText(title);
		item.getName().setText(Html.fromHtml("<u>" + name + "</u>"));

		Typeface userTypeFace = TypeFaceSingleton.getInstance(context)
				.getUserTypeFace();
		
		int userFontSize = TypeFaceSingleton.getInstance(context)
				.getUserFontSize();
		
		if (userTypeFace != null) {
			item.getName().setTypeface(userTypeFace, Typeface.BOLD);
			item.getTitle().setTypeface(userTypeFace);
		}
		if (userFontSize != Constants.FONT_SIZE) {
			item.getName().setTextSize(userFontSize);
			item.getTitle().setTextSize(userFontSize);
		}

		item.setIsRead(isRead == null ? 0 : isRead);

		if (isRead != 0) {
			item.getAlreadyRead().setVisibility(View.VISIBLE);
		} else {
			item.getAlreadyRead().setVisibility(View.GONE);
		}

		return convertView;
	}
	
	
	/*
	 * 
	 * 
	 String timeAgo = "";
		try {
			timeAgo = toTime(sdf.parse(getCursor().getString(7)), new Date());
		} catch (Exception e) {
		}

	private final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.FRENCH);
	
	private final long MILLIS_PER_SEC = 1000;
	private final long MILLIS_PER_MIN = 60 * MILLIS_PER_SEC;
	private final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MIN;
	private final long MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR;
	private final long MILLIS_PER_WEEK = 7 * MILLIS_PER_DAY;
	private final long MILLIS_PER_MONTH = 4 * MILLIS_PER_WEEK;
	private final long MILLIS_PER_YEAR = 12 * MILLIS_PER_MONTH;
	
	private String toTime(Date publicationDate, Date currentDate){

		long diff = currentDate.getTime() - publicationDate.getTime();
		
		if (diff / MILLIS_PER_SEC < 1) {
			return "One second";
		} else if (diff / MILLIS_PER_MIN < 1) {
			return diff / MILLIS_PER_SEC + " sec";
		} else if (diff / MILLIS_PER_HOUR < 1) {
			return diff / MILLIS_PER_MIN + " min";
		} else if (diff / MILLIS_PER_DAY < 1) {
			return diff / MILLIS_PER_HOUR + " hour";
		} else if (diff / MILLIS_PER_WEEK < 1) {
			return diff / MILLIS_PER_DAY + " day";
		} else if (diff / MILLIS_PER_MONTH < 1) {
			return diff / MILLIS_PER_WEEK + " week";
		} else if (diff / MILLIS_PER_YEAR < 1) {
			return diff / MILLIS_PER_MONTH + " month";
		} else {
			 return diff / MILLIS_PER_YEAR + " year";
		}
	}*/
}
