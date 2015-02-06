package free.solnRss.activity;

import free.solnRss.fragment.ReaderFragment;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.widget.FrameLayout;

public class ReaderActivity2 extends Activity {

	private ReaderFragment readerFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FrameLayout frame = new FrameLayout(this);

		if (savedInstanceState == null) {
			readerFragment = new ReaderFragment();
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.add(frame.getId(), readerFragment).commit();
		}
		setContentView(frame);
	}
}
