package free.solnRss.utility;


import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;

import android.os.Environment;


public class FileUtil {

	public String readFileToSring(final String path) throws FileNotFoundException, IOException {

		BufferedReader reader = null;
		String s = null;
		try {
			File f = new File(path);

			reader = new BufferedReader(new FileReader(f));
			StringBuffer buffer = new StringBuffer();

			while ((s = reader.readLine()) != null) {
				s = s.concat("\n");
				buffer.append(s);
			}
			s = new String(buffer);

		} finally {
			reader.close();
		}
		return s;
	}

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
