package free.solnRss.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.Toast;
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

		/*TextView textView = (TextView) findViewById(R.id.reader);
		textView.setText(Html.fromHtml(text));
		textView.setMovementMethod(LinkMovementMethod.getInstance());*/
		
		WebView webView = (WebView) findViewById(R.id.reader);		
		WebSettings settings = webView.getSettings();
		settings.setDefaultTextEncodingName("utf-8");
		webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		webView.loadDataWithBaseURL(null, text, "text/html", "utf-8", null);
	}

	private void goToSite() {

		Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
		try {
			startActivity(openUrlIntent);
		} catch (Exception e) {
			Toast.makeText(this, 
				this.getResources()	.getString(
					R.string.open_browser_bad_url),
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.reader_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.read_on_site:
			goToSite();
			this.finish();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	// somewhere on your code...
			/*WebViewClient yourWebClient = new WebViewClient(){
			    // you tell the webclient you want to catch when a url is about to load
			    @Override
			    public boolean shouldOverrideUrlLoading(WebView  view, String  url){
			        return true;
			    }
			    // here you execute an action when the URL you want is about to load
			    @Override
			    public void onLoadResource(WebView  view, String  url){
			        if( url.equals("http://cnn.com") ){
			            // do whatever you want
			           //download the image from url and save it whereever you want
			        }
			    }
			};*/
}
