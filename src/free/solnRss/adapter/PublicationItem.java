package free.solnRss.adapter;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class PublicationItem {

	private TextView title;
	private TextView name;
	private ImageView alreadyRead;
	private ImageButton favorite;
	private Integer isRead;

	public TextView getTitle() {
		return title;
	}

	public void setTitle(TextView title) {
		this.title = title;
	}

	public TextView getName() {
		return name;
	}

	public void setName(TextView name) {
		this.name = name;
	}

	public Integer getIsRead() {
		return isRead;
	}

	public void setIsRead(Integer isRead) {
		this.isRead = isRead;
	}

	public ImageView getAlreadyRead() {
		return alreadyRead;
	}

	public void setAlreadyRead(ImageView alreadyRead) {
		this.alreadyRead = alreadyRead;
	}

	public ImageButton getFavorite() {
		return favorite;
	}

	public void setFavorite(ImageButton favorite) {
		this.favorite = favorite;
	}

}
