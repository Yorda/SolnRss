package free.solnRss.adapter;

import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;
import free.solnRss.R;
import free.solnRss.fragment.CategoriesFragment;
import free.solnRss.fragment.PublicationsFragment2;
import free.solnRss.fragment.SyndicationsFragment;

public class SectionsPagerAdapter extends FragmentPagerAdapter{ //FragmentStatePagerAdapter {

	public static enum TAB_SELECTED {
		Categories(0), 
		Publications(1), 
		Syndications(2);
		
		private TAB_SELECTED(int pos) {
			
		}
	}

	private PublicationsFragment2 publicationsFragment;
	private SyndicationsFragment syndicationsFragment;
	private CategoriesFragment categoriesFragment;
	private Resources r;

	public SectionsPagerAdapter(FragmentManager fm, Resources r) {
		super(fm);
		syndicationsFragment = new SyndicationsFragment();
		publicationsFragment = new PublicationsFragment2();
		categoriesFragment   = new CategoriesFragment();
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
		Fragment fragment = null;
		switch (TAB_SELECTED.values()[position]) {
		case Categories:
			fragment = categoriesFragment;
			break;
		case Publications:
			fragment = publicationsFragment;
			break;
		case Syndications:
			fragment = syndicationsFragment;
			break;
		}
		return fragment;
	}
	
	@Override
	public int getCount() {
		return TAB_SELECTED.values().length;
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
