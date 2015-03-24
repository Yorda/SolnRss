package free.solnRss.model;


import java.io.Serializable;
import java.util.Date;


public class Publication implements Serializable {
	private static final long	serialVersionUID	= 1015800126769745622L;
	private Integer				syndicationId;
	private String				url;
	private Date				publicationDate;
	private String				title;
	private String				description;

	public Publication(final String url, final Date publicationDate, final String title, final String description) {
		super();
		this.url = url;
		this.publicationDate = publicationDate;
		this.title = title;
		this.description = description;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(final String url) {
		this.url = url;
	}

	public Date getPublicationDate() {
		return publicationDate;
	}

	public void setPublicationDate(final Date publicationDate) {
		this.publicationDate = publicationDate;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public Integer getSyndicationId() {
		return syndicationId;
	}

	public void setSyndicationId(final Integer syndicationId) {
		this.syndicationId = syndicationId;
	}
}
