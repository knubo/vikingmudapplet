package no.knubo.mud;

import java.awt.MediaTracker;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class ImageFactory {

	static Map images = new HashMap();

	public static Icon getImageIcon(String imageFileName) {
		java.net.URL imageURL = null;
		
		ImageIcon cached = (ImageIcon) images.get(imageFileName);

		if (cached != null && cached.getImageLoadStatus() == MediaTracker.COMPLETE) {
			return cached;
		}
		
		try {
			imageURL = new java.net.URL("http://www.vikingmud.org/knubo/"
					+ imageFileName);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
		ImageIcon im = new ImageIcon(imageURL);
		
		System.out.println("Fetching image:"+imageURL.toString());
		images.put(imageFileName, im);

		return im;
	}
}
