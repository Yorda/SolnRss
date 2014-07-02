package free.solnRss.state;

import android.content.Loader;
import android.database.Cursor;

public class PublicationsByCategoryListState extends
		AbstractPublicationListState {

	private Integer categoryId;

	@Override
	public Loader<Cursor> displayList() {
		return repository.loadPublicationsByCategory(filterText, categoryId);
	}

	@Override
	public String getActionBarTitle() {
		return repository.categoryName(categoryId);
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

}
