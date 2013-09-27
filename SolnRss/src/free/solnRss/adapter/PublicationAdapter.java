package free.solnRss.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import free.solnRss.R;

public class PublicationAdapter extends SimpleCursorAdapter
{
	
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
		PublicationItem publicationItem = null;

		if (convertView == null) {
			convertView = View.inflate(context, layout, null);
			publicationItem = new PublicationItem();
			
			// Title of syndication
			publicationItem.setName((TextView) convertView.findViewById(R.id.name));
			
			//convertView.findViewById(R.id.name).setVisibility(View.GONE);
			
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
		publicationItem.getName ().setText(Html.fromHtml("<b><u>"+name+"</b></u>"));
		
		publicationItem.setIsRead(isRead == null ? 0 : isRead);
		
		 //Typeface tf = null; //TypeFaceSingleton.getInstance(context).getUserTypeFace();
		 
		if (isRead == 0 && mustDisplayUnreadInBold()) {
           
			//publicationItem.getName().setTypeface(tf , Typeface.BOLD);
			//publicationItem.getTitle().setTypeface(tf , Typeface.BOLD);

		} else {
			publicationItem.getName().setTypeface(null , Typeface.NORMAL);
			publicationItem.getTitle().setTypeface(null, Typeface.NORMAL);
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
