package free.solnRss.activity;


import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.widget.FrameLayout;
import free.solnRss.fragment.ReaderFragment;


public class ReaderActivity2 extends Activity {

	private ReaderFragment	readerFragment;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final FrameLayout frame = new FrameLayout(this);

		if (savedInstanceState == null) {
			readerFragment = new ReaderFragment();
			final FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.add(frame.getId(), readerFragment).commit();
		}

		setContentView(frame);
	}
}
