package free.solnRss.singleton;

import android.content.Context;
import android.graphics.Typeface;

public class TypeFaceSingleton {

	private static TypeFaceSingleton typeFaceSingleton;

	private TypeFaceSingleton() {

	}

	public static TypeFaceSingleton getInstance(Context context) {
		if (typeFaceSingleton == null) {
			typeFaceSingleton = new TypeFaceSingleton();
			typeFaceSingleton.initTypeFace(context);
		}
		return typeFaceSingleton;
	}

	Typeface monofur;

	public Typeface getUserTypeFace() {
		return monofur;
	}

	private void initTypeFace(Context context) {
		monofur = Typeface.createFromAsset(context.getAssets(),
				"fonts/MONOF55.TTF");
	}
}
