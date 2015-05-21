package free.solnRss.fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import free.solnRss.R;
import free.solnRss.activity.SolnRss;
import free.solnRss.adapter.SyndicationAdapter;
import free.solnRss.dialog.OneEditTextDialogBox;
import free.solnRss.fragment.listener.SyndicationsFragmentListener;
import free.solnRss.repository.SyndicationRepository;


/**
 * 
 * @author jftomasi
 *
 */
public class SyndicationsFragment extends AbstractFragment implements SyndicationsFragmentListener,
		SharedPreferences.OnSharedPreferenceChangeListener {

	private SyndicationRepository	syndicationRepository;
	private Integer					selectedSyndicationID;
	private Integer					activeStatus;
	private Integer					isDisplayOnMainTimeLine;

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup vg, final Bundle save) {

		selectedSyndicationID = null;
		activeStatus = null;
		final View fragment = inflater.inflate(R.layout.fragment_syndications, vg, false);

		emptyLayoutId = R.id.emptySyndicationsLayout;

		listContainer = fragment.findViewById(R.id.syndicationsListContainer);
		progressContainer = fragment.findViewById(R.id.syndicationsProgressContainer);
		listShown = true;

		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);

		return fragment;
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {

		final Cursor cursor = ((SyndicationAdapter) l.getAdapter()).getCursor();
		final int syndicationId = cursor.getInt(cursor.getColumnIndex("_id"));

		((SolnRss) getActivity()).reLoadPublicationsBySyndication(syndicationId);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		syndicationRepository = new SyndicationRepository(getActivity());
		registerForContextMenu(getListView());
		((SolnRss) getActivity()).setSyndicationsFragmentListener(this);
		setHasOptionsMenu(true);
		setListShown(false);
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		final Cursor c = ((SyndicationAdapter) getListAdapter()).getCursor();
		c.moveToPosition(info.position);

		selectedSyndicationID = c.getInt(c.getColumnIndex("_id"));
		activeStatus = c.getInt(c.getColumnIndex("syn_is_active"));
		isDisplayOnMainTimeLine = c.getInt(c.getColumnIndex("syn_display_on_timeline"));

		menu.setHeaderTitle(c.getString(c.getColumnIndex("syn_name")));
		final MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.syndications_context, menu);

		setLabelContextMenuActivation(menu);
		setLabelContextMenuForDisplayOnMainTimeLine(menu);
	}

	@Override
	protected void initAdapter() {
		final String[] from = { "syn_name", "syn_number_click" };
		final int[] to = { android.R.id.text1, android.R.id.text2 };

		simpleCursorAdapter = new SyndicationAdapter(getActivity(), R.layout.syndications, null, from, to, 0);
		setListAdapter(simpleCursorAdapter);
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle bundle) {
		return syndicationRepository.loadSyndications(getFilterText());
	}

	@Override
	protected void queryTheTextChange() {
		getLoaderManager().restartLoader(0, null, this);
	}

	/*
	 * In context menu, if syndication is inactive set label to reactivate
	 * @param menu
	 */
	private void setLabelContextMenuActivation(final ContextMenu menu) {
		final boolean isActive = activeStatus == 0 ? true : false;
		final MenuItem itemActive = menu.getItem(1);
		if (!isActive) {
			itemActive.setTitle(getResources().getString(R.string.active_articles_btn));
		}
	}

	private void setLabelContextMenuForDisplayOnMainTimeLine(final ContextMenu menu) {
		final boolean isDisplayed = isDisplayOnMainTimeLine == 1 ? true : false;
		final MenuItem itemActive = menu.getItem(2);
		if (!isDisplayed) {
			itemActive.setTitle(getResources().getString(R.string.display_articles_on_time_line));
		}

	}

	@Override
	public boolean onContextItemSelected(final MenuItem item) {
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

			case R.id.menu_rename:
				rename();
				break;

			default:
				break;
		}
		return super.onContextItemSelected(item);
	}

	private void rename() {
		OneEditTextDialogBox oneEditTextDialogBox;
		oneEditTextDialogBox = new OneEditTextDialogBox(getActivity(), syndicationName(selectedSyndicationID), getResources().getString(
				R.string.input_new_name), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				final EditText e = (EditText) ((AlertDialog) dialog).findViewById(R.id.one_edit_text_dialog);
				renameSyndication(e.getText().toString());
			}
		});
		oneEditTextDialogBox.displayDialogBox();
	}

	private void renameSyndication(final String newName) {
		syndicationRepository.renameSyndication(selectedSyndicationID, newName);
		((SolnRss) getActivity()).refreshPublications();
	}

	private void markSyndicationPublicationsAsRead() {

		final OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				((SolnRss) getActivity()).getPublicationsFragmentListener().markSyndicationPublicationsAsRead(selectedSyndicationID);
			}
		};

		final Resources r = getResources();

		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setMessage(r.getString(R.string.confirm_mark_as_read, syndicationName(selectedSyndicationID)))
				.setNegativeButton(r.getString(android.R.string.cancel), null).setPositiveButton(r.getString(android.R.string.ok), listener).create()
				.show();
	}

	private void changeDisplayMode(final MenuItem item) {

		syndicationRepository.changeSyndicationDisplayMode(selectedSyndicationID, isDisplayOnMainTimeLine == 0 ? 1 : 0);
		// Reload publications list
		((SolnRss) getActivity()).refreshPublications();

		String msg = getResources().getString(R.string.display_syndication);
		if (isDisplayOnMainTimeLine != 0) {
			msg = getResources().getString(R.string.undisplay_syndication);
		}
		Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
	}

	public void changeStatus(final MenuItem item) {

		syndicationRepository.changeSyndicationActivityStatus(selectedSyndicationID, activeStatus == 0 ? 1 : 0);

		String title = getResources().getString(R.string.active_articles_btn);
		String msg = getResources().getString(R.string.unactive_syndication);

		if (activeStatus != 0) {
			title = getResources().getString(R.string.unactive_articles_btn);
			msg = getResources().getString(R.string.active_syndication);
		}

		item.setTitle(title);
		Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();

	}

	public void delete() {

		final AsyncTask<Integer, Void, Integer> t = new AsyncTask<Integer, Void, Integer>() {
			@Override
			protected Integer doInBackground(final Integer... arg0) {
				syndicationRepository.delete(selectedSyndicationID);
				return selectedSyndicationID;
			};

			@Override
			protected void onPostExecute(final Integer result) {
				((SolnRss) getActivity()).reLoadPublicationsAfterSyndicationDeleted(selectedSyndicationID);
				((SolnRss) getActivity()).reLoadCategoriesAfterSyndicationDeleted();
				reloadSyndications();
				Toast.makeText(getActivity(), getResources().getString(R.string.delete_syndication_ok), Toast.LENGTH_LONG).show();
			};
		};

		dialogBox(getResources().getString(R.string.delete_confirm), t);
	}

	public void clean() {

		final AsyncTask<Integer, Void, Integer> t = new AsyncTask<Integer, Void, Integer>() {
			@Override
			protected Integer doInBackground(final Integer... arg0) {
				((SolnRss) getActivity()).getPublicationsFragmentListener().deletePublications(selectedSyndicationID);
				return selectedSyndicationID;
			};

			@Override
			protected void onPostExecute(final Integer result) {
				// ((SolnRss)getActivity()).refreshPublications();
				final String ok = getResources().getString(R.string.clean_syndication_ok);
				Toast.makeText(getActivity(), ok, Toast.LENGTH_LONG).show();
			};
		};

		dialogBox(getResources().getString(R.string.clean_confirm), t);
	}

	public void dialogBox(final String message, final AsyncTask<Integer, Void, Integer> task) {

		final OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				task.execute(selectedSyndicationID);
			}
		};

		final Resources r = getResources();
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(message).setNegativeButton(r.getString(android.R.string.cancel), null)
				.setPositiveButton(r.getString(android.R.string.ok), listener).create().show();
	}

	@Override
	public void addOneReadToSyndication(final Integer syndicationId, final Integer numberOfClick) {
		syndicationRepository.addOneReadToSyndication(syndicationId, numberOfClick);
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

	@Override
	public void moveListViewToTop() {
		getListView().setSelection(0);
	}

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
		if (key.compareTo("pref_sort_syndications") == 0 || key.compareTo("pref_user_font_face") == 0 || key.compareTo("pref_user_font_size") == 0) {
			reloadSyndications();
		}
	}
}
