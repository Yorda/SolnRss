package free.solnRss.utility;


import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntry;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.FeedException;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.SyndFeedInput;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.XmlReader;


public class FeedUtil {

	@SuppressWarnings("unchecked")
	public List<SyndEntry> lastEntries(File file) throws IOException, FeedException {
		SyndFeedInput sfi = new SyndFeedInput();
		List<SyndEntry> entries = sfi.build(new XmlReader(file)).getEntries();
		return entries;
	}

}
