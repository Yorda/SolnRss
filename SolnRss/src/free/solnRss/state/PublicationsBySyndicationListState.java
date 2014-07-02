package free.solnRss.state;

import free.solnRss.repository.PublicationRepository;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;

public class PublicationsBySyndicationListState extends
		AbstractPublicationListState {

	private Integer syndicationId;

	public void init(Context context) {
		this.context = context;
		repository = new PublicationRepository(this.context);
	}

	@Override
	public Loader<Cursor> displayList() {
		return repository.loadPublicationsBySyndication(filterText,
				syndicationId);
	}

	@Override
	public String getActionBarTitle() {
		return repository.syndicationName(syndicationId);
	}

	public void setSyndicationId(Integer syndicationId) {
		this.syndicationId = syndicationId;
	}
}
