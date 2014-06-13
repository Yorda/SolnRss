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
	
	private final String tag = ImageLoaderUtil.class.getName();
	private Context context;
	private String url, imageName, path, description;
	private Bitmap bitmap;


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
				
				// String fileName = url.substring( url.lastIndexOf('/')+1, url.length() );
				// String fileNameWithoutExtn = fileName.substring(0, fileName.lastIndexOf('.'));

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
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

		out.flush();
		out.close();

	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	// private File sdIconStorageDir;
		//private final String storagePath = "/soln.r/images/", cName = ImageLoaderUtil.class.getName();
		/*public void initStoragePath() {
			
			String iconsStoragePath = Environment.getExternalStorageDirectory()	+ storagePath;
			sdIconStorageDir = new File(iconsStoragePath);
			sdIconStorageDir.mkdirs();
		}*/

		/*public void testSaveImg2() {
			new AsyncTask<Void, Void, Void>(){
				@Override
				protected Void doInBackground(Void... params) {
					String t = saveImage();
					System.err.println();
					return null;
				}
				
			}.execute();
			
		}*/
	
	/*String d = "<p class=\"chapo\"><a href=\"http://www.frandroid.com/marques/motorola/216416_motorola-moto-e-se-presente-video\">Officialis� � la�la mi-mai 2014</a>, <strong>le nouveau Motorola Moto E se plie d�sormais au supplice des d�veloppeurs</strong>.<strong> Ces derni�res heures auront �t� plus que b�n�fiques pour le Moto E</strong>�qui se voit offrir�la <strong>possibilit� de d�bloquer officiellement le bootloader et d�obtenir�les�droits root gr�ce au recovery de TWRP</strong>.</p>"+
			"<p><a href=\"http://images.frandroid.com/wp-content/uploads/2014/05/android-motorola-moto-e-root-recovery-bootloader-image-01.jpg\"><img class=\"aligncenter size-medium wp-image-217540\" src=\"http://images.frandroid.com/wp-content/uploads/2014/05/android-motorola-moto-e-root-recovery-bootloader-image-01-630x222.jpg\" alt=\"android motorola moto e root recovery bootloader image 01\" width=\"630\" height=\"222\" /></a></p>"+
			"<p>Avec le Moto E, la question de la bidouille ne se pose plus ! Pour ceux qui le souhaitent, le fabricant am�ricain vient de publier une solution compl�te pour d�verrouiller le bootloader de l�appareil. <strong>Entreprendre ce type de manipulation n�est pas sans cons�quence puisque la garantie</strong>, autant sur l�aspect logiciel que mat�riel, <strong>sera invalid�e une fois�le d�blocage dudit bootloader termin�</strong>. Pour qui est-ce disponible ? Il s�agit d�<strong>un d�ploiement assez�massif ciblant principalement�le Canada</strong>, <strong>les �tats-Unis</strong>, <strong>l�Am�rique latine</strong>, <strong>l�Europe, dont notamment�le Royaume-Uni</strong>.</p>"+
			"<p>Parall�lement, <strong>TeamWin Projects officialis� la disponibilit� du�<abbr style=\"border-bottom: dotted 1px black; cursor: help;\" title=\"Le root est une autorisation qui permet de modifier les droits du syst�me d�exploitation de votre appareil afin d�en obtenir le contr�le total. De ce fait, vous pourrez installer un nouveau recovery, un nouveau kernel, une nouvelle rom ou encore faire un overclock de votre CPU/GPU, etc. En clair, c�est avoir la mainmise � la racine du syst�me pour le modeler selon ses souhaits. Attention, cette op�ration n�est pas anodine et si vous n��tes pas s�r de ce que vous faites, nous vous d�conseillons formellement�d�entreprendre de telles manipulations.\">root</abbr>�sur le Moto E</strong>. Quels avantages ? <strong>La personnalisation sans limite ou presque !</strong> <strong>La mise � disposition des droits super-user octroie � tous les utilisateurs la possibilit� de modifier plus profond�ment le syst�me</strong>�: modification du syst�me, installation d�une ROM Custom,�modification de la fr�quence du processeur, changement de l�animation de d�marrage et de quoi��pater vos amis geeks.</p>"+
			"<p>Sans plus attendre, voici les liens :</p>"+
			"<ul>"+
			"<li>le <a href=\"https://motorola-global-portal.custhelp.com/app/answers/detail/a_id/87215\">site officiel de Motorola</a>�pour le bootloader</li>"+
			"<li>le <a href=\"http://forum.xda-developers.com/showthread.php?t=2754358\">forum de XDA</a> pour le root et le recovery TWRP</li>"+
			"</ul>"+
			"<p>Bien s�r, le forum de FrAndroid, et sa�<a href=\"http://forum.frandroid.com/forum/1185-motorola-moto-e/\">section Motorola Moto E</a>, reste � votre enti�re disposition."+
			"<p class=\"source\"><a target=\"_blank\" href=\"http://www.talkandroid.com/207386-motorola-adds-super-cheap-moto-e-to-bootloader-unlock-program/\"><span>Talk Android</span></a></p>";
			*/
}
