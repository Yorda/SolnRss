package free.solnRss.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Syndication implements Serializable {
	private static final long serialVersionUID = -5995704417797241060L;
	private Integer id;
	private String name;
	private String url;
	private String websiteUrl;
	private List<Publication> publications;

	public Syndication() {
		setPublications(new ArrayList<Publication>());
	}

	public void addPublication(Publication publication) {
		publications.add(publication);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<Publication> getPublications() {
		return publications;
	}

	public void setPublications(List<Publication> publications) {
		this.publications = publications;
	}

	public String getWebsiteUrl() {
		return websiteUrl;
	}

	public void setWebsiteUrl(String websiteUrl) {
		this.websiteUrl = websiteUrl;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
}
