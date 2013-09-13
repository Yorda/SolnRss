package free.solnRss.fragment;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.res.Resources;
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
import android.widget.Toast;
import free.solnRss.R;
import free.solnRss.activity.SolnRss;
import free.solnRss.adapter.SyndicationAdapter;
import free.solnRss.fragment.listener.SyndicationsFragmentListener;
import free.solnRss.provider.PublicationsProvider;
import free.solnRss.provider.SyndicationsProvider;
import free.solnRss.repository.PublicationRepository;
import free.solnRss.repository.PublicationTable;
import free.solnRss.repository.SyndicationRepository;
import free.solnRss.repository.SyndicationTable;

/**
 * 
 * @author jftomasi
 *
 */
public class SyndicationsFragment extends AbstractFragment implements
		SyndicationsFragmentListener {
	
	private Integer selectedSyndicationID;
	private Integer activeStatus;
	private Integer isDisplayOnMainTimeLine;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle save) {
		
		selectedSyndicationID = null;
		activeStatus = null;
		View fragment = inflater.inflate( R.layout.fragment_syndications, vg, false);
		
		emptyLayoutId = R.id.emptySyndicationsLayout;	
		
		listContainer = fragment.findViewById(R.id.syndicationsListContainer);
		progressContainer = fragment.findViewById(R.id.syndicationsProgressContainer);
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
		((SolnRss) getActivity()).setSyndicationsFragmentListener(this);
		setHasOptionsMenu(true);
		setListShown(false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}
		

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,	ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		Cursor c = ((SyndicationAdapter) getListAdapter()).getCursor();
		c.moveToPosition(info.position);
		
		selectedSyndicationID = c.getInt(c.getColumnIndex("_id"));
		activeStatus =  c.getInt(c.getColumnIndex("syn_is_active"));
		isDisplayOnMainTimeLine = c.getInt(c.getColumnIndex("syn_display_on_timeline"));
		
		
		menu.setHeaderTitle(c.getString(c.getColumnIndex("syn_name")));
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.syndications_context, menu);
		
		setLabelContextMenuActivation(menu);
		setLabelContextMenuForDisplayOnMainTimeLine(menu);
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
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		
		String selection = null;
		String[] args = null;
		
		if (!TextUtils.isEmpty(getFilterText())) {
			selection = SyndicationTable.COLUMN_NAME + " like ? ";
			args = new String[1];
			args[0] = "%" + getFilterText().toString() + "%";
		}

		CursorLoader cursorLoader = new CursorLoader(getActivity(),
				SyndicationsProvider.URI,
				SyndicationsProvider.syndicationProjection, 
				selection, args, null);

		return cursorLoader;
	}
	
	@Override
	protected void queryTheTextChange() {
		getLoaderManager().restartLoader(0, null, this);
	}
	
	/*
	 * In context menu, if syndication is inactive set label to reactivate
	 * 
	 * @param menu
	 */
	private void setLabelContextMenuActivation(ContextMenu menu) {
		boolean isActive = activeStatus == 0 ? true : false;
		MenuItem itemActive = menu.getItem(1);
		if (!isActive) {
			itemActive.setTitle(getResources().getString(R.string.active_articles_btn));
		}
	}
	
	private void setLabelContextMenuForDisplayOnMainTimeLine(ContextMenu menu) {
		boolean isDisplayed = isDisplayOnMainTimeLine == 1 ? true : false;
		MenuItem itemActive = menu.getItem(2);
		if (!isDisplayed) {
			itemActive.setTitle(getResources().getString(R.string.display_articles_on_time_line));
		}
		
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_mark_syndication_as_read:
			markSyndicationPublicationsAsRead();
			break;

		case R.id.menu_active:
			changeStatus(item);
			break;

		case R.id.menu_display_on_time_line:
			changeDisplayMode(item);
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

	private void markSyndicationPublicationsAsRead() {

		OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ContentValues values = new ContentValues();
				values.put(PublicationTable.COLUMN_ALREADY_READ, "1");
				String selection = " syn_syndication_id = ? ";
				String[] args = {selectedSyndicationID.toString()};
				getActivity().getContentResolver().update(PublicationsProvider.URI, values, selection, args);
				((SolnRss) getActivity()).refreshPublications();
				Toast.makeText(getActivity(),
						getResources().getString(R.string.mark_as_read_confirm),
						Toast.LENGTH_LONG).show();
			}
		};
		
		Resources r = getResources();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(r.getString(R.string.mark_as_read_confirm))
			.setNegativeButton(r.getString(android.R.string.cancel), null)
			.setPositiveButton(r.getString(android.R.string.ok), listener)
			.create().show();
	}
	
	private void changeDisplayMode(final MenuItem item) {
		
		Uri uri = Uri.parse(SyndicationsProvider.URI + "/displayMode/" + selectedSyndicationID);
		ContentValues values = new ContentValues();
		values.put("syn_display_on_timeline",  isDisplayOnMainTimeLine == 0 ? 1: 0);
		getActivity().getContentResolver().update(uri, values, null, null);
		getLoaderManager().restartLoader(0, null, this);
		// Reload publications list
		((SolnRss) getActivity()).refreshPublications();
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
		
		AsyncTask<Integer, Void, Integer> t = new AsyncTask<Integer, Void, Integer>() {
			@Override
			protected Integer doInBackground(Integer... arg0) {
				SyndicationRepository repository = new SyndicationRepository(getActivity());
				repository.delete(selectedSyndicationID);
				return selectedSyndicationID;
			};
			@Override
			protected void onPostExecute(Integer result) {
				((SolnRss)getActivity()).reLoadPublicationsAfterSyndicationDeleted(selectedSyndicationID);
				((SolnRss)getActivity()).reLoadCategoriesAfterSyndicationDeleted();
				reloadSyndications();
				Toast.makeText(getActivity(),
						getResources().getString(R.string.delete_syndication_ok),
						Toast.LENGTH_LONG).show();
			};
		};
		
		dialogBox(getResources().getString(R.string.delete_confirm), t);
	}
	
	public void clean() {
		
		AsyncTask<Integer, Void, Integer> t = new AsyncTask<Integer, Void, Integer>() {
			@Override
			protected Integer doInBackground(Integer... arg0) {
				PublicationRepository repository = new PublicationRepository(getActivity());
				repository.clean(selectedSyndicationID);
				return selectedSyndicationID;
			};
			@Override
			protected void onPostExecute(Integer result) {
				((SolnRss)getActivity()).refreshPublications();
				Toast.makeText(getActivity(),
						getResources().getString(R.string.clean_syndication_ok),
						Toast.LENGTH_LONG).show();
			};
		};
		
		dialogBox(getResources().getString(R.string.clean_confirm), t);
	}
	
	public void dialogBox(String message,
			final AsyncTask<Integer, Void, Integer> task) {
		
		OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				task.execute(selectedSyndicationID);
			}
		};
		
		Resources r = getResources();
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(message)
				.setNegativeButton(r.getString(android.R.string.cancel), null)
				.setPositiveButton(r.getString(android.R.string.ok), listener)
				.create().show();
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

	@Override
	protected void setListPositionOnScreen() {

	}
}
