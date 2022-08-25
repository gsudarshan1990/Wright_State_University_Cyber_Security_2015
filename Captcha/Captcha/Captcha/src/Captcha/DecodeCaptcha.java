/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Captcha;

/**
 *
 * @author lingg
 */
import java.awt.Color;
import java.awt.List;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.imageio.ImageIO;



public class DecodeCaptcha {
	
	private static BufferedImage[] templates = new BufferedImage[10];
	private static boolean debug = false;
	private static boolean show = true;
	public static void main(String[] args) throws Exception {
		
		int numTests = 100;
		
		
	
		// read in the templates for each character
		for (int i=0; i<10; i++) { // for each possible character
			templates[i] = ImageIO.read(new File("template" + i+ ".gif"));
		}

		int correct = 0;
		for (int i=0; i<numTests; i++) {
			
			// generate a random string
			String rightanswer = getRandomString(6);
			
			// make a captcha from it
			BufferedImage captchaImage = generateCaptcha(rightanswer);
			
			if (show) {
				ImageIO.write(captchaImage, "jpeg", new File("see.jpg"));
				show = false;
			}
			
			// guess the captcha
			String guess = guessCaptcha(captchaImage);
			
			// see if we got it right
			if (guess.equals(rightanswer)) correct++;
		}

		
		System.out.println("accuracy: " + (correct / (double) numTests));
		
		
	
	}
	
	
	public static String guessCaptcha(BufferedImage testImage) throws Exception {
		
		String guess = "";
		
		// convert the image from a jpeg to a gif
		BufferedImage testImageGif = jpegToGif(testImage);

		// cut the CAPTCHA image into subimages that hopefully contain one
		// character each
		// note: hardcoding the number of characters is not very robust
		BufferedImage[] subImages = cutUpImage(testImageGif, 6); 
		
		for(int i=0;i<6;i++)
		{
			ImageIO.write(subImages[i], "gif", new File("see"+i+".gif"));
		}
					
		// for each of these subimages, clean it up and then try to guess
		// the character
		int k=0;
		for (BufferedImage subImage: subImages) {
			normalizeColor(subImage);
			ImageIO.write(subImage, "gif", new File("colorNormalized"+k+".gif"));
			filterNoise(subImage);
			int bestGuess = makeGuess(subImage);
			ImageIO.write(subImage, "gif", new File("noiseFiltered"+k+".gif"));
			guess += "" + bestGuess;
			k++;
		}
		
		return guess;
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
	
	
	private static void normalizeColor(BufferedImage image) {
		
		// count the number of pixels of each color in the image
		HashMap<Integer, Integer> counts = colorHistogram(image);
		//Sorting all the color values obtained due to color Histogram function
HashMap<Integer, Integer> sortcount=sortHashMap(counts);
		
		ArrayList keyvalues=new ArrayList(sortcount.keySet());
	/*	
		
		System.out.println("sortedHashMap");
		
		for(Integer i:m.keySet())
		{
			System.out.println(i+":"+m.get(i));
		}
		
	for(int i=0;i<keyvalues.size();i++)
	{
		System.out.println(keyvalues.get(i));
	}
		
	
	*/
		//Adding the 2nd highest color value as this indicates the number while the highest color indicates the Background color	
		
		ArrayList<Integer> topValues = new ArrayList<>();
		for (Integer i: sortcount.keySet()) {
			//System.out.println(m.get(i)+"::");
			if (i == keyvalues.get(keyvalues.size()-2) || sortcount.get(i)<=50) {
				//System.out.println(i+"::"+keyvalues.get(keyvalues.size()-2));
				topValues.add(i);
			
			}
		}
		

		//Undo the remove frequent value 
		// find all values greater than 50 (a non-robust hardcoded parameter)
		// remove the most frequent value from topValues, as we are hoping
		// it's the background color
	/*	
		Integer maxFreq = 0;
		Integer topColor = 0;
		for (Integer i: counts.keySet()) {
			if (counts.get(i) > maxFreq) {
				maxFreq = counts.get(i);
				topColor = i;
			}
		}
		topValues.remove(topColor);
	*/
		
		// create a new image with the most second most frequent color black
		// and all the others yellow

		int white_rgb = Color.YELLOW.getRGB();
		int black_rgb = Color.BLACK.getRGB();

		for (int x=image.getWidth()-1; x>0; x--) {
			for (int y=image.getHeight()-1; y>0; y--) {
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

				if ((matchCount / (double) totalCount) <.2) {
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
	
	
	public static int makeGuess(BufferedImage subImage) {
		// check the degree of overlap between each character template and the image
		double bestOverlap = -1;
		int bestGuess = -1;

		for (int i=0; i<templates.length; i++) { // for each possible character

			int totalCount = 0;
			int matchCount = 0;

			for (int x=subImage.getWidth()-1; x>0; x--) {
				for (int y=subImage.getHeight()-1; y>0; y--) {

					int pixelVal = subImage.getRGB(x, y);

					if (!isBlack(pixelVal)) continue;
					if (isBlack(templates[i].getRGB(x, y))) matchCount++;
					totalCount++;
				}
			}

			if (debug) 
				System.out.println(i + ": matched " + matchCount + " / " + totalCount);

			double overlap = matchCount / (double) totalCount;
			if (overlap > bestOverlap) {
				bestOverlap = overlap;
				bestGuess = i;
			}
		}

		return bestGuess;
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


	private static boolean isBlack(int value) {
		return (16777216 - Math.abs(value)) / (double) 16777216 < .05;
	}
	
	
	private static String getRandomString(int length) {
		String val = "";
		
		for (int i=0; i<length; i++) {
			double rand = Math.random();
			int randInt = (int) (rand * 10);
			val += String.valueOf(randInt);
		}
		return val;
	}
	
	
	private static BufferedImage generateCaptcha(String answer) {
        SkewImage skewImage = new SkewImage();
        return skewImage.skewImage(answer);
	}
	//Sorting all the color values
	public static LinkedHashMap sortHashMap(HashMap passedMap)
	{
		ArrayList mapKeys = new ArrayList(passedMap.keySet());
		   ArrayList mapValues = new ArrayList(passedMap.values());
		   Collections.sort(mapKeys);
		   Collections.sort(mapValues);
		   LinkedHashMap sortedMap = new LinkedHashMap();
		   Iterator valueIt=mapValues.iterator();
		   while (valueIt.hasNext()) {
		       Object val = valueIt.next();
		       Iterator keyIt = mapKeys.iterator();

		       while (keyIt.hasNext()) {
		           Object key = keyIt.next();
		           String comp1 = passedMap.get(key).toString();
		           String comp2 = val.toString();

		           if (comp1.equals(comp2)){
		               passedMap.remove(key);
		               mapKeys.remove(key);
		               sortedMap.put(key, val);
		               break;
		           }

		       }

		   }
		return sortedMap;
		
	}
}

