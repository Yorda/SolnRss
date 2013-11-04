package free.solnRss.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.PluginState;
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

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reader);

		getActionBar().setBackgroundDrawable(new ColorDrawable(0xeeeeee));
		
		String text = getIntent().getStringExtra("read");
		String title = getIntent().getStringExtra("title");
		
		if (!TextUtils.isEmpty(title)) {
			getActionBar().setTitle(Html.fromHtml("<b><u>" + title + "</u><b>"));
		}
		
		link = getIntent().getStringExtra("link");
	
		WebView webView = (WebView) findViewById(R.id.reader);		
		WebSettings settings = webView.getSettings();
		settings.setDefaultTextEncodingName("utf-8");
		
		// For enable video
		webView.setWebChromeClient(new WebChromeClient());
		settings.setPluginState(PluginState.ON);
		settings.setJavaScriptEnabled(true);
		
		webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		webView.loadDataWithBaseURL(null, text, "text/html", "utf-8", null);
	}

	/*String t = "<html>\r\n" + 
			"<head>\r\n" + 
			"<style type=\"text/css\">\r\n" + 
			"@font-face {\r\n" + 
			"    font-family: MyFont;\r\n" + 
			"    src: url(\"file:///assets/fonts/monofur/MONOF55.TTF\")\r\n" + 
			"}\r\n" + 
			"body {\r\n" + 
			"    font-family: MyFont;\r\n" + 
			"    font-size: 18;\r\n" + 
			"    text-align: justify;\r\n" + 
			"}\r\n" + 
			"</style>\r\n" + 
			"</head>\r\n" + 
			"<body>\r\n" + 
			"Your text can go here! Your text can go here! Your text can go here!\r\n" + 
			"</body>\r\n" + 
			"</html>";*/
	
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
