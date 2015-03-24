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
import android.widget.Toast;
import free.solnRss.R;


@Deprecated
public class SeekBarPreference extends Preference implements OnSeekBarChangeListener {

	public static int	maximum		= 100;
	public static int	interval	= 5;
	private int			oldValue	= 50;

	private TextView	monitorBox;

	public SeekBarPreference(final Context context) {
		super(context);
	}

	public SeekBarPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public SeekBarPreference(final Context context, final AttributeSet attrs, final int defStyle) {
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
		bar.setMax(maximum);
		bar.setProgress(oldValue);
		bar.setLayoutParams(params2);
		bar.setOnSeekBarChangeListener(this);

		monitorBox = new TextView(getContext());
		monitorBox.setTextSize(12);
		monitorBox.setTypeface(Typeface.MONOSPACE, Typeface.ITALIC);
		monitorBox.setLayoutParams(params3);
		monitorBox.setPadding(2, 5, 0, 0);
		monitorBox.setText(bar.getProgress() + " min");

		layout.addView(view);
		layout.addView(bar);
		layout.addView(monitorBox);
		layout.setId(android.R.id.widget_frame);

		return layout;
	}

	@Override
	public void onProgressChanged(final SeekBar seekBar, int progress, final boolean fromUser) {

		progress = Math.round((float) progress / interval) * interval;

		if (!callChangeListener(progress)) {
			seekBar.setProgress(oldValue);
			return;
		}

		seekBar.setProgress(progress);
		oldValue = progress;
		monitorBox.setText(progress + " min");
	}

	@Override
	protected Object onGetDefaultValue(final TypedArray ta, final int index) {
		return validateValue(ta.getInt(index, 50));
	}

	@Override
	protected void onSetInitialValue(final boolean restoreValue, final Object defaultValue) {
		final int temp = restoreValue ? getPersistedInt(50) : (Integer) defaultValue;
		if (!restoreValue) {
			persistInt(temp);
		}
		oldValue = temp;
	}

	private int validateValue(int value) {
		if (value > maximum) {
			value = maximum;
		} else if (value < 0) {
			value = 0;
		} else if (value % interval != 0) {
			value = Math.round((float) value / interval) * interval;
		}
		return value;
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

		updatePreference(oldValue);

		if (oldValue == 0) {
			// Warn user, the new publication search is deactivate
			Toast.makeText(getContext(), getContext().getResources().getString(R.string.stop_search), Toast.LENGTH_LONG).show();
		}

		notifyChanged();
	}
}
