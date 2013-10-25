package free.solnRss.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import free.solnRss.R;
import free.solnRss.repository.SyndicationsByCategoryRepository;

public class SyndicationsCategorieAdapter extends SimpleCursorAdapter implements
		FilterQueryProvider {

	protected Cursor cursor;
	private Context context;
	private int layout;
	private Integer selectedCategoryId;
	private SyndicationsByCategoryRepository syndicationsByCategoryRepository;

	public SyndicationsCategorieAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		this.cursor = c;
		this.context = context;
		this.layout = layout;
		setFilterQueryProvider(this);
		//tf = Typeface.createFromAsset(context.getAssets(), "fonts/MONOF55.TTF");
		syndicationsByCategoryRepository = new SyndicationsByCategoryRepository(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SyndicationsCategorieItem item = null;

		if (convertView == null) {
			convertView = View.inflate(context, layout, null);
			item = new SyndicationsCategorieItem();

			item.setName((TextView) convertView
					.findViewById(R.id.syndication_to_categorie_name));
			item.setCheck((CheckBox) convertView
					.findViewById(R.id.syndication_to_categorie_check));
			convertView.setTag(item);

		} else {
			item = (SyndicationsCategorieItem) convertView.getTag();
		}

		getCursor().moveToPosition(position);

		final Integer syndicationId = getCursor().getInt(
				getCursor().getColumnIndex("_id"));

		final String name = getCursor().getString(
				getCursor().getColumnIndex("syn_name"));

		final Integer categorieId = getCursor().getInt(
				getCursor().getColumnIndex("cas_categorie_id"));

		item.getName().setText(name);
		item.getCheck().setTag(syndicationId);

		if (categorieId == null || categorieId == 0) {
			item.getCheck().setChecked(false);
		} else {
			item.getCheck().setChecked(true);
		}

		return convertView;
	}

	@Override
	public Cursor runQuery(CharSequence constraint) {
		return syndicationsByCategoryRepository.reloadSyndicationsByCategory(
				selectedCategoryId, constraint.toString());
	}

	public void setSelectedCategoryId(Integer selectedCategoryId) {
		this.selectedCategoryId = selectedCategoryId;
	}
}
