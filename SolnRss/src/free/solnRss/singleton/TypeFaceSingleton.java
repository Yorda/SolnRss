package free.solnRss.singleton;

public class TypeFaceSingleton {

	private TypeFaceSingleton typeFaceSingleton;

	private TypeFaceSingleton() {

	}

	public TypeFaceSingleton getInstance() {
		if (typeFaceSingleton == null) {
			typeFaceSingleton = new TypeFaceSingleton();
		}
		return typeFaceSingleton;
	}
}
