package free.solnRss.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FileUtil {

	public static String readFileToSring(final String path)	
			throws FileNotFoundException, IOException {

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

}
