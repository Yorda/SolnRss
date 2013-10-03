package free.solnRss.adapter;

import android.content.Context;
import android.database.Cursor;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import free.solnRss.R;

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

		getCursor().moveToPosition(position);

		String title = getCursor().getString(1); // pub_title
		String name = getCursor().getString(4);  // syn_name
		Integer isRead = getCursor().getInt(3);  // pub_already_read

		item.getTitle().setText(title);
		item.getName().setText(Html.fromHtml("<b><u>" + name + "</b></u>"));

		item.setIsRead(isRead == null ? 0 : isRead);

		// Typeface tf =  TypeFaceSingleton.getInstance(context).getUserTypeFace();

		if (isRead != 0) {
			item.getAlreadyRead().setVisibility(View.VISIBLE);
		} else {
			item.getAlreadyRead().setVisibility(View.GONE);
		}

		return convertView;
	}
}
