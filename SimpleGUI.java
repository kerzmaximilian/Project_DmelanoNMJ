package Util;
import java.awt.Color;

import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class SimpleGUI {

	private ImagePlus imgMax;
	private ImagePlus imgCopy;
	private ByteProcessor imgIP;
	private int LOWRESOLUTION;
	private int HLRATIO;
	private int pixWH;

	private Color co = new Color(250, 200, 7);

	public SimpleGUI(ImagePlus img, int LOWRESOLUTION, int HLRATIO) {
		this.imgMax = img;
		this.imgCopy = img;
		this.imgIP = (ByteProcessor) imgMax.getProcessor();
		this.imgIP.setColor(co);
		this.LOWRESOLUTION = LOWRESOLUTION;
		this.HLRATIO = HLRATIO;
		this.pixWH = this.HLRATIO * this.LOWRESOLUTION;

		System.out.println("MaxIntentsity Image: \nPixelcount: " + pixWH
				* pixWH + "\nH x W: " + pixWH + "x" + pixWH);
	}

	public void viewRatio() {
		// vertical

		for (int i = 0; i < pixWH / HLRATIO; i++) {

			imgIP.drawLine(HLRATIO * i, 0, HLRATIO * i, pixWH);
		}
		// horizontal
		for (int i = 0; i < pixWH / HLRATIO; i++) {
			imgIP.drawLine(0, HLRATIO * i, pixWH, HLRATIO * i);
		}
		imgMax.show();
	}

	public void viewMap() {
		
		for (int i = 0; i < DmelanoNMJ.interMap.length; i++) {
			if (DmelanoNMJ.interMap[i] == 'O') {
				imgIP.drawRect((i / LOWRESOLUTION) * HLRATIO,
						(i % LOWRESOLUTION) * HLRATIO, HLRATIO, HLRATIO);
			}
		}
		imgMax.show();
	}

	public void viewComponents() {

		if (DmelanoNMJ.components.length > 0) {

			for (int i = 0; i < DmelanoNMJ.components.length; i++) {
				for (int j = 0; j < DmelanoNMJ.components[i].length / 2; j++) {
					imgIP.drawRect(DmelanoNMJ.components[i][j * 2 + 1]
							* HLRATIO, DmelanoNMJ.components[i][j * 2]
							* HLRATIO, HLRATIO, HLRATIO);
				}
			}
			imgMax.show();
		} else {
			System.err.println("No connected components found.");
		}
	}
	
}
