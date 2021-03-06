package free.solnRss.activity;


import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.annotation.SuppressLint;
import android.app.Activity;
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

	final String					tag	= ReaderActivity.class.getName();
	private String					link, publicationTitle, syndicationName;
	private Integer					publicationId, syndicationId;
	private boolean					isFavorite;
	private PublicationRepository	publicationRepository;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_reader);

		final int apiVersion = android.os.Build.VERSION.SDK_INT;
		if (apiVersion >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			getActionBar().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
			getActionBar().setStackedBackgroundDrawable(new ColorDrawable(Color.WHITE));
		}

		syndicationName = getIntent().getStringExtra("syndicationName");

		if (TextUtils.isEmpty(syndicationName)) {
			getActionBar().setTitle(Html.fromHtml("<b><u>" + getTitle().toString() + "</u><b>"));
		} else {
			getActionBar().setTitle(Html.fromHtml("<b><u>" + syndicationName + "</u><b>"));
		}

		publicationTitle = getIntent().getStringExtra("title");
		if (!TextUtils.isEmpty(publicationTitle)) {
			final TextView tv = (TextView) findViewById(R.id.reader_title);
			tv.setTypeface(TypeFaceSingleton.getInstance(this).getUserTypeFace(), Typeface.BOLD);

			tv.setText(publicationTitle);
			getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
		}

		getActionBar().setBackgroundDrawable(new ColorDrawable(0xeeeeee));
		getActionBar().setStackedBackgroundDrawable(new ColorDrawable(0xeeeeee));

		final String text = getIntent().getStringExtra("read");
		link = getIntent().getStringExtra("link");
		isFavorite = getIntent().getBooleanExtra("isFavorite", false);
		publicationId = getIntent().getIntExtra("publicationId", -1);
		syndicationId = getIntent().getIntExtra("syndicationId", -1);

		final WebView webView = (WebView) findViewById(R.id.reader);
		final WebSettings settings = webView.getSettings();
		settings.setAllowFileAccess(true);
		settings.setDefaultTextEncodingName("utf-8");

		// For enable video
		webView.setWebChromeClient(new WebChromeClient());

		settings.setJavaScriptEnabled(true);

		settings.setDefaultFontSize(TypeFaceSingleton.getInstance(getApplicationContext()).getUserFontSize());

		webView.loadDataWithBaseURL(null, getHtmlData(text), "text/html", "utf-8", null);

		publicationRepository = new PublicationRepository(this);
	}

	@Override
	protected void onNewIntent(final Intent intent) {
		super.onNewIntent(intent);
		finish();
		startActivity(intent);
	}

	private void goToSite() {
		final Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
		try {
			startActivity(openUrlIntent);
		} catch (final Exception e) {
			Toast.makeText(this, getResources().getString(R.string.open_browser_bad_url), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {

		final MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.reader_menu, menu);

		if (isFavorite) {
			final MenuItem item = menu.getItem(1);
			item.setIcon(R.drawable.ic_favorite_full);
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case R.id.reader_go_to_site:
				goToSite();
				finish();
				return true;

			case R.id.reader_menu_add_to_favorite:
				publicationRepository.markOnePublicationAsFavorite(publicationId, isFavorite ? 1 : 0);
				isFavorite = !isFavorite;
				if (isFavorite) {
					Toast.makeText(this, R.string.add_favorite, Toast.LENGTH_SHORT).show();
					item.setIcon(R.drawable.ic_favorite_full);
				} else {
					Toast.makeText(this, R.string.remove_favorite, Toast.LENGTH_SHORT).show();
					item.setIcon(R.drawable.ic_favorite_empty);
				}
				break;

			case R.id.reader_menu_share:
				final Intent sendIntent = new Intent();
				sendIntent.setAction(Intent.ACTION_SEND);
				sendIntent.putExtra(Intent.EXTRA_TEXT, publicationTitle + ": " + link);
				sendIntent.setType("text/plain");
				startActivity(Intent.createChooser(sendIntent, getResources().getString(R.string.menu_Share)));
				break;

			case R.id.reader_menu_delete:
				try {
					publicationRepository.deletePublication(publicationId, syndicationId);
					Toast.makeText(this, R.string.Delete, Toast.LENGTH_SHORT).show();
				} catch (final Exception e) {
					Toast.makeText(this, R.string.Delete_fail, Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}
				finish();
				break;

			default:
				return super.onOptionsItemSelected(item);
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@SuppressLint("DefaultLocale")
	private String improveHtml(final String html) {

		final Document document = Jsoup.parse(html);

		for (final Element e : document.getAllElements()) {
			if (e.hasAttr("style")) {

				for (final Attribute a : e.attributes()) {
					if (a.getKey().compareTo("style") == 0) {
						final String[] items = a.getValue().trim().split(";");
						String newValue = "";
						for (final String item : items) {
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

	private String getHtmlData(final String data) {

		final String head = "<head>" + "<style>@font-face {" + " font-family: 'monaco';"
				+ " src: url('file:///android_asset/fonts/monaco/monaco.ttf'); " + " font-weight: bold; " + " font-style: normal;" + " }"
				+ " iframe {max-width: 100%; width:auto; height: auto;}  " + " img {max-width: 100%; width:auto; height: auto;}"
				+ " body {font-family: 'monaco'; font-size:16sp; max-width: 100%; width:auto; height: auto;}</style>" + "</head>";

		final StringBuilder sb = new StringBuilder("<html>").append(head).append("<body>").append(improveHtml(data)).append("</body></html>");

		return sb.toString();
	}
}
