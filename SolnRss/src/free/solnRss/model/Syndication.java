package free.solnRss.model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntry;

import free.solnRss.utility.SyndicateUtil;


public class Syndication implements Serializable {
	private static final long	serialVersionUID	= -5995704417797241060L;
	private Integer				id;
	private String				name;
	private String				url;
	private String				websiteUrl;
	private List<Publication>	publications;
	private String				rss;
	private String				oldRss;
	SyndicateUtil				syndicateUtil		= new SyndicateUtil();

	private List<SyndEntry>		entries;

	public boolean isPublicationAlreadyRecorded(final String title, final String url) throws Exception {

		if (entries == null) {
			syndicateUtil.init(oldRss);
			entries = syndicateUtil.lastEntries();
		}

		//String buff = null;
		boolean isAlreadyRecorded = false;
		for (final SyndEntry entry : entries) {
			//buff = StringUtil.unescapeHTML(entry.getTitle());
			if (entry.getLink().trim().compareTo(url.trim()) == 0) {
				// && buff.trim().compareTo(title.trim()) == 0) {
				isAlreadyRecorded = true;
				break;
			}
		}

		return isAlreadyRecorded;
	}

	public Syndication() {
		setPublications(new ArrayList<Publication>());
	}

	public void addPublication(final Publication publication) {
		publications.add(publication);
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(final String url) {
		this.url = url;
	}

	public List<Publication> getPublications() {
		return publications;
	}

	public void setPublications(final List<Publication> publications) {
		this.publications = publications;
	}

	public String getWebsiteUrl() {
		return websiteUrl;
	}

	public void setWebsiteUrl(final String websiteUrl) {
		this.websiteUrl = websiteUrl;
	}

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public String getRss() {
		return rss;
	}

	public void setRss(final String rss) {
		this.rss = rss;
	}

	public String getOldRss() {
		return oldRss;
	}

	public void setOldRss(final String oldRss) {
		this.oldRss = oldRss;
	}
}
