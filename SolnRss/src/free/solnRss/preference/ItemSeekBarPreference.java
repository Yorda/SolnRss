package free.solnRss.preference;

import free.solnRss.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class ItemSeekBarPreference extends Preference implements
		OnSeekBarChangeListener {

	private int MINIMUM = 25, MAXIMUM = 500, DEFAULT_PROGRESS = 100;

	private String elements = new String();
	
	private TextView monitorBox;

	public ItemSeekBarPreference(Context context) {
		super(context);
	}

	public ItemSeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ItemSeekBarPreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected View onCreateView(ViewGroup parent) {

		LinearLayout layout = new LinearLayout(getContext());

		LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		params1.gravity = Gravity.LEFT;
		params1.weight = 1.0f;

		LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);

		params2.gravity = Gravity.LEFT;

		LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		params3.gravity = Gravity.CENTER;

		layout.setPadding(15, 5, 10, 5);
		layout.setOrientation(LinearLayout.VERTICAL);

		TextView view = new TextView(getContext());
		view.setText(getTitle());
		view.setTextSize(16);
		view.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL);
		view.setGravity(Gravity.LEFT);
		view.setLayoutParams(params1);

		SeekBar bar = new SeekBar(getContext());
		bar.setMax(MAXIMUM);

		bar.setProgress(getPersistedInt(100) - MINIMUM);
		bar.setLayoutParams(params2);
		bar.setOnSeekBarChangeListener(this);

		elements =  getContext().getResources().getString(R.string.item_brev);
		
		monitorBox = new TextView(getContext());
		monitorBox.setTextSize(12);
		monitorBox.setTypeface(Typeface.MONOSPACE, Typeface.ITALIC);
		monitorBox.setLayoutParams(params3);
		monitorBox.setPadding(2, 5, 0, 0);
		monitorBox.setText(getPersistedInt(100) + " " + elements);

		layout.addView(view);
		layout.addView(bar);
		layout.addView(this.monitorBox);
		layout.setId(android.R.id.widget_frame);

		return layout;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {

		monitorBox.setText(String.valueOf(progress + MINIMUM) + " " + elements);
	}

	@Override
	protected Object onGetDefaultValue(TypedArray ta, int index) {
		return ta.getInt(index, DEFAULT_PROGRESS);
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		
	}

	private void updatePreference(int newValue) {
		SharedPreferences.Editor editor = getEditor();
		editor.putInt(getKey(), newValue);
		editor.commit();
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		updatePreference(seekBar.getProgress() + MINIMUM);
		notifyChanged();
	}
}
