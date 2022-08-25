package Captcha;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.*;

import javax.imageio.ImageIO;

public class Test2 {
	
	Test2() throws IOException
	{
		String path="C:/Users/sudarshan/workspace2/Captcha";
		File newfile=new File(path,"noiseFiltered1.gif");
		BufferedImage image=ImageIO.read(newfile);
		//Image transpImg1=TransformGrayToTransparency(image);
		//BufferedImage resultImage=ImageToBufferedImage(transpImg1,image.getWidth(),image.getHeight());
		
		File outFile1= new File(path,"transparencyfile.gif");
		//ImageIO.write(resultImage,"gif",outFile1);
		
		
		
	}

}
