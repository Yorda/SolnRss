package free.solnRss.dialog;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.content.res.Resources;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import free.solnRss.R;


public class OneEditTextDialogBox {

	private CharSequence	title;
	private CharSequence	hint;
	private Context			context;
	private OnClickListener	listener;

	public OneEditTextDialogBox(final Context contex, final String title, final String hint, final OnClickListener listener) {
		context = contex;
		this.title = title;
		this.hint = hint;
		this.listener = listener;
	}

	@SuppressLint("InflateParams")
	public void displayDialogBox() {

		final Resources r = context.getResources();

		final LayoutInflater li = LayoutInflater.from(context);
		final View layout = li.inflate(R.layout.one_edit_text_dialog_box, null);

		final EditText editText = (EditText) layout.findViewById(R.id.one_edit_text_dialog);
		editText.setHint(hint);
		editText.setPadding(10, 25, 15, 10);

		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(layout).setTitle(title).setNegativeButton(r.getString(android.R.string.cancel), null)
		.setPositiveButton(r.getString(android.R.string.ok), listener);

		final AlertDialog dialog = builder.create();
		dialog.setOnShowListener(new OnShowListener() {
			@Override
			public void onShow(final DialogInterface dialog) {
				final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
			}
		});
		dialog.show();

		dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);

		editText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(final Editable s) {
				boolean enabled = false;
				if (!TextUtils.isEmpty(editText.getText().toString())) {
					enabled = true;
				}
				dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(enabled);
			}

			@Override
			public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
			}

			@Override
			public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
			}
		});
	}
}
