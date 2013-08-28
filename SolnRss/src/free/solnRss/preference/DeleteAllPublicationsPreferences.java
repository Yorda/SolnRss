package free.solnRss.preference;

import java.util.Date;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import free.solnRss.R;
import free.solnRss.provider.PublicationsProvider;

public class DeleteAllPublicationsPreferences extends DialogPreference {

	public DeleteAllPublicationsPreferences(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {

		super.onClick(dialog, which);

		switch (which) {
		case DialogInterface.BUTTON_POSITIVE:
			
			// 
			setDialogMessage(getContext().getResources().getString(R.string.please_wait));
			
			
			getContext().getContentResolver().delete(PublicationsProvider.URI,null, null);
			SharedPreferences.Editor editor = getEditor();
			editor.putLong(getKey(), new Date().getTime());
			editor.commit();
			
			break;

		case DialogInterface.BUTTON_NEGATIVE:
			break;

		default:
			break;
		}

	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
	}
}
