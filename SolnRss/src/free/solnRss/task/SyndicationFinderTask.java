package free.solnRss.task;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.widget.Toast;
import free.solnRss.R;
import free.solnRss.activity.SolnRss;
import free.solnRss.business.SyndicationBusiness;
import free.solnRss.business.impl.SyndicationBusinessImpl;
import free.solnRss.exception.ExtractFeedException;
import free.solnRss.model.Syndication;
import free.solnRss.repository.SyndicationRepository;

@Deprecated
public class SyndicationFinderTask extends AsyncTask<String, Void, String> {
	
	private SyndicationBusiness syndicationBusiness;
	private final ProgressDialog dialog;;
	private Activity context;
	private Resources resources;
	private Long newSyndicationId = null;
	

	public SyndicationFinderTask(Activity activity, Resources resources) {
		this.context = activity;
		this.resources = resources;
		dialog = new ProgressDialog(this.context);
	}

	@Override
	protected String doInBackground(String... args) {
		
		String err = null;
		
		// No record a syndication twice
		if (isStillRecorded(args[0])) {
			err = resources.getString(R.string.site_already_recorded);
			return err;
		}

		// Search syndication
		Syndication syndication = searchSyndication(args[0].trim(), err);
		
		// Work with result
		if (syndication == null || syndication.getUrl() == null) {
			err = resources.getString(R.string.feed_not_found);
		} else {
			try {
				recordNewSite(syndication);
			} catch (Exception e) {
				e.printStackTrace();
				err = resources.getString(R.string.site_record_error);
			}
		}
		return err;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
		this.dialog.setMessage(resources.getString(R.string.load_syndication));          
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface d, int which) {
						cancel(true);
						if(dialog.isShowing()){  
							dialog.dismiss();  
				        } 
						// Something was found delete what it recorded
						if (newSyndicationId != null) {
							delete();
						}
					}					
				});
       
        this.dialog.show();	
	}
	
	private void delete() {	
		SyndicationRepository repository = new SyndicationRepository(context);
		repository.delete(Integer.valueOf(newSyndicationId.toString()));
		newSyndicationId = null;
	}
	
	@Override
	protected void onPostExecute(String result) {
		
		super.onPostExecute(result);
		
		final int len = Toast.LENGTH_LONG;
		if(dialog.isShowing()){  
            dialog.dismiss();  
        }
		if (result != null) {
			warmUser(result);
		} else {
			String success = resources.getString(R.string.feed_search_ok);
			Toast.makeText(context.getApplicationContext(), success, len).show();
			// Refresh the syndication tab
			((SolnRss)context).refreshSyndications();
			// Display his publications 
			((SolnRss)context).reLoadPublicationsBySyndication(newSyndicationId.intValue());
		}
		
	}
	
	/**
	 * Open a simple dialog box for warm user
	 * @param msg
	 */
	private void warmUser(String msg) {
		String ok = resources.getString(android.R.string.ok);

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(msg);

		OnClickListener listener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		};
		builder.setPositiveButton(ok, listener);

		builder.create();
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	/**
	 * Error message retrieve with error code thrown by business
	 * @param id
	 * @return
	 */
	private String error(int id) {
		final Resources r = resources;
		String message = null;
		switch (id) {
		case 0x00000001:
			message = r.getString(R.string.bad_url);
			break;
		case 0x00000002:
			message = r.getString(R.string.http_get_error);
			break;
		case 0x00000003:
			message =r.getString(R.string.feed_search_error);
			break;
		}
		return message;
	}
	
	public boolean isStillRecorded (String url)
	{
		SyndicationRepository repository = new SyndicationRepository(context);
		return repository.isStillRecorded(url);
	}
	
	/**
	 * With the Url search the syndication in a HTML page
	 * 
	 * @param url
	 * @param err
	 * @return
	 */
	public Syndication searchSyndication(String url, String err){
		Syndication syndication = null;
		try {
			syndicationBusiness = new SyndicationBusinessImpl();
			syndication = syndicationBusiness.searchSyndication(url);
			// syndication.setWebsiteUrl(url);
		} catch (ExtractFeedException e) {
			err = error(e.getError().getId());
		}
		return syndication;
	}

	/**
	 * Syndication found so record the informations in database with the last
	 * articles published
	 * 
	 * @param syndication
	 * @throws Exception
	 */
	public void recordNewSite(Syndication syndication) throws Exception {
		SyndicationRepository repository = new SyndicationRepository(context);
		newSyndicationId = repository.addWebSite(syndication);
	}
}
