package free.solnRss.utility;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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

public class ImageLoaderUtil {
	
	private final String tag = ImageLoaderUtil.class.getName();
	private Context context;
	private String imgUrl, imageName, path, description;
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
					/*
					URL url = new URL(imgUrl);
					HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

					InputStream in = new BufferedInputStream(urlConnection.getInputStream());
					ByteArrayOutputStream out = new ByteArrayOutputStream();

					int c;
					while ((c = in.read()) != -1) {
						out.write(c);
					}
					out.flush();

					imageRaw = out.toByteArray();
					
					urlConnection.disconnect();
					in.close();
					out.close();
					*/
					
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
	
	public String saveImage() {

		Document document = Jsoup.parse(description);

		Elements images = document.select("img");

		for (Element image : images) {
			try {
				imgUrl = image.absUrl("src");
				
				if (TextUtils.isEmpty(imgUrl)) {
					continue;
				}
				
				imageName = Integer.valueOf(imgUrl.hashCode()).toString() + ".jpg";
				
				path = "file://" + context.getFilesDir() + "/" + imageName;

				// Must not already registered
				if (!isImageAlreadyDownloded(path)) {
					loadImage();
				}

				image.attr("src", path);

			} catch (Exception e) {
				Log.e(tag, "Error retrieving image file with url: " + imgUrl + " -> " + e.getCause());
			}
		}
		return document.html();
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
	
	private void loadImage() throws Exception {
		bitmap = Picasso.with(context).load(imgUrl).get();
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
