package free.solnRss.adapter;


import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import free.solnRss.R;
import free.solnRss.repository.SyndicationsByCategoryRepository;
import free.solnRss.singleton.TypeFaceSingleton;
import free.solnRss.utility.Constants;


public class SyndicationsCategorieAdapter extends SimpleCursorAdapter implements FilterQueryProvider {

	protected Cursor							cursor;
	private Context								context;
	private int									layout;
	private Integer								selectedCategoryId;
	private SyndicationsByCategoryRepository	syndicationsByCategoryRepository;

	public SyndicationsCategorieAdapter(final Context context, final int layout, final Cursor c, final String[] from, final int[] to, final int flags) {
		super(context, layout, c, from, to, flags);
		cursor = c;
		this.context = context;
		this.layout = layout;
		setFilterQueryProvider(this);
		syndicationsByCategoryRepository = new SyndicationsByCategoryRepository(context);
	}

	@Override
	public View getView(final int position, View convertView, final ViewGroup parent) {
		SyndicationsCategorieItem item = null;

		if (convertView == null) {
			convertView = View.inflate(context, layout, null);
			item = new SyndicationsCategorieItem();

			item.setName((TextView) convertView.findViewById(R.id.syndication_to_categorie_name));
			item.setCheck((CheckBox) convertView.findViewById(R.id.syndication_to_categorie_check));
			convertView.setTag(item);

		} else {
			item = (SyndicationsCategorieItem) convertView.getTag();
		}

		getCursor().moveToPosition(position);

		// getCursor().getColumnIndex("_id")
		final Integer syndicationId = getCursor().getInt(0);

		// getCursor().getColumnIndex("syn_name")
		final String name = getCursor().getString(1);

		// getCursor().getColumnIndex("cas_categorie_id")
		final Integer categorieId = getCursor().getInt(2);

		item.getName().setText(name);
		item.getCheck().setTag(syndicationId);

		if (categorieId == null || categorieId == 0) {
			item.getCheck().setChecked(false);
		} else {
			item.getCheck().setChecked(true);
		}

		final Typeface userTypeFace = TypeFaceSingleton.getInstance(context).getUserTypeFace();

		final int userFontSize = TypeFaceSingleton.getInstance(context).getUserFontSize();

		if (userTypeFace != null) {
			item.getName().setTypeface(userTypeFace);
		}
		if (userFontSize != Constants.FONT_SIZE) {
			item.getName().setTextSize(userFontSize);
		}

		return convertView;
	}

	@Override
	public Cursor runQuery(final CharSequence constraint) {
		return syndicationsByCategoryRepository.reloadSyndicationsByCategory(selectedCategoryId, constraint.toString());
	}

	public void setSelectedCategoryId(final Integer selectedCategoryId) {
		this.selectedCategoryId = selectedCategoryId;
	}
}
