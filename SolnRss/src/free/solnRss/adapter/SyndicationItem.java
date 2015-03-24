package free.solnRss.adapter;


import android.widget.TextView;


public class SyndicationItem {

	private TextView	title;
	private TextView	numberOfClick;
	private TextView	lastSearchError;

	public SyndicationItem() {

	}

	public TextView getTitle() {
		return title;
	}

	public void setTitle(final TextView title) {
		this.title = title;
	}

	public TextView getNumberOfClick() {
		return numberOfClick;
	}

	public void setNumberOfClick(final TextView numberOfClick) {
		this.numberOfClick = numberOfClick;
	}

	public TextView getLastSearchError() {
		return lastSearchError;
	}

	public void setLastSearchError(final TextView lastSearchError) {
		this.lastSearchError = lastSearchError;
	}
}
