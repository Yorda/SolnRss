package free.solnRss.preference;


import java.util.Date;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import free.solnRss.repository.PublicationRepository;


public class DeleteAllPublicationsPreferences extends DialogPreference {

	PublicationRepository	publicationRepository;

	public DeleteAllPublicationsPreferences(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		publicationRepository = new PublicationRepository(context);
	}

	@Override
	public void onClick(final DialogInterface dialog, final int which) {

		super.onClick(dialog, which);

		switch (which) {
			case DialogInterface.BUTTON_POSITIVE:

				try {
					publicationRepository.deleteAllPublication();
				} catch (final Exception e) {
					e.printStackTrace();
				}

				final SharedPreferences.Editor editor = getEditor();
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
	protected void onDialogClosed(final boolean positiveResult) {
		super.onDialogClosed(positiveResult);
	}
}
