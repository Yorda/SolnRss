package free.solnRss.state;

import free.solnRss.repository.PublicationRepository;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;

public class BySyndicationPublicationListState implements PublicationsListState {

	private Context context;
	private String filterText;
	private Integer syndicationId;
	private PublicationRepository repository;
	private boolean displayAlreadyRead;

	public void init(Context context) {
		this.context = context;
		repository = new PublicationRepository(this.context);
	}
	
	@Override
	public Loader<Cursor> displayList() {
		return repository.loadPublications(filterText, syndicationId, null,
				null, displayAlreadyRead);
	}

	@Override
	public String getActionBarTitle() {
		return null;
	}

	@Override
	public void setFilterText(String filterText) {
		this.filterText = filterText;
	}

	@Override
	public String getFilterText() {
		return filterText;
	}
}
