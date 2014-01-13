package Util;


import ij.IJ;
import ij.ImagePlus;
import ij.io.Opener;


public class JPEGopener {

	private ImagePlus img;
	private String title;

	public JPEGopener(String file) {
		Opener opener = new Opener();
		img = opener.openImage(file);
		title=img.getTitle();
	}
	
	public ImagePlus getImage() {
		return img;
	}
	
	public String getTitle() {
		return title;
	}
}

