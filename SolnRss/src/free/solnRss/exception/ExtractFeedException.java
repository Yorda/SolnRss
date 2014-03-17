package free.solnRss.exception;

public class ExtractFeedException extends Exception {
	private static final long serialVersionUID = 350047721669140096L;
	
	public static enum Error {
		
		BAD_URL(0x0000001, "Bad url entered."), 
		
		GET_HTTP_DATA(0x00000002,"An error occured when loading web site data."), 
		
		GET_FEED_INFO(0x00000003, "An error occured when searching a web site's RSS feed.");

		private final int id;
		private final String message;

		Error(int id, String message) {
			this.id = id;
			this.message = message;
		}

		public int getId() {
			return id;
		}

		public String getMessage() {
			return message;
		}
	}

	private Error error;
	
	public ExtractFeedException(Error error) {
		super(error.getMessage());
		this.setError(error);
	}

	public Error getError() {
		return error;
	}

	public void setError(Error error) {
		this.error = error;
	}
	
}
