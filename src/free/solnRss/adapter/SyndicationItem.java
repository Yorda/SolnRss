package free.solnRss.adapter;

import android.widget.TextView;

public class SyndicationItem {

	private TextView title;
	private TextView numberOfClick;
	private TextView lastSearchError;

	public SyndicationItem() {

	}

	public TextView getTitle() {
		return title;
	}

	public void setTitle(TextView title) {
		this.title = title;
	}

	public TextView getNumberOfClick() {
		return numberOfClick;
	}

	public void setNumberOfClick(TextView numberOfClick) {
		this.numberOfClick = numberOfClick;
	}

	public TextView getLastSearchError() {
		return lastSearchError;
	}

	public void setLastSearchError(TextView lastSearchError) {
		this.lastSearchError = lastSearchError;
	}
}
