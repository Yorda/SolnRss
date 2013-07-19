package free.solnRss.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Resources;
import android.support.v13.app.FragmentPagerAdapter;
import android.view.ViewGroup;
import free.solnRss.R;
import free.solnRss.fragment.CategoriesFragment;
import free.solnRss.fragment.PublicationsFragment;
import free.solnRss.fragment.SyndicationsFragment;

public class SectionsPagerAdapter extends FragmentPagerAdapter {
     
	public static enum TAB_SELECTED {
		Categories(0), 
		Publications(1), 
		Syndications(2);
		
		private TAB_SELECTED(int pos) {
			
		}
	}

	private PublicationsFragment publicationsFragment  = new PublicationsFragment();
	private SyndicationsFragment syndicationsFragment  = new SyndicationsFragment();
	private CategoriesFragment categoriesFragment      = new CategoriesFragment();
	private Resources r;

	public SectionsPagerAdapter(FragmentManager fm, Resources r) {
		super(fm);
		/*syndicationsFragment = new SyndicationsFragment();
		publicationsFragment = new PublicationsFragment();
		categoriesFragment   = new CategoriesFragment();*/
		this.r = r;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		return super.instantiateItem(container, position);
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}
	
	@Override
	public Fragment getItem(int position) {
		// Method getItem is called to instantiate the fragment for the given page.
		//Fragment fragment = null;
		switch (TAB_SELECTED.values()[position]) {
		case Categories:
			return categoriesFragment;

		case Publications:
			return publicationsFragment;

		case Syndications:
			return syndicationsFragment;
		}
		return null;
	}
	
	@Override
	public int getCount() {
		return 3;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		switch (position) {
		case 0:
			return r.getString(R.string.title_categories);
		case 1:
			return r.getString(R.string.title_publications);
		case 2:
			return r.getString(R.string.title_syndications);
		}
		return null;
	}
	
	public static String getFragementTag(int viewId, int index) {
		return "android:switcher:" + viewId + ":" + index;
	}
}
