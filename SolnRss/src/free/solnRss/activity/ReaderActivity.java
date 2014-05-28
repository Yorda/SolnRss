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
	
	private String link, publicationTitle, text, syndicationName;
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
		
		this.syndicationName = getIntent().getStringExtra("syndicationName");

		if (!TextUtils.isEmpty(syndicationName)) {
			getActionBar().setTitle(Html.fromHtml("<b><u>" + syndicationName + "</u><b>"));
		} else {
			getActionBar().setTitle(Html.fromHtml("<b><u>" + this.getTitle().toString()	+ "</u><b>"));
		}

		this.publicationTitle = getIntent().getStringExtra("title");
		if (!TextUtils.isEmpty(publicationTitle)) {
			TextView tv = (TextView) findViewById(R.id.reader_title);
			tv.setTypeface(TypeFaceSingleton.getInstance(this).getUserTypeFace(), Typeface.BOLD);
			
			tv.setText(publicationTitle);
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
		settings.setAllowFileAccess(true);
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
			sendIntent.putExtra(Intent.EXTRA_TEXT, publicationTitle + ": " + link);
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
	/*
	String s = "<html>\r\n" + 
			" <head></head>\r\n" + 
			" <body>\r\n" + 
			"  <div class=\"ob-section ob-section-text\"> \r\n" + 
			"   <a href=\"http://img.over-blog-kiwi.com/1/04/43/04/20140528/ob_be21ac_4426781-3-b515-l-hexachlorobenzene-hcb.jpg\" \r\n" + 
			"   class=\"ob-link-img\" target=\"_blank\">\r\n" + 
			"   <img src=\"file:///data/data/free.solnRss/files/1860214005.jpg\" class=\"ob-media ob-img ob-pull-left ob-media-left\"   alt=\"++++ALT++++\" /> </a> \r\n" + 
			"   <div class=\"ob-text\"> \r\n" + 
			"    <p>On a d&eacute;j&agrave; assez de d&eacute;chets comme cela, et pas que radioactifs, non ?</p> \r\n" + 
			"   </div> \r\n" + 
			"  </div> \r\n" + 
			"  <div class=\"ob-section ob-section-html\">\r\n" + 
			"   <p id=\"yui_3_5_0_1_1401111426569_200423\"><span style=\"font-size:26px;\"><span style=\"font-family:georgia,serif;\"><span style=\"color:#000000;\">Une entreprise australienne cherche &agrave; refourger ses 15 000 tonnes de d&eacute;chets radioactifs canc&eacute;rig&egrave;nes &agrave; la France</span></span></span></p> \r\n" + 
			"   <p><br /> <span style=\"font-size:16px;\"><span style=\"font-family:georgia,serif;\"><strong><span style=\"color:#000000;\">Le groupe chimique australien Orica a annonc&eacute;, mardi 27 mai,</span><a href=\"http://conjugaison.lemonde.fr/conjugaison/auxiliaire/avoir\" target=\"_self\"><span style=\"color:#000000;\">avoir</span></a><span style=\"color:#000000;\">&nbsp;d&eacute;pos&eacute; une demande d'envoi vers la&nbsp;</span><a href=\"http://www.lemonde.fr/europeennes-france/\" target=\"_self\"><span style=\"color:#000000;\">France</span></a><span style=\"color:#000000;\">&nbsp;d'un chargement de d&eacute;chets hautement toxiques, qu'il tente d'</span><a href=\"http://conjugaison.lemonde.fr/conjugaison/premier-groupe/envoyer\" target=\"_self\"><span style=\"color:#000000;\">envoyer</span></a><span style=\"color:#000000;\">&nbsp;&agrave; l'&eacute;tranger depuis des ann&eacute;es, faute de&nbsp;</span><a href=\"http://conjugaison.lemonde.fr/conjugaison/troisieme-groupe/pouvoir\" target=\"_self\"><span style=\"color:#000000;\">pouvoir</span></a><span style=\"color:#000000;\">, selon lui, le&nbsp;</span><a href=\"http://conjugaison.lemonde.fr/conjugaison/troisieme-groupe/d%C3%A9truire\" target=\"_self\"><span style=\"color:#000000;\">d&eacute;truire</span></a><span style=\"color:#000000;\">&nbsp;en</span><a href=\"http://www.lemonde.fr/australie/\" target=\"_self\"><span style=\"color:#000000;\">Australie</span></a><span style=\"color:#000000;\">.</span></strong></span></span></p> \r\n" + 
			"   <p><span style=\"font-size:16px;\"><span style=\"font-family:georgia,serif;\"><span style=\"color:#000000;\">Le groupe poss&egrave;de 15&nbsp;000 tonnes d'hexachlorobenz&egrave;ne (HCB), un produit soup&ccedil;onn&eacute; d'</span><a href=\"http://conjugaison.lemonde.fr/conjugaison/auxiliaire/%C3%AAtre\" target=\"_self\"><span style=\"color:#000000;\">&ecirc;tre</span></a><span style=\"color:#000000;\">&nbsp;canc&eacute;rog&egrave;ne pour l'homme et dont l'utilisation est interdite dans la Communaut&eacute; europ&eacute;enne depuis 1981. Il est parfois incin&eacute;r&eacute; pour&nbsp;</span><a href=\"http://conjugaison.lemonde.fr/conjugaison/auxiliaire/%C3%AAtre\" target=\"_self\"><span style=\"color:#000000;\">&ecirc;tre</span></a><span style=\"color:#000000;\">d&eacute;truit.</span></span></span></p> \r\n" + 
			"   <p><span style=\"font-size:16px;\"><span style=\"font-family:georgia,serif;\"><span style=\"color:#000000;\">L'hexachlorobenz&egrave;ne d&eacute;coule de plusieurs proc&eacute;d&eacute;s industriels de chloration, souvent associ&eacute;s aux usines de production de chlore et de soude caustique. Il peut&nbsp;</span><a href=\"http://conjugaison.lemonde.fr/conjugaison/premier-groupe/persister\" target=\"_self\"><span style=\"color:#000000;\">persister</span></a><span style=\"color:#000000;\">&nbsp;longtemps dans l'environnement.</span></span></span></p> \r\n" + 
			"   <p>&nbsp;</p> \r\n" + 
			"   <p><span style=\"color:#000000;\"><span style=\"font-size:16px;\"><span style=\"font-family:georgia,serif;\"><strong>&nbsp;UN TEST AVEC 132 TONNES</strong></span></span></span></p> \r\n" + 
			"   <p><span style=\"font-size:16px;\"><span style=\"font-family:georgia,serif;\"><span style=\"color:#000000;\">L'Australie ne dispose pas d'infrastructures permettant la destruction de ces d&eacute;chets, et des demandes d&eacute;pos&eacute;es en&nbsp;</span><a href=\"http://www.lemonde.fr/allemagne/\" target=\"_self\"><span style=\"color:#000000;\">Allemagne</span></a><span style=\"color:#000000;\">&nbsp;(2007) et au&nbsp;</span><a href=\"http://www.lemonde.fr/danemark/\" target=\"_self\"><span style=\"color:#000000;\">Danemark</span></a><span style=\"color:#000000;\">(2010) ont &eacute;t&eacute; refus&eacute;es par les gouvernements de ces deux pays, apr&egrave;s les protestations de groupes de&nbsp;</span><a href=\"http://www.lemonde.fr/defense/\" target=\"_self\"><span style=\"color:#000000;\">d&eacute;fense</span></a><span style=\"color:#000000;\">&nbsp;de l'environnement, dont Greenpeace.</span></span></span></p> \r\n" + 
			"   <p><span style=\"font-size:16px;\"><span style=\"font-family:georgia,serif;\"><span style=\"color:#000000;\">Le groupe a indiqu&eacute;&nbsp;</span><a href=\"http://conjugaison.lemonde.fr/conjugaison/auxiliaire/avoir\" target=\"_self\"><span style=\"color:#000000;\">avoir</span></a><span style=\"color:#000000;\">&nbsp;d&eacute;pos&eacute; une demande aupr&egrave;s du groupe fran&ccedil;ais Tredi SA, qui dispose des infrastructures n&eacute;cessaires &agrave; la destruction de ces d&eacute;chets. Quelque 132 tonnes de HCB seraient envoy&eacute;es dans un premier temps. Si l'op&eacute;ration r&eacute;ussit, le reste de la cargaison suivrait.</span></span></span></p> \r\n" + 
			"   <p><span style=\"font-size:16px;\"><span style=\"font-family:georgia,serif;\"><span style=\"color:#000000;\">L'Australie est signataire des conventions de B&acirc;le (1989) et de Stockholm (2001). La premi&egrave;re r&eacute;glemente les&nbsp;</span><a href=\"http://www.lemonde.fr/transports/\" target=\"_self\"><span style=\"color:#000000;\">transports</span></a><span style=\"color:#000000;\">&nbsp;de d&eacute;chets dangereux et la seconde vise &agrave;&nbsp;</span><a href=\"http://conjugaison.lemonde.fr/conjugaison/premier-groupe/%C3%A9liminer\" target=\"_self\"><span style=\"color:#000000;\">&eacute;liminer</span></a><span style=\"color:#000000;\">&nbsp;les polluants organiques persistants. Orica indique que sa demande aupr&egrave;s de la France respecte ces deux conventions.</span></span></span></p> \r\n" + 
			"   <p><span style=\"font-size:16px;\"><span style=\"font-family:georgia,serif;\"><span style=\"color:#000000;\">L'ONG Greenpeace s'est oppos&eacute;e au transport de ces mat&eacute;riaux, en raison des risques d'accident en mer, et &agrave; leur incin&eacute;ration, en raison des risques de pollution de l'air. La quantit&eacute; de d&eacute;chets est telle qu'Orica peut&nbsp;</span><a href=\"http://conjugaison.lemonde.fr/conjugaison/troisieme-groupe/construire\" target=\"_self\"><span style=\"color:#000000;\">construire</span></a><span style=\"color:#000000;\">&nbsp;les infrastructures n&eacute;cessaires pour la destruction de ces mat&eacute;rieux autrement que par l'incin&eacute;ration, estime Adam Walters, charg&eacute; de la recherche au sein de Greenpeace pour la r&eacute;gion&nbsp;</span><a href=\"http://www.lemonde.fr/asie-pacifique/\" target=\"_self\"><span style=\"color:#000000;\">Asie-Pacifique</span></a><span style=\"color:#000000;\">.&nbsp;</span><em><span style=\"color:#000000;\">&laquo;&nbsp;Mais ils veulent juste&nbsp;</span><a href=\"http://conjugaison.lemonde.fr/conjugaison/premier-groupe/envoyer\" target=\"_self\"><span style=\"color:#000000;\">envoyer</span></a><span style=\"color:#000000;\">tout &ccedil;a &agrave; l'&eacute;tranger. &raquo;</span></em></span></span></p> \r\n" + 
			"   <p>&nbsp;</p> \r\n" + 
			"   <p><span style=\"font-size:16px;\"><span style=\"font-family:georgia,serif;\"><em><a href=\"http://www.lemonde.fr/planete/article/2014/05/27/le-groupe-chimique-orica-veut-envoyer-en-france-ses-dechets-toxiques_4426693_3244.html\" target=\"_self\"><span style=\"color:#000000;\">source</span></a></em></span></span></p> \r\n" + 
			"  </div> \r\n" + 
			" </body>\r\n" + 
			"</html>";*/
}
