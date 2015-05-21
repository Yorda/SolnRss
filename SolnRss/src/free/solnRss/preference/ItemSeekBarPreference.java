package free.solnRss.preference;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import free.solnRss.R;


public class ItemSeekBarPreference extends Preference implements OnSeekBarChangeListener {

	private final int	MINIMUM		= 25, MAXIMUM = 500, DEFAULT_PROGRESS = 100;
	private String		elements	= new String();

	private TextView	monitorBox;

	public ItemSeekBarPreference(final Context context) {
		super(context);
	}

	public ItemSeekBarPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public ItemSeekBarPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected View onCreateView(final ViewGroup parent) {

		final LinearLayout layout = new LinearLayout(getContext());

		final LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		params1.gravity = Gravity.LEFT;
		params1.weight = 1.0f;

		final LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);

		params2.gravity = Gravity.LEFT;

		final LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		params3.gravity = Gravity.CENTER;

		layout.setPadding(15, 5, 10, 5);
		layout.setOrientation(LinearLayout.VERTICAL);

		final TextView view = new TextView(getContext());
		view.setText(getTitle());
		view.setTextSize(16);
		view.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL);
		view.setGravity(Gravity.LEFT);
		view.setLayoutParams(params1);

		final SeekBar bar = new SeekBar(getContext());
		bar.setMax(MAXIMUM);

		bar.setProgress(getPersistedInt(100) - MINIMUM);
		bar.setLayoutParams(params2);
		bar.setOnSeekBarChangeListener(this);

		elements = getContext().getResources().getString(R.string.item_brev);

		monitorBox = new TextView(getContext());
		monitorBox.setTextSize(12);
		monitorBox.setTypeface(Typeface.MONOSPACE, Typeface.ITALIC);
		monitorBox.setLayoutParams(params3);
		monitorBox.setPadding(2, 5, 0, 0);
		monitorBox.setText(getPersistedInt(100) + " " + elements);

		layout.addView(view);
		layout.addView(bar);
		layout.addView(monitorBox);
		layout.setId(android.R.id.widget_frame);

		return layout;
	}

	@Override
	public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {

		monitorBox.setText(String.valueOf(progress + MINIMUM) + " " + elements);
	}

	@Override
	protected Object onGetDefaultValue(final TypedArray ta, final int index) {
		return ta.getInt(index, DEFAULT_PROGRESS);
	}

	@Override
	protected void onSetInitialValue(final boolean restoreValue, final Object defaultValue) {

	}

	private void updatePreference(final int newValue) {
		final SharedPreferences.Editor editor = getEditor();
		editor.putInt(getKey(), newValue);
		editor.commit();
	}

	@Override
	public void onStartTrackingTouch(final SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(final SeekBar seekBar) {
		updatePreference(seekBar.getProgress() + MINIMUM);
		notifyChanged();
	}
}
