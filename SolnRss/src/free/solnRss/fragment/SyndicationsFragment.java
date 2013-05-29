package free.solnRss.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import free.solnRss.R;
import free.solnRss.activity.ActivityResult;
import free.solnRss.activity.SolnRss;
import free.solnRss.adapter.SyndicationAdapter;
import free.solnRss.fragment.listener.SyndicationsFragmentListener;
import free.solnRss.provider.SyndicationsProvider;
import free.solnRss.repository.PublicationRepository;
import free.solnRss.repository.SyndicationRepository;

/**
 * 
 * @author jftomasi
 *
 */
public class SyndicationsFragment extends AbstractFragment implements
		SyndicationsFragmentListener {
	
	private Integer selectedSyndicationID;
	private Integer activeStatus;
	//private SyndicationAdapter syndicationAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle save) {
		
		selectedSyndicationID = null;
		activeStatus = null;
		View fragment = inflater.inflate( R.layout.fragment_syndications, vg, false);
		
		listContainer = fragment.findViewById(R.id.syndicationsListContainer);
		progressContainer = fragment.findViewById(R.id.syndicationsProgressContainer);
		emptyLayoutId = R.id.emptySyndicationsLayout;	
		listShown = true;
		
		return fragment;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Cursor cursor = ((SyndicationAdapter) l.getAdapter()).getCursor();
		int syndicationID = cursor.getInt(cursor.getColumnIndex("_id"));
		((SolnRss) getActivity()).reLoadPublicationsBySyndication(syndicationID);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registerForContextMenu(getListView());
		getListView().setTextFilterEnabled(true);
		((SolnRss)getActivity()).setSyndicationsFragmentListener(this);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,	ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		Cursor c = ((SyndicationAdapter) getListAdapter()).getCursor();
		c.moveToPosition(info.position);
		
		selectedSyndicationID = c.getInt(c.getColumnIndex("_id"));
		activeStatus =  c.getInt(c.getColumnIndex("syn_is_active"));
		
		menu.setHeaderTitle(c.getString(c.getColumnIndex("syn_name")));
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.syndications_context, menu);
		
		setLabelContextMenuActivation(menu);
	}
	
	@Override
	protected void initAdapter() {
		final String[] from = { "syn_name", "syn_number_click" };
		final int[] to = { android.R.id.text1, android.R.id.text2 };

		simpleCursorAdapter = new SyndicationAdapter(getActivity(),
				R.layout.syndications, null, from, to, 0);
		setListAdapter((SyndicationAdapter)simpleCursorAdapter);
	}

	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		
		CursorLoader cursorLoader = new CursorLoader(getActivity(),
				SyndicationsProvider.URI,
				SyndicationsProvider.syndicationProjection, 
				null, null, null);

		return cursorLoader;
	}
	
	/*
	 * In context menu, if syndication is inactive set label to reactivate
	 * 
	 * @param menu
	 */
	private void setLabelContextMenuActivation(ContextMenu menu) {
		boolean isActive = activeStatus == 0 ? true : false;
		MenuItem itemActive = menu.getItem(0);
		if (!isActive) {
			itemActive.setTitle(getResources().getString(R.string.active_articles_btn));
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_active:
			changeStatus(item);
			break;

		case R.id.menu_clean:
			clean();
			break;

		case R.id.menu_delete:
			delete();
			break;

		default:
			break;
		}
		return super.onContextItemSelected(item);
	}	

	public void changeStatus(final MenuItem item) {

		new AsyncTask<Integer, Void, Void>() {
			@Override
			protected Void doInBackground(Integer... arg0) {
				SyndicationRepository repository = new SyndicationRepository(getActivity());
				repository.changeActiveStatus(arg0[0], activeStatus == 0 ? 1: 0);
				return null;
			};

			@Override
			protected void onPostExecute(Void result) {
				boolean newStatus = activeStatus == 0 ? false : true;
				item.setTitle(newStatus 
						? getResources().getString(R.string.unactive_articles_btn) : 
							getResources().getString(R.string.active_articles_btn));
				reloadSyndications();
			};
		}.execute(selectedSyndicationID);
	}

	public void delete() {
		
		AsyncTask<Integer, Void, ActivityResult> t = new AsyncTask<Integer, Void, ActivityResult>() {
			@Override
			protected ActivityResult doInBackground(Integer... arg0) {
				SyndicationRepository dao = new SyndicationRepository(getActivity());
				dao.delete(arg0[0]);
				return ActivityResult.DELETE;
			};
			@Override
			protected void onPostExecute(ActivityResult result) {
				reloadSyndications();
			};
		};
		
		dialogBox(getResources().getString(R.string.delete_confirm), t);
	}
	
	public void clean() {
		
		AsyncTask<Integer, Void, ActivityResult> t = new AsyncTask<Integer, Void, ActivityResult>() {
			@Override
			protected ActivityResult doInBackground(Integer... arg0) {
				PublicationRepository repository = new PublicationRepository(getActivity());
				repository.clean(selectedSyndicationID);
				return ActivityResult.CLEAN;
			};
			@Override
			protected void onPostExecute(ActivityResult result) {
				((SolnRss)getActivity()).reLoadAllPublications();
			};
		};
		
		dialogBox(getResources().getString(R.string.clean_confirm), t);
	}
	
	public void dialogBox(String message,
			final AsyncTask<Integer, Void, ActivityResult> task) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(message);

		OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		};
		
		listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				task.execute(selectedSyndicationID);
			}
		};
		
		builder.setNegativeButton(
						getResources().getString(android.R.string.cancel), listener)
				.setPositiveButton(
						getResources().getString(android.R.string.ok), listener);
		builder.create().show();
	}


	@Override
	public void loadSyndications() {
		if (getListAdapter() == null && isAdded()) {
			getLoaderManager().initLoader(0, null, this);
		}
	}

	@Override
	public void reloadSyndications() {
		if (isAdded()) {
			getLoaderManager().restartLoader(0, null, this);
		}
	}

	private String filterText;

	@Override
	public void filterSyndications(String text) {
		if (this.getListView() != null) {
			if (TextUtils.isEmpty(text)) {
				setFilterText(null);
				this.getListView().clearTextFilter();
			} else {
				setFilterText(text);
				this.getListView().setFilterText(text);
			}
		}
	}

	public String getFilterText() {
		return filterText;
	}

	public void setFilterText(String filterText) {
		this.filterText = filterText;
	}
	
}
