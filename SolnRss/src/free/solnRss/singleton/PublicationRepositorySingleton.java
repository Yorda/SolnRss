package free.solnRss.singleton;

import free.solnRss.repository.PublicationRepository;
import android.content.Context;

public class PublicationRepositorySingleton {

	private static PublicationRepositorySingleton instance;
	protected PublicationRepository repository;

	public PublicationRepositorySingleton(Context context) {
		repository = new PublicationRepository(context);
	}

	public static PublicationRepositorySingleton getInstance(Context context) {
		if (instance == null) {
			instance = new PublicationRepositorySingleton(context);
		}
		return instance;
	}

	public PublicationRepository getPublicationRepository() {
		return repository;
	}
}
