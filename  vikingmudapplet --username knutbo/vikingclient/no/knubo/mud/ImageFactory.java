package no.knubo.mud;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class ImageFactory {

	static Map images = new HashMap();
	static Set fixedImages = new HashSet();

	public static Icon getImageIcon(String imageFileName) {
		return getImageIcon(imageFileName, false);
	}

	public static Icon getImageIcon(String imageFileName, boolean tagged) {
		java.net.URL imageURL = null;

		Font smallfont = new Font("Verdana",Font.BOLD, 12);
		
		ImageIcon cached = (ImageIcon) images.get(imageFileName + tagged);

		if (cached != null
				&& cached.getImageLoadStatus() == MediaTracker.COMPLETE) {

			 if (!tagged || fixedImages.contains(imageFileName)) {
				return cached;
			}
			/*
			 * Okay, minor hack - add the T to it when it is refreshed the
			 * second time.
			 */
			fixedImages.add(imageFileName);

			BufferedImage bi = toBufferedImage(cached.getImage());

			Graphics graphics = bi.getGraphics();
			graphics.setColor(Color.BLUE);
			graphics.setFont(smallfont);
			graphics.drawString("T", bi.getWidth() - 15, bi.getHeight() - 3);

			ImageIcon im = new ImageIcon(bi);
			images.put(imageFileName + tagged, im);

			return im;

		}

		try {
			imageURL = new java.net.URL("http://www.vikingmud.org/knubo/"
					+ imageFileName);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
		ImageIcon im = new ImageIcon(imageURL);

		System.out.println("Fetching image:" + imageURL.toString());
		images.put(imageFileName + tagged, im);

		return im;
	}

	public static BufferedImage toBufferedImage(Image image) {
		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		}

		// Determine if the image has transparent pixels; for this method's
		// implementation, see e661 Determining If an Image Has Transparent
		// Pixels
		boolean hasAlpha = hasAlpha(image);

		// Create a buffered image with a format that's compatible with the
		// screen
		BufferedImage bimage = null;
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		try {
			// Determine the type of transparency of the new buffered image
			int transparency = Transparency.OPAQUE;
			if (hasAlpha) {
				transparency = Transparency.BITMASK;
			}

			// Create the buffered image
			GraphicsDevice gs = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gs.getDefaultConfiguration();
			bimage = gc.createCompatibleImage(image.getWidth(null), image
					.getHeight(null), transparency);
		} catch (HeadlessException e) {
			// The system does not have a screen
		}

		if (bimage == null) {
			// Create a buffered image using the default color model
			int type = BufferedImage.TYPE_INT_RGB;
			if (hasAlpha) {
				type = BufferedImage.TYPE_INT_ARGB;
			}
			bimage = new BufferedImage(image.getWidth(null), image
					.getHeight(null), type);
		}

		// Copy image to buffered image
		Graphics g = bimage.createGraphics();

		// Paint the image onto the buffered image
		g.drawImage(image, 0, 0, null);
		g.dispose();

		return bimage;
	}

	public static boolean hasAlpha(Image image) {
		// If buffered image, the color model is readily available
		if (image instanceof BufferedImage) {
			BufferedImage bimage = (BufferedImage) image;
			return bimage.getColorModel().hasAlpha();
		}

		// Use a pixel grabber to retrieve the image's color model;
		// grabbing a single pixel is usually sufficient
		PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Get the image's color model
		ColorModel cm = pg.getColorModel();
		return cm.hasAlpha();
	}
}
