package free.solnRss.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FilterQueryProvider;
import android.widget.TextView;
import free.solnRss.R;
import free.solnRss.provider.PublicationsProvider;
import free.solnRss.repository.PublicationTable;

public class PublicationAdapter extends SimpleCursorAdapter implements
		FilterQueryProvider {
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
		setFilterQueryProvider(this);
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
	
		String  title   = getCursor().getString(1); //getCursor().getColumnIndex("pub_title")); //
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
	
	private Integer selectedSyndicationID, selectedCategoryId;
	@Override
	public Cursor runQuery(CharSequence constraint) {		
		
		Cursor cursor = null;
		/*
		PublicationRepository repository = new PublicationRepository(context);
		
		if (selectedCategoryId != null) {
			cursor = repository.fetchPublicationByCategorie(selectedCategoryId, 
					constraint.toString(), mustDisplayUnread());
			
		} else if (selectedSyndicationID != null) {
			cursor = repository.fetchFilteredPublication(selectedSyndicationID,
					constraint.toString(), mustDisplayUnread());
		} else {
			cursor = repository.fetchFilteredPublication(null, 
					constraint.toString(), mustDisplayUnread());
		}*/
		
		Uri uri = PublicationsProvider.URI;
		if (selectedSyndicationID != null) {
			uri = Uri.parse(PublicationsProvider.URI + "/" + selectedSyndicationID);
		} else if (selectedCategoryId != null) {
			uri = Uri.parse(PublicationsProvider.URI + "/categoryId/" + selectedCategoryId);
		}

		String selection = null;
		String[] args = null;
		
		if (!TextUtils.isEmpty(constraint.toString())) {
			selection = PublicationTable.COLUMN_TITLE + " like ? ";
			args = new String[1];
			args[0] = "%" + constraint.toString() + "%";
		}

		cursor = context.getContentResolver().query(uri, 
				PublicationsProvider.projection, selection, args, null);

		return cursor;
	}
	
	public void setSelectedSyndicationId(Integer selectedSyndicationID) {
		this.selectedCategoryId = null;
		this.selectedSyndicationID = selectedSyndicationID;
	}

	public void setSelectedCategoryId(Integer selectedCategoryId) {
		this.selectedSyndicationID = null;
		this.selectedCategoryId = selectedCategoryId;
	}
	
}
