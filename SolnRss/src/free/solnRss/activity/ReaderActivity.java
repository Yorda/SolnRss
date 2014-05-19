package free.solnRss.activity;

import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;
import free.solnRss.R;
import free.solnRss.repository.PublicationRepository;
import free.solnRss.singleton.TypeFaceSingleton;

/**
 * Display publication's description vale
 * 
 * @author jftomasi
 * 
 */
public class ReaderActivity extends Activity {
	
	private String link, title, text;
	private Integer publicationId, syndicationId;
	private boolean isFavorite;
	private PublicationRepository publicationRepository;

	@SuppressLint("SetJavaScriptEnabled")
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_reader);

		int apiVersion = android.os.Build.VERSION.SDK_INT;
		if (apiVersion >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			getActionBar().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
			getActionBar().setStackedBackgroundDrawable(new ColorDrawable(Color.WHITE));
		}
		
		this.title = getIntent().getStringExtra("title");
		
		if (!TextUtils.isEmpty(title)) {
			getActionBar().setTitle(Html.fromHtml("<b><u>" + this.getTitle().toString()	+ "</u><b>"));
			
			TextView tv = (TextView) findViewById(R.id.reader_title);
			
			tv.setTypeface(TypeFaceSingleton.getInstance(this).getUserTypeFace(), Typeface.BOLD);
			tv.setText(title);
			
			
			getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
		}

		getActionBar().setBackgroundDrawable(
				new ColorDrawable(0xeeeeee));
		getActionBar().setStackedBackgroundDrawable(
				new ColorDrawable(0xeeeeee));
	
		this.text          = getIntent().getStringExtra("read");
		this.link          = getIntent().getStringExtra("link");
		this.isFavorite    = getIntent().getBooleanExtra("isFavorite", false);
		this.publicationId = getIntent().getIntExtra("publicationId", -1);
		this.syndicationId = getIntent().getIntExtra("syndicationId", -1);
		
		WebView webView = (WebView) findViewById(R.id.reader);		
		WebSettings settings = webView.getSettings();
		settings.setDefaultTextEncodingName("utf-8");
		
		// For enable video
		webView.setWebChromeClient(new WebChromeClient());

		settings.setJavaScriptEnabled(true);
		
		settings.setDefaultFontSize(TypeFaceSingleton.getInstance(getApplicationContext()).getUserFontSize());
		
		webView.loadDataWithBaseURL(null, getHtmlData(getApplicationContext(), text) , "text/html", "utf-8", null);
		
		publicationRepository = new PublicationRepository(this);
	}
	
	@Override
	protected void onPause() {
		// When press home button
		if (!isFinishing()) {
			this.finish();
		}
		super.onPause();
	}
	
	private void goToSite() {
		Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
		try {
			startActivity(openUrlIntent);
		} catch (Exception e) {
			Toast.makeText(this, this.getResources().getString(R.string.open_browser_bad_url),
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.reader_menu, menu);
		
		if (isFavorite) {
			MenuItem item = menu.getItem(1);
			item.setIcon(R.drawable.ic_favorite_full);
		}
		
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.reader_go_to_site:
			goToSite();
			this.finish();
			return true;
			
		case R.id.reader_menu_add_to_favorite:
			publicationRepository.markOnePublicationAsFavorite(publicationId, (isFavorite ? 1 : 0));
			isFavorite = !isFavorite;
			if (isFavorite) {
				Toast.makeText(this, R.string.add_favorite, Toast.LENGTH_SHORT).show();
				item.setIcon(R.drawable.ic_favorite_full);
			}
			else {
				Toast.makeText(this, R.string.remove_favorite, Toast.LENGTH_SHORT).show();
				item.setIcon(R.drawable.ic_favorite_empty);
			}
			break;
			
		case R.id.reader_menu_share:
			Intent sendIntent = new Intent();
			sendIntent.setAction(Intent.ACTION_SEND);
			sendIntent.putExtra(Intent.EXTRA_TEXT, title + ": " + link);
			sendIntent.setType("text/plain");
			startActivity(Intent.createChooser(sendIntent, getResources().getString(R.string.menu_Share)));
			break;

		case R.id.reader_menu_delete:
			try {
				publicationRepository.deletePublication(publicationId, syndicationId);
				Toast.makeText(this, R.string.Delete, Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				Toast.makeText(this, R.string.Delete_fail, Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
			this.finish();
			break;

		default:
			return super.onOptionsItemSelected(item);
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
	
	@SuppressLint("DefaultLocale")
	private String improveHtml(String html) {
		
		Document document = Jsoup.parse(html);
		
		for (Element e : document.getAllElements()) {

			if (e.hasAttr("style")) {
				for (Attribute a : e.attributes()) {
					if (a.getKey().compareTo("style") == 0) {
						String[] items = a.getValue().trim().split(";");
						String newValue = "";
						for (String item : items) {
							if (!item.toLowerCase(Locale.ENGLISH).contains("font-family:") 
									&& !item.toLowerCase(Locale.ENGLISH).contains("font-size:")) {
								newValue = newValue.concat(item).concat(";");
							}
						}
						a.setValue(newValue);
					}
				}
			}
		}
	
		return document.body().html();
	}

	
	private String getHtmlData(Context context, String data) {
		
		final String head = "<head>" +
				"<style>@font-face {" +
				" font-family: 'monaco';" +
				" src: url('file:///android_asset/fonts/monaco/monaco.ttf'); " +
				" font-weight: bold; " + 
				" font-style: normal;" +
				" }" +
				" iframe {max-width: 100%; width:auto; height: auto;}  " +
				" img {max-width: 100%; width:auto; height: auto;}" +
				" body {font-family: 'monaco'; font-size:16sp; max-width: 100%; width:auto; height: auto;}</style>" +
				"</head>";
		
		StringBuilder sb = new StringBuilder("<html>")
			.append(head)
			.append("<body>")
			.append(improveHtml(data))
			.append("</body></html>");
		
	    return sb.toString();
	 }
}
