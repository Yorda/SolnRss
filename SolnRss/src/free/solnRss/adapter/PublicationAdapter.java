package free.solnRss.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import free.solnRss.R;

public class PublicationAdapter extends SimpleCursorAdapter //implements FilterQueryProvider 
{
	
	protected Cursor cursor;
	private Context context;
	private int layout;
	
	private Typeface tf = null;
	public PublicationAdapter(final Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {

		super(context, layout, c, from, to, flags);
		this.cursor = c;
		this.context = context;
		this.layout = layout;
		tf = Typeface.createFromAsset(context.getAssets(), "fonts/MONOF55.TTF");
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		PublicationItem publicationItem = null;

		if (convertView == null) {
			convertView = View.inflate(context, layout, null);
			publicationItem = new PublicationItem();
			// Title of syndication
			publicationItem.setName((TextView) convertView.findViewById(R.id.name));
			// Title of publication
			publicationItem.setTitle((TextView) convertView.findViewById(R.id.title));
			convertView.setTag(publicationItem);
			
		} else {
			publicationItem = (PublicationItem) convertView.getTag();
		}
		
		getCursor().moveToPosition(position);
	
		String  title   = getCursor().getString(1); // pub_title
		String  name    = getCursor().getString(4); // syn_name
		Integer isRead  = getCursor().getInt   (3); // pub_already_read
		
		publicationItem.getTitle().setText(title);
		publicationItem.getName ().setText(name);
		
		publicationItem.setIsRead(isRead == null ? 0 : isRead);
		 
		if (isRead == 0 && mustDisplayUnreadInBold()) {
			publicationItem.getName() .setTypeface(tf, Typeface.BOLD);
			publicationItem.getTitle().setTypeface(tf, Typeface.BOLD);
		} else {
			publicationItem.getName() .setTypeface(tf, Typeface.NORMAL);
			publicationItem.getTitle().setTypeface(tf, Typeface.NORMAL);
		}
		
		return convertView;
	}

	public boolean mustDisplayUnreadInBold() {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean("pref_unread_font_weight", true);
	}

	public boolean mustDisplayUnread() {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean("pref_display_unread", true);
	}
}
