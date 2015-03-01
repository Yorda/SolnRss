package free.solnRss.model;

import java.io.Serializable;
import java.util.Date;

public class Publication implements Serializable{
	private static final long serialVersionUID = 1015800126769745622L;
	private Integer syndicationId;
	private String url;
	private Date publicationDate;
	private String title;
	private String description;
	
	public Publication(String url, Date publicationDate, String title, String description) {
		super();
		this.url = url;
		this.publicationDate = publicationDate;
		this.title = title;
		this.description = description;
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
}
