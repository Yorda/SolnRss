package free.solnRss.utility;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.squareup.picasso.Picasso;


public class ImageLoaderUtil {

	private final String	tag	= ImageLoaderUtil.class.getName();
	private Context			context;
	private String			url, imageName, path, description;
	private Bitmap			bitmap;

	public ImageLoaderUtil(Context context) {
		this.context = context;
	}

	public String saveImage() {

		Document document = Jsoup.parse(description);

		Elements images = document.select("img");

		for (Element image : images) {
			try {
				url = image.absUrl("src");

				if (TextUtils.isEmpty(url)) {
					continue;
				}

				imageName = Integer.valueOf(url.hashCode()).toString() + ".jpg";

				path = "file://" + context.getFilesDir() + "/" + imageName;

				// Must not already registered
				if (!isImageAlreadyDownloded(path)) {
					loadImage();
				}

				image.attr("src", path);

			} catch (Exception e) {
				Log.e(tag, "Error retrieving image file with url: " + url + " -> " + e.getCause());
			}
		}
		return document.html();
	}

	private boolean isImageAlreadyDownloded(String path) {
		File file = new File(path);
		return file.exists();
	}

	private void loadImage() throws Exception {
		bitmap = Picasso.with(context).load(url).get();
		File file = new File(context.getFilesDir(), imageName);

		String filePath = file.toString();
		FileOutputStream fos = new FileOutputStream(filePath);

		BufferedOutputStream out = new BufferedOutputStream(fos);
		bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);

		out.flush();
		out.close();

	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
