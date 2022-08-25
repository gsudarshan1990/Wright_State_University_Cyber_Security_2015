package Captcha;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.imageio.ImageIO;


public class Test {
	private static boolean debug = false;

	public static void main (String args[]) throws IOException
	{
	
	        SkewImage skewImage = new SkewImage();
	        BufferedImage captchaImage=skewImage.skewImage("234393");
	        System.out.println(captchaImage);
	        ImageIO.write(captchaImage, "jpeg", new File("see.jpg"));
	        //System.out.println(guessCaptcha(captchaImage));
	        BufferedImage testImageGif = jpegToGif(captchaImage);
	        BufferedImage[] subImages = cutUpImage(testImageGif, 6); 
	        for(int i=0;i<subImages.length;i++)
	        {
	        	//System.out.println(subImages[i]);
	        	ImageIO.write(subImages[i], "jpeg", new File("see"+i+".jpg"));
	        	
	        }
	        normalizeColor(subImages[3]);
	        //filterNoise(subImages[2]);
					
		}
private static void filterNoise(BufferedImage image) {
		
		// try to clean up the image by removing stray marks
		for (int x=0; x<image.getWidth(); x++) {
			for (int y=0; y<image.getHeight(); y++) {
				
				int pixelVal = image.getRGB(x, y);
					
				// check how many pixels in a 2 x 2 rectangle with this point 
				// in the center have the same color as this point; if not 
				// many, flip this pixel's color
				int startX = Math.max(x-2, 0);
				int startY = Math.max(y-2, 0);
				int endX = Math.min(x+2, image.getWidth()-1);
				int endY = Math.min(y+2, image.getHeight()-1);

				int matchCount = 0;
				int totalCount = 0;
				for (int i=startX; i<=endX; i++) {
					for (int j=startY; j<=endY; j++) {
						if (image.getRGB(i,j) == pixelVal) {
							matchCount++;
						}
						totalCount++;
					}
				}

				if ((matchCount / (double) totalCount) < .2) {
					if (pixelVal == Color.YELLOW.getRGB()) {
						image.setRGB(x, y, Color.BLACK.getRGB());
					} else {
						image.setRGB(x, y, Color.YELLOW.getRGB());
					}
				}
			}
		}
		
		if (debug) {
			try {
				ImageIO.write(image, "gif", new File("noiseFiltered.gif"));
			} catch (Exception e) {e.printStackTrace();}
		}
}
private static void normalizeColor(BufferedImage image) {
		
		// count the number of pixels of each color in the image
		HashMap<Integer, Integer> counts = colorHistogram(image);
		
	for(Integer i:counts.keySet())
	{
		System.out.println(counts.get(i));
	}
		

		// find all values greater than 50 (a non-robust hardcoded parameter)
		ArrayList<Integer> topValues = new ArrayList<>();
		for (Integer i: counts.keySet()) {
			if (counts.get(i) >= 50) {
				topValues.add(i);
				
			}
		}

		// remove the most frequent value from topValues, as we are hoping
		// it's the background color
		Integer maxFreq = 0;
		Integer topColor = 0;
		for (Integer i: counts.keySet()) {
			if (counts.get(i) > maxFreq) {
				maxFreq = counts.get(i);
				topColor = i;
				
				
			}
		}
		topValues.remove(topColor);
		
		// create a new image with the most second most frequent color black
		// and all the others yellow

		int white_rgb = Color.YELLOW.getRGB();
		int black_rgb = Color.BLACK.getRGB();

		for (int x=0; x<image.getWidth(); x++) {
			for (int y=0; y<image.getHeight(); y++) {
				int pixelVal = image.getRGB(x, y);

				if (!topValues.contains(pixelVal)) {
					image.setRGB(x, y, white_rgb);
				} else {
					image.setRGB(x, y, black_rgb);
				}
			}
		}
		
		if (debug) {
			try {
				ImageIO.write(image, "gif", new File("colorNormalized.gif"));
			} catch (Exception e) {e.printStackTrace();}
		}
	}
	
private static HashMap<Integer, Integer> colorHistogram(BufferedImage image) {
	HashMap<Integer, Integer> counts = new HashMap<>();

	for (int x=0; x<image.getWidth(); x++) {
		for (int y=0; y<image.getHeight(); y++) {
			int pixelVal = image.getRGB(x, y);

			if (!counts.containsKey(pixelVal)) {
				counts.put(pixelVal, 1);
			} else {
				counts.put(pixelVal, counts.get(pixelVal)+1);
			}
		}
	}

	if (debug) {
		for (Integer i: counts.keySet()) {
			System.out.println(i + ": " + counts.get(i));
		}
	}

	return counts;
}




private static BufferedImage jpegToGif(BufferedImage image) {
		
		// this is pretty kludgy... we're just going to write the jpeg
		// out as a gif and read it back in
		
		BufferedImage gifImage = image;
		
		try {
			ImageIO.write(image, "gif", new File("temp.gif"));
			gifImage = ImageIO.read(new File("temp.gif"));
		} catch (Exception e) {e.printStackTrace();}
		
		return gifImage;
	}
private static BufferedImage[] cutUpImage(BufferedImage image, int numSlices) {
	BufferedImage[] subimages = new BufferedImage[numSlices];

	int origHeight = image.getHeight();
	int origWidth = image.getWidth();
	int width = origWidth / numSlices;

	for (int i=0; i<numSlices; i++) {
		subimages[i] = image.getSubimage(i*width, 0, width, origHeight);
	}

	return subimages;
}

}
