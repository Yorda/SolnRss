package free.solnRss.singleton;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;


public class TypeFaceSingleton {

	private static TypeFaceSingleton	typeFaceSingleton;
	private static SharedPreferences	preferences;

	private TypeFaceSingleton() {

	}

	public static TypeFaceSingleton getInstance(final Context context) {
		if (typeFaceSingleton == null) {
			typeFaceSingleton = new TypeFaceSingleton();
			typeFaceSingleton.initTypeFace(context);
			preferences = PreferenceManager.getDefaultSharedPreferences(context);

		}
		return typeFaceSingleton;
	}

	Typeface	monofur;
	Typeface	anonymousPro;
	Typeface	inconsolata;
	Typeface	monospace;

	public Typeface getUserTypeFace() {
		final int index = Integer.valueOf(preferences.getString("pref_user_font_face", "0"));
		switch (index) {
			case 0:
				return null;
			case 1:
				return anonymousPro;
			case 2:
				return inconsolata;
			case 3:
				return monofur;
			case 4:
				return monospace;
		}
		return null;
	}

	public int getUserFontSize() {
		return preferences.getInt("pref_user_font_size", 16);
	}

	private void initTypeFace(final Context context) {
		//monofur = Typeface.createFromAsset(context.getAssets(),
		//		"fonts/monofur/MONOF55.TTF");
		monofur = Typeface.createFromAsset(context.getAssets(), "fonts/monaco/monaco.ttf");
		anonymousPro = Typeface.createFromAsset(context.getAssets(), "fonts/anonymous_pro/Anonymous Pro.ttf");
		inconsolata = Typeface.createFromAsset(context.getAssets(), "fonts/inconsolata/Inconsolata.otf");
		monospace = Typeface.MONOSPACE;
	}
}
