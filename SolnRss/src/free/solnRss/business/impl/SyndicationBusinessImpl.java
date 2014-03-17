package free.solnRss.business.impl;

import java.net.URL;
import java.util.List;

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndContent;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntry;

import free.solnRss.business.SyndicationBusiness;
import free.solnRss.exception.ExtractFeedException;
import free.solnRss.model.Publication;
import free.solnRss.model.Syndication;
import free.solnRss.utility.HttpUtil;
import free.solnRss.utility.SyndicateUtil;
import free.solnRss.utility.WebSiteUtil;

public class SyndicationBusinessImpl implements SyndicationBusiness {
	private SyndicateUtil syndicateUtil;

	public SyndicationBusinessImpl() {
		syndicateUtil = new SyndicateUtil();
	}

	@Override
	public Syndication retrieveSyndicationContent(String html, String url)
			throws Exception {
		return setSyndication(html, url);
	}

	@Override
	public List<SyndEntry> newRssPublished(String url)
			throws ExtractFeedException {
		List<SyndEntry> entries = null;

		if (!HttpUtil.isValidUrl(url)) {
			throw new ExtractFeedException(ExtractFeedException.Error.BAD_URL);
		}
		String rss = null;

		try {
			rss = HttpUtil.retrieveHtml(url);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ExtractFeedException(
					ExtractFeedException.Error.GET_HTTP_DATA);
		}

		try {
			syndicateUtil.init(rss);
			entries = syndicateUtil.lastEntries();

		} catch (Exception e) {
			throw new ExtractFeedException(
					ExtractFeedException.Error.GET_FEED_INFO);
		}
		return entries;
	}

	/**
	 * 
	 * @param html
	 * @param url
	 * @return An object syndication otherwise null if no syndication
	 *         information found
	 * @throws Exception
	 */
	private Syndication setSyndication(String html, String url)
			throws Exception {
		Syndication syndication = new Syndication();
		syndication.setWebsiteUrl(url);

		// If html is already a feed
		if (syndicateUtil.isFeed(html)) {
			syndication.setUrl(url);
			syndicateUtil.init(html);
			syndication.setRss(html);

		} else {
			// Try to find rss url in html
			String syndicationUrl = WebSiteUtil.searchSyndicate(html, url);
			if (syndicationUrl == null) {
				return null;
			}

			syndication.setUrl(syndicationUrl);
			syndicateUtil.init(new URL(syndicationUrl));

			syndication.setRss(HttpUtil.retrieveHtml(syndicationUrl));
		}

		syndication.setName(syndicateUtil.syndicationName());
		List<SyndEntry> entries = syndicateUtil.lastEntries();
		Publication publication = null;
		String description;
		for (SyndEntry entry : entries) {

			description = entry.getDescription() != null ? entry
					.getDescription().getValue() : null;

			if (entry.getContents() != null && entry.getContents().size() > 0) {
				description = ((SyndContent) entry.getContents().get(0))
						.getValue();
			}

			publication = new Publication(entry.getLink(),
					entry.getPublishedDate(), entry.getTitle(), description);
			syndication.addPublication(publication);
		}

		return syndication;
	}
}
