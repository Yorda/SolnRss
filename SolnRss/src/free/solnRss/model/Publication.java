package free.solnRss.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.text.TextUtils;

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndContent;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntry;

import free.solnRss.utility.ImageLoaderUtil;
import free.solnRss.utility.StringUtil;

public class Publication implements Serializable{
	private static final long serialVersionUID = 1015800126769745622L;
	private Integer syndicationId;
	private String url;
	private Date publicationDate;
	private String title;
	private String description;
	private String link;
	private List<PublicationImage> publicationImages;
	private ImageLoaderUtil imageLoader;
	
	public Publication(String url, Date publicationDate, String title, String description) {
		super();
		this.url = url;
		this.publicationDate = publicationDate;
		this.title = title;
		this.description = description;
		setDescriptionImages(new ArrayList<PublicationImage>());
	}
	
	public Publication(SyndEntry syndEntry) {
		this.link = syndEntry.getLink();
		this.title = syndEntry.getTitle();
		this.description = getDescription(syndEntry);
	}

	private String getDescription(SyndEntry syndEntry) {
		String description = null;

		if (syndEntry.getDescription() != null) {
			description = syndEntry.getDescription().getValue();
		}

		if (syndEntry.getContents() != null
				&& syndEntry.getContents().size() > 0) {
			description = ((SyndContent) syndEntry.getContents().get(0)).getValue();
		}
		
		if (description == null) {
			description = new String();
		}
		return description;
	}
	
	public String improveDescription() {
		String fixedDescription = description;
		
		if (!TextUtils.isEmpty(description)) {
			fixedDescription = StringUtil.unescapeHTML(fixedDescription);
			
			fixedDescription = fixYouTubeLinkInIFrame(fixedDescription);
		}
		return fixedDescription;
	}
	
	private String fixYouTubeLinkInIFrame(String description) {
		return description.replaceAll("src=\"//www.youtube.com/embed",
				"src=\"http://www.youtube.com/embed");
	}
	
	public void loadImages() {
		getImageLoader().saveImages(this);
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Date getPublicationDate() {
		return publicationDate;
	}
	public void setPublicationDate(Date publicationDate) {
		this.publicationDate = publicationDate;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getSyndicationId() {
		return syndicationId;
	}

	public void setSyndicationId(Integer syndicationId) {
		this.syndicationId = syndicationId;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public List<PublicationImage> getDescriptionImages() {
		return publicationImages;
	}

	public void setDescriptionImages(List<PublicationImage> descriptionImages) {
		this.publicationImages = descriptionImages;
	}

	public ImageLoaderUtil getImageLoader() {
		return imageLoader;
	}

	public void setImageLoader(ImageLoaderUtil imageLoader) {
		this.imageLoader = imageLoader;
	}
}
