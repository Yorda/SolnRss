package free.solnRss.event;

import free.solnRss.state.PublicationsListState;



public class SyndicationEvent {

	private PublicationsListState state;

	public PublicationsListState getState() {
		return state;
	}

	public void setState(PublicationsListState state) {
		this.state = state;
	}
}
