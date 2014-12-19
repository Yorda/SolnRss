package free.solnRss.utility;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.squareup.picasso.Picasso;

import free.solnRss.model.Publication;
import free.solnRss.model.PublicationImage;

public class ImageLoaderUtil {
	
	private final String tag = ImageLoaderUtil.class.getName();
	private Context context;
	private String imgUrl, imageName, description;
	private Bitmap bitmap;


	public ImageLoaderUtil(Context context) {
		this.context = context;
	}
	
	public String writeImageToBase64() {

		Document document = Jsoup.parse(description);
		Elements images = document.select("img");

		for (Element image : images) {
			try {
				imgUrl = image.absUrl("src");
				
				if (TextUtils.isEmpty(imgUrl)) {
					continue;
				}
				
				byte[] imageRaw = null;
				try {
					imageRaw = loadImageToByteArray().toByteArray();
					
					String image64 = Base64.encodeToString(imageRaw, Base64.DEFAULT);
					image.attr("src", "data:image/jpeg;base64," + image64);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				Log.e(tag, "Error retrieving image file with url: " + imgUrl + " -> " + e.getCause());
			}
		}
		return document.html();
	}
	
	private List<PublicationImage> publicationsImages = new ArrayList<PublicationImage>();
	
	public void saveImages(Publication publication) {
		publicationsImages.clear();

		Document document = Jsoup.parse(publication.getDescription());
		Elements images = document.select("img");
		String path = new String();
		
		for (Element image : images) {
			try {
				imgUrl = image.absUrl("src");

				if (TextUtils.isEmpty(imgUrl)) {
					continue;
				}

				imageName = Integer.valueOf(imgUrl.hashCode()).toString() + ".png";

				path = context.getExternalCacheDir() + "/" + imageName;

				// Must not already registered
				if (!isImageAlreadyDownloded(path)) {
					loadImage(path);
				}
				
				image.attr("src", "file://" + path);

				PublicationImage descriptionImage = new PublicationImage();
				descriptionImage.setUrl(imgUrl);
				descriptionImage.setPath(path);
				descriptionImage.setName(imageName);
				publicationsImages.add(descriptionImage);

			} catch (Exception e) {
				// e.printStackTrace();
				Log.e(tag, "Error retrieving image file with url: " + imgUrl + " -> " + e.getCause());
			}
		}
		publication.setDescriptionImages(publicationsImages);
		publication.setDescription(document.html());
	}

	private void loadImage(String path) throws Exception {

		bitmap = Picasso.with(context).load(imgUrl).get();
		//File file = new File(context.getFilesDir(), imageName);
		File file = new File(path);
		
		String filePath = file.toString();
		FileOutputStream fos = new FileOutputStream(filePath);

		BufferedOutputStream out = new BufferedOutputStream(fos);
		bitmap.compress(Bitmap.CompressFormat.PNG, 50, out);
		out.flush();
		out.close();

	}
	
	private boolean isImageAlreadyDownloded(String path) {		
		File file = new File(path);
		return file.exists();
	}
	
	private ByteArrayOutputStream loadImageToByteArray() throws Exception {
		bitmap = Picasso.with(context).load(imgUrl).get();
		ByteArrayOutputStream byos = new ByteArrayOutputStream();
		
		BufferedOutputStream out = new BufferedOutputStream(byos);
		bitmap.compress(Bitmap.CompressFormat.JPEG, 25, out);
		out.flush();
		out.close();
		
		return byos;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
