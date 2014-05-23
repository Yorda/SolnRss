package free.solnRss.utility;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.squareup.picasso.Picasso;

public class ImageLoaderUtil {

	private Context context;
	private String uri;
	private String imageName;
	private final String storagePath = "/soln.r/images/";

	ImageLoaderUtil(Context context) {
		this.context = context;
	}
	
	public void saveImage() {
		// Find url
		// Load remote image to local disk
		loadImage();
		// Write the new path in text.
	}
	
	private void loadImage() {
		try {
			uri ="http://square.github.io/picasso/static/sample.png";
			imageName = "/test.jpg";
			
			Bitmap bitmap = Picasso.with(context).load(uri).get();

			// get path to external storage (SD card)
			String iconsStoragePath = Environment.getExternalStorageDirectory()	+ storagePath;
			
			File sdIconStorageDir = new File(iconsStoragePath);
			// create storage directories, if they don't exist
			sdIconStorageDir.mkdirs();

			try {
				String filePath = sdIconStorageDir.toString() + imageName;
				FileOutputStream fos = new FileOutputStream(filePath);

				BufferedOutputStream out = new BufferedOutputStream(fos);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

				out.flush();
				out.close();

			} catch (FileNotFoundException e) {
				Log.e("TAG", "Error saving image file: " + e.getMessage());
			} catch (IOException e) {
				Log.e("TAG", "Error saving image file: " + e.getMessage());
			}
		} catch (Exception e) {
			Log.e("TAG", "Error saving image file: " + e.getMessage());
		}
	}
}
