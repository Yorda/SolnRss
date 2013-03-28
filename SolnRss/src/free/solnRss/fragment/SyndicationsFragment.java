package free.solnRss.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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
import free.solnRss.repository.SyndicationTable;
import free.solnRss.task.SyndicationsReloaderTask;

/**
 * 
 * @author jftomasi
 *
 */
public class SyndicationsFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<Cursor>, SyndicationsFragmentListener {
	
	private SyndicationAdapter syndicationAdapter;
	
	private void provideSyndications() {
		getLoaderManager().initLoader(0, null, this);
		
		final String[] from = { 
				"syn_name", 
				"syn_number_click" 
			};
		
		final int[] to = {
				android.R.id.text1, 
				android.R.id.text2 
			};
		
		syndicationAdapter = new SyndicationAdapter(getActivity(),R.layout.syndications, null, from, to, 0);
		setListAdapter(syndicationAdapter);
	}
	
	private void clickOnSyndicationItem(ListView l, View v, int position,	long id) {
		//Cursor cursor = ((SyndicationAdapter) l.getAdapter()).getCursor();
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		final String syndicationTable = SyndicationTable.SYNDICATION_TABLE;
		String columns[] = new String[] {
				syndicationTable + "." + SyndicationTable.COLUMN_ID,
				syndicationTable + "." + SyndicationTable.COLUMN_NAME,
				syndicationTable + "." + SyndicationTable.COLUMN_URL,
				syndicationTable + "." + SyndicationTable.COLUMN_IS_ACTIVE,
				syndicationTable + "." + SyndicationTable.COLUMN_NUMBER_CLICK
			};
		
		CursorLoader cursorLoader = new CursorLoader(getActivity(),
				SyndicationsProvider.URI, columns, null, null, null);
		
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		syndicationAdapter.swapCursor(arg1);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		syndicationAdapter.swapCursor(null);
	}
	
	final private int layoutID = R.layout.fragment_syndications;
	
	private Integer selectedSyndicationID;
	private Integer activeStatus;
	
	private String ok;
	private String cancel;
	private String confirmDelete;
	private String confirmClean;
	private String activate;
	private String desactivate;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle save) {
		selectedSyndicationID = null;
		activeStatus = null;
		View fragment = inflater.inflate(layoutID, vg, false);
		initWidget(fragment);
		return fragment;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	public void initWidget(View view) {
		Resources r = getResources();
		ok      = r.getString(android.R.string.ok);
		cancel  = r.getString(android.R.string.cancel);
		confirmDelete = r.getString(R.string.delete_confirm);
		confirmClean  = r.getString(R.string.clean_confirm);
		activate    = r.getString(R.string.active_articles_btn);
		desactivate = r.getString(R.string.unactive_articles_btn);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Cursor cursor = ((SyndicationAdapter) l.getAdapter()).getCursor();
		int syndicationID = cursor.getInt(cursor.getColumnIndex("_id"));
		((SolnRss) getActivity()).reLoadPublicationsBySyndication(syndicationID);
		
		clickOnSyndicationItem(l, v, position, id);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registerForContextMenu(getListView());
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

	/**
	 * In context menu, if syndication is inactive set label to reactivate
	 * 
	 * @param menu
	 */
	void setLabelContextMenuActivation(ContextMenu menu) {
		boolean isActive = activeStatus == 0 ? true : false;
		MenuItem itemActive = menu.getItem(0);
		if (!isActive) {
			itemActive.setTitle(activate);
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
				item.setTitle(newStatus ? desactivate : activate);
				reloadSyndications(getActivity());
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
				reloadSyndications(getActivity());
			};
		};
		
		dialogBox(confirmDelete, t);
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
		
		dialogBox(confirmClean, t);
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
		
		builder
			.setNegativeButton(cancel, listener)
			.setPositiveButton(ok, listener);
		builder.create().show();
	}


	@Override
	public void loadSyndications(Context context) {
		
		if (getListAdapter() == null) {
			provideSyndications();
			//SyndicationsLoaderTask task = new SyndicationsLoaderTask(this,	(SolnRss)context);
			//task.execute();
		}
	}

	@Override
	public void reloadSyndications(Context context) {
		SyndicationsReloaderTask reloader =	new SyndicationsReloaderTask(context, this);
		reloader.execute(selectedSyndicationID);
	}
	
}
