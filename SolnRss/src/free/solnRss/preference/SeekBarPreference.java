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
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import free.solnRss.R;

public class SeekBarPreference extends Preference implements
		OnSeekBarChangeListener {

	public static int maximum = 100;
	public static int interval = 5;

	private int oldValue = 50;
	private TextView monitorBox;

	public SeekBarPreference(Context context) {
		super(context);
	}

	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SeekBarPreference(Context context, AttributeSet attrs, int defStyle) {
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
		view.setTextSize(18);
		view.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
		view.setGravity(Gravity.LEFT);
		view.setLayoutParams(params1);

		SeekBar bar = new SeekBar(getContext());
		bar.setMax(maximum);
		bar.setProgress((int) this.oldValue);
		bar.setLayoutParams(params2);
		bar.setOnSeekBarChangeListener(this);

		this.monitorBox = new TextView(getContext());
		this.monitorBox.setTextSize(12);
		this.monitorBox.setTypeface(Typeface.MONOSPACE, Typeface.ITALIC);
		this.monitorBox.setLayoutParams(params3);
		this.monitorBox.setPadding(2, 5, 0, 0);
		this.monitorBox.setText(bar.getProgress() + " min");

		layout.addView(view);
		layout.addView(bar);
		layout.addView(this.monitorBox);
		layout.setId(android.R.id.widget_frame);

		return layout;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {

		progress = Math.round(((float) progress) / interval) * interval;

		if (!callChangeListener(progress)) {
			seekBar.setProgress((int) this.oldValue);
			return;
		}

		seekBar.setProgress(progress);
		this.oldValue = progress;
		this.monitorBox.setText(progress + " min");
	}

	@Override
	protected Object onGetDefaultValue(TypedArray ta, int index) {
		return validateValue((int) ta.getInt(index, 50));
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		int temp = restoreValue ? getPersistedInt(50) : (Integer) defaultValue;
		if (!restoreValue)
			persistInt(temp);
		this.oldValue = temp;
	}

	private int validateValue(int value) {
		if (value > maximum) {
			value = maximum;
		} else if (value < 0) {
			value = 0;
		} else if (value % interval != 0) {
			value = Math.round(((float) value) / interval) * interval;
		}
		return value;
	}

	private void updatePreference(int newValue) {
		SharedPreferences.Editor editor = getEditor();
		editor.putInt(getKey(), newValue);
		editor.commit();
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		//Log.e(SeekBarPreference.class.getName() , "------> START TRACKING !!!! ");
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		//Log.e(SeekBarPreference.class.getName() , "------> STOP TRACKING !!!! ");
		updatePreference(this.oldValue);
		
		if (this.oldValue == 0) {
			// Warn user, the new publication search is deactivate
			Toast.makeText(getContext(),
					getContext().getResources().getString(R.string.stop_search),
					Toast.LENGTH_LONG).show();
		}
		
		notifyChanged();
	}
}
