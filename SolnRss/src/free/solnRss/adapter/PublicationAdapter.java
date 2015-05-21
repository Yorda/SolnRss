package free.solnRss.adapter;


import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import free.solnRss.R;
import free.solnRss.repository.PublicationRepository;
import free.solnRss.singleton.TypeFaceSingleton;
import free.solnRss.utility.Constants;


public class PublicationAdapter extends SimpleCursorAdapter {

	protected Cursor				cursor;
	private Context					context;
	private int						layout;
	private PublicationRepository	publicationRepository;

	private int						emptyStarImageId;
	private int						fullStarImageId;

	// private int threeDotsMenu;

	public PublicationAdapter(final Context context, final int layout, final Cursor c, final String[] from, final int[] to, final int flags) {

		super(context, layout, c, from, to, flags);
		cursor = c;
		this.context = context;
		this.layout = layout;
		publicationRepository = new PublicationRepository(context);

		final String pn = context.getPackageName();
		final Resources res = context.getResources();

		emptyStarImageId = res.getIdentifier("ic_favorite_empty", "drawable", pn);
		fullStarImageId = res.getIdentifier("ic_favorite_full", "drawable", pn);
		// this.threeDotsMenu = res.getIdentifier("three_dots_menu_grey" , "drawable", pn);
	}

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
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
			item.setAlreadyRead((ImageView) convertView.findViewById(R.id.check_already_read_pict));

			item.setFavorite((ImageButton) convertView.findViewById(R.id.isFavoriteButton));

			convertView.setTag(item);

		} else {
			item = (PublicationItem) convertView.getTag();
		}

		final Integer _id = getCursor().getInt(0); // _id
		final String title = getCursor().getString(1); // pub_title
		final String name = getCursor().getString(3); // syn_name
		final Integer isRead = getCursor().getInt(2); // pub_already_read
		final Integer isFavorite = getCursor().getInt(6); // pud_favorite

		item.getTitle().setText(title);
		item.getName().setText(Html.fromHtml("<u>" + name + "</u>"));

		final Typeface userTypeFace = TypeFaceSingleton.getInstance(context).getUserTypeFace();

		final int userFontSize = TypeFaceSingleton.getInstance(context).getUserFontSize();

		if (userTypeFace != null) {
			item.getName().setTypeface(userTypeFace, Typeface.BOLD);
			item.getTitle().setTypeface(userTypeFace, Typeface.NORMAL);
		}
		if (userFontSize != Constants.FONT_SIZE) {
			item.getName().setTextSize(userFontSize);
			item.getTitle().setTextSize(userFontSize);
		}

		item.setIsRead(isRead == null ? 0 : isRead);

		if (isRead != null && isRead != 0) {
			item.getAlreadyRead().setVisibility(View.VISIBLE);
		} else {
			item.getAlreadyRead().setVisibility(View.GONE);
		}

		if (isFavorite.compareTo(Integer.valueOf(1)) == 0) {
			//item.getFavorite().setImageResource(fullStarImageId);
			item.getFavorite().setBackgroundResource(fullStarImageId);
		} else {
			// item.getFavorite().setImageResource(emptyStarImageId);
			item.getFavorite().setBackgroundResource(emptyStarImageId);
			// item.getFavorite().setBackgroundResource(threeDotsMenu);
		}

		item.getFavorite().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				try {
					publicationRepository.markOnePublicationAsFavorite(_id, isFavorite);
					final String value = isFavorite.compareTo(Integer.valueOf(1)) == 0 ? "Remove to favorite" : "Add to favorite";
					Toast.makeText(context, value, Toast.LENGTH_LONG).show();
				} catch (final Exception e) {
					Toast.makeText(context, "Error unable to add to favorite", Toast.LENGTH_LONG).show();
				}
			}
		});

		return convertView;
	}

	/*
	 * String timeAgo = new String(); try { timeAgo = toTime(sdf.parse(getCursor().getString(7)), new Date()); } catch (Exception e) {
	 * e.printStackTrace(); }
	 */
	/*
	 * private final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRENCH); private final long MILLIS_PER_SEC = 1000; private
	 * final long MILLIS_PER_MIN = 60 * MILLIS_PER_SEC; private final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MIN; private final long MILLIS_PER_DAY =
	 * 24 * MILLIS_PER_HOUR; private final long MILLIS_PER_WEEK = 7 * MILLIS_PER_DAY; private final long MILLIS_PER_MONTH = 4 * MILLIS_PER_WEEK;
	 * private final long MILLIS_PER_YEAR = 12 * MILLIS_PER_MONTH; private String toTime(Date publicationDate, Date currentDate) { long diff =
	 * currentDate.getTime() - publicationDate.getTime(); if (diff / MILLIS_PER_SEC < 1) { return "One second"; } else if (diff / MILLIS_PER_MIN < 1)
	 * { return diff / MILLIS_PER_SEC + " sec"; } else if (diff / MILLIS_PER_HOUR < 1) { return diff / MILLIS_PER_MIN + " min"; } else if (diff /
	 * MILLIS_PER_DAY < 1) { return diff / MILLIS_PER_HOUR + " hour"; } else if (diff / MILLIS_PER_WEEK < 1) { return diff / MILLIS_PER_DAY + " day";
	 * } else if (diff / MILLIS_PER_MONTH < 1) { return diff / MILLIS_PER_WEEK + " week"; } else if (diff / MILLIS_PER_YEAR < 1) { return diff /
	 * MILLIS_PER_MONTH + " month"; } else { return diff / MILLIS_PER_YEAR + " year"; } }
	 */
}
