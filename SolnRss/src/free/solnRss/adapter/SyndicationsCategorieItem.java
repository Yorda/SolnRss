package free.solnRss.adapter;


import android.widget.CheckBox;
import android.widget.TextView;


public class SyndicationsCategorieItem {
	private TextView	name;
	private CheckBox	check;

	public TextView getName() {
		return name;
	}

	public void setName(final TextView name) {
		this.name = name;
	}

	public CheckBox getCheck() {
		return check;
	}

	public void setCheck(final CheckBox check) {
		this.check = check;
	}
}
