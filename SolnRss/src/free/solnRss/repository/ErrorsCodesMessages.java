package free.solnRss.repository;


public enum ErrorsCodesMessages {

	BAD_URL(0x0000001, "Bad url entered."),

	GET_HTTP_DATA(0x00000002, "An error occured when loading web site data."),

	GET_FEED_INFO(0x00000003, "An error occured when searching a web site's RSS feed.");

	private final int		id;
	private final String	message;

	ErrorsCodesMessages(final int id, final String message) {
		this.id = id;
		this.message = message;
	}

	public int getId() {
		return id;
	}

	public String getMessage() {
		return message;
	}

	public String getMessage(final int id) {

		return message;
	}

}
