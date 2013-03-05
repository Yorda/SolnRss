package free.solnRss.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import free.solnRss.R;
import free.solnRss.activity.SolnRss;

public class AddItemDialog extends DialogFragment implements OnEditorActionListener {
	public static enum Item {
		Site("Site"), Categorie("Categorie");
		private Item(String s) {
		};
	};
	
	private final int layoutID = R.layout.dialog_add_item;
	private Item item;
	private EditText editText;
	private TextView label;

	public interface NewAddItemDialogListener {
		void onFinishEditDialog(CharSequence seq, Item item);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup vg,
			Bundle savedInstanceState) {

		item = Item.valueOf(getArguments().getString("item"));

		View view = inflater.inflate(layoutID, vg);

		getDialog().getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);

		editText = (EditText) view.findViewById(R.id.text_add_new_item);
		label = (TextView) view.findViewById(R.id.label_add_new_item);

		editText.requestFocus();
		editText.setOnEditorActionListener(this);

		switch (item) {
		case Site:
			getDialog().setTitle(getResources().getString(R.string.add_site));
			label.setText(getResources().getString(R.string.add_site_explain));
			break;

		case Categorie:
			getDialog().setTitle(getResources().getString(R.string.add_categorie));
			label.setText(getResources().getString(R.string.add_categorie_explain));
			break;

		default:
			break;
		}

		return view;
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		
		if (EditorInfo.IME_ACTION_DONE == actionId) {
			Log.e(AddItemDialog.this.getClass().getName(), " DONE !");
			SolnRss activity = (SolnRss) getActivity();
			activity.onFinishEditDialog(editText.getText(), item);
			this.dismiss();
			return true;
		}
		return false;
	}
}
