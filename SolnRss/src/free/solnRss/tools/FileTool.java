package free.solnRss.tools;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import android.os.Environment;

public class FileTool {

	public void saveDataToFile(String data, String path) {
		ObjectOutputStream objectOut = null;
		try {
			String sdrep = Environment.getDataDirectory().getAbsolutePath();

			// Path like sdrep + "/data/free.solnRss/XYZ.txt"
			File file = new File(sdrep + path);

			if (!file.exists())
				file.createNewFile();

			FileOutputStream stream = new FileOutputStream(file);
			objectOut = new ObjectOutputStream(new BufferedOutputStream(stream));
			objectOut.writeObject(data);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (objectOut != null)
					objectOut.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
}
