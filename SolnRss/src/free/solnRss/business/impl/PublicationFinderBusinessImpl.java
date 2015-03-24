package free.solnRss.business.impl;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.SparseArray;

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndContent;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntry;

import free.solnRss.business.PublicationFinderBusiness;
import free.solnRss.business.SyndicationBusiness;
import free.solnRss.exception.ExtractFeedException;
import free.solnRss.manager.UpdatingProcessConnectionManager;
import free.solnRss.notification.NewPublicationsNotification;
import free.solnRss.provider.SolnRssProvider;
import free.solnRss.repository.PublicationContentTable;
import free.solnRss.repository.PublicationTable;
import free.solnRss.repository.RssRepository;
import free.solnRss.repository.RssTable;
import free.solnRss.repository.SyndicationRepository;
import free.solnRss.repository.SyndicationTable;
import free.solnRss.utility.StringUtil;


public class PublicationFinderBusinessImpl implements PublicationFinderBusiness {

	private final DateFormat					sdf					= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRENCH);
	private SyndicationBusiness					syndicationBusiness	= new SyndicationBusinessImpl();

	private SyndicationRepository				syndicationRepository;
	private RssRepository						rssRepository;

	private SharedPreferences					sharedPreferences;
	private SparseArray<String>					syndications		= new SparseArray<String>();

	private List<SyndEntry>						syndEntries			= new ArrayList<SyndEntry>();
	private ArrayList<ContentProviderOperation>	operations			= new ArrayList<ContentProviderOperation>();

	private Context								context;
	private Integer								newPublicationsRecorded;

	//private ImageLoaderUtil imageLoader;

	NewPublicationsNotification					newPublicationsNotification;

	public PublicationFinderBusinessImpl(final Context context) {
		this.context = context;
		init(context);
	}

	private void init(final Context context) {
		syndicationRepository = new SyndicationRepository(context);
		rssRepository = new RssRepository(context);
		newPublicationsNotification = new NewPublicationsNotification(context);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		//imageLoader = new ImageLoaderUtil(context);
	}

	@Override
	public void searchNewPublications() {
		retrieveSyndicationsToRefresh();
		retrieveNewPublications();
		updateLastRefreshTime();
	}

	@Override
	public void searchNewPublications(final SparseArray<String> syndications) {
		this.syndications = syndications;
		retrieveNewPublications();
	}

	private void retrieveNewPublications() {
		// For test
		//StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
		//.permitAll().build();StrictMode.setThreadPolicy(policy);

		newPublicationsRecorded = 0;

		final String updateDate = sdf.format(new Date());

		Integer syndicationId = -1;
		String url = null;

		operations.clear();
		syndEntries.clear();

		for (int i = 0, nsize = syndications.size(); i < nsize; i++) {

			syndicationId = syndications.keyAt(i);
			url = syndications.get(syndicationId);

			try {
				if (UpdatingProcessConnectionManager.canUseConnection(context)) {

					syndEntries = syndicationBusiness.newRssPublished(url);

					addNewPublicationIfNotAlreadyRegistered(syndicationId, updateDate);

					updateRegisteredRss(syndicationId);

					updateRefreshTime(syndicationId, updateDate);

				}

			} catch (final ExtractFeedException fe) {
				// Set this syndication in error
				setSyndicationInError(syndicationId, fe.getError().getId(), updateDate);
			}
		}

		registerNewPublications(updateDate);
	}

	private void setSyndicationInError(final Integer syndicationId, final int errorCode, final String updateDateFormat) {
		operations.add(ContentProviderOperation.newUpdate(Uri.parse(SolnRssProvider.URI + "/syndication"))
				.withValue(SyndicationTable.COLUMN_LAST_EXTRACT_TIME, updateDateFormat)
				.withValue(SyndicationTable.COLUMN_LAST_RSS_SEARCH_RESULT, errorCode)
				.withSelection(SyndicationTable.COLUMN_ID + " = ? ", new String[] { Integer.valueOf(syndicationId).toString() })
				.withYieldAllowed(true).build());
	}

	private void addNewPublicationIfNotAlreadyRegistered(final Integer syndicationId, final String updateDateFormat) {

		for (final SyndEntry syndEntry : syndEntries) {

			if (!rssRepository.isAlreadyRetrieved(syndEntry.getTitle(), syndEntry.getLink())) {
				operations.add(ContentProviderOperation.newInsert(Uri.parse(SolnRssProvider.URI + "/publication"))
						.withValues(addNewPublication(syndicationId, syndEntry, updateDateFormat)).withYieldAllowed(true).build());

				operations.add(ContentProviderOperation.newInsert(Uri.parse(SolnRssProvider.URI + "/publicationContent"))
						.withValue("syndicationId", syndicationId.toString()).withValue(PublicationContentTable.COLUMN_LINK, syndEntry.getLink())

						.withValue(PublicationContentTable.COLUMN_PUBLICATION, inmproveDescription(getDescription(syndEntry)))
						// .withValue(PublicationContentTable.COLUMN_PUBLICATION,WebSiteUtil.htmlToReadableText(syndEntry.getLink()))

						.withValueBackReference(PublicationContentTable.COLUMN_PUBLICATION_ID, operations.size() - 1).withYieldAllowed(true).build());

				newPublicationsRecorded++;
			}
		}
	}

	private void updateRegisteredRss(final Integer syndicationId) {
		operations.add(ContentProviderOperation.newDelete(Uri.parse(SolnRssProvider.URI + "/rss"))
				.withSelection("syn_syndication_id = ?", new String[] { syndicationId.toString() }).withYieldAllowed(true).build());

		for (final SyndEntry syndEntry : syndEntries) {

			operations.add(ContentProviderOperation.newInsert(Uri.parse(SolnRssProvider.URI + "/rss"))
					.withValue(RssTable.COLUMN_URL, syndEntry.getLink()).withValue(RssTable.COLUMN_TITLE, syndEntry.getTitle())
					.withValue(RssTable.COLUMN_SYNDICATION_ID, syndicationId).withYieldAllowed(true).build());
		}
	}

	private void registerNewPublications(final String updateDate) {
		if (newPublicationsRecorded > 0) {
			// Run the batch
			try {
				context.getContentResolver().applyBatch(SolnRssProvider.AUTHORITY, operations);
				// Notify
				if (mustDisplayNotification()) {
					newPublicationsNotification.notificationForNewPublications(newPublicationsRecorded, updateDate);
				}
			} catch (final OperationApplicationException e) {

			} catch (final RemoteException r) {

			}
		}
	}

	private void updateRefreshTime(final Integer syndicationId, final String updateDateFormat) {
		// Update last refreshTime for syndication
		operations.add(ContentProviderOperation.newUpdate(Uri.parse(SolnRssProvider.URI + "/syndication"))
				.withValue(SyndicationTable.COLUMN_LAST_EXTRACT_TIME, updateDateFormat)
				.withValue(SyndicationTable.COLUMN_LAST_RSS_SEARCH_RESULT, null)
				.withSelection(SyndicationTable.COLUMN_ID + " = ? ", new String[] { Integer.valueOf(syndicationId).toString() })
				.withYieldAllowed(true).build());
	}

	private ContentValues addNewPublication(final Integer syndicationId, final SyndEntry syndEntry, final String date) {

		final ContentValues values = new ContentValues();
		values.put(PublicationTable.COLUMN_SYNDICATION_ID, syndicationId);
		values.put(PublicationTable.COLUMN_TITLE, StringUtil.unescapeHTML(syndEntry.getTitle()));
		values.put(PublicationTable.COLUMN_ALREADY_READ, 0);
		values.put(PublicationTable.COLUMN_PUBLICATION_DATE, date);

		return values;
	}

	private String getDescription(final SyndEntry syndEntry) {
		String description = null;

		if (syndEntry.getDescription() != null) {
			description = syndEntry.getDescription().getValue();
		}

		if (syndEntry.getContents() != null && syndEntry.getContents().size() > 0) {
			description = ((SyndContent) syndEntry.getContents().get(0)).getValue();
		}

		if (description == null) {
			return new String();
		}
		return description;
	}

	protected void retrieveSyndicationsToRefresh() {
		syndications.clear();
		final Cursor cursor = syndicationRepository.findSyndicationsToRefresh(timeToRefresh());
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			do {
				syndications.put(cursor.getInt(0), cursor.getString(1));
			} while (cursor.moveToNext());
			cursor.close();
		}
	}

	private Date timeToRefresh() {
		final int refresh = sharedPreferences.getInt("pref_search_publication_time", 15);
		final GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(new Date());
		calendar.add(Calendar.MINUTE, -refresh);
		return calendar.getTime();
	}

	private void updateLastRefreshTime() {
		final SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putLong("publicationsLastRefresh", Calendar.getInstance().getTimeInMillis());
		editor.commit();
	}

	private String inmproveDescription(final String description) {
		String fixedDescription = description;

		if (!TextUtils.isEmpty(description)) {
			fixedDescription = StringUtil.unescapeHTML(fixedDescription);
			fixedDescription = fixYouTubeLinkInIFrame(fixedDescription);
		}

		// get image
		//imageLoader.setDescription(fixedDescription);
		//fixedDescription = imageLoader.saveImage();

		return fixedDescription;
	}

	private String fixYouTubeLinkInIFrame(final String description) {
		return description.replaceAll("src=\"//www.youtube.com/embed", "src=\"http://www.youtube.com/embed");
	}

	public boolean mustDisplayNotification() {
		return sharedPreferences.getBoolean("pref_display_notify", true);
	}

	@Override
	public int getNewPublicationsRecorded() {
		return newPublicationsRecorded == null ? 0 : newPublicationsRecorded;
	}
}
