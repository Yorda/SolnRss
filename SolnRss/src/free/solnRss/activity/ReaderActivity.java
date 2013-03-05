package free.solnRss.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import free.solnRss.R;

/**
 * Display publication's description vale
 * 
 * @author jftomasi
 * 
 */
public class ReaderActivity extends Activity {
	private String link;
	final int layoutID = R.layout.activity_reader;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(layoutID);
		String text = getIntent().getStringExtra("read");
		link = getIntent().getStringExtra("link");

		TextView tv0 = (TextView) findViewById(R.id.reader);
		tv0.setText(Html.fromHtml(text));
	}
	
	
	public void goToSite(View v){
		Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
		startActivity(openUrlIntent);
	}
}
