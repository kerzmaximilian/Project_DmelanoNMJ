package Util;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class Importer {

	private File fileFly;
	private File fileImg;
	private boolean succLoad = false;
	private boolean imgT = false;
	private String imgN;

	// CONSTRUCTORS
	public Importer(File fileFly) {
		this.fileFly = fileFly;
	}

	public Importer(File fileFly, String imgName) {
		this.fileFly = fileFly;

		String iPath = fileFly.getPath();
		String[] iAPath = iPath.split("/");
		iAPath[iAPath.length - 1] = imgName;
		iPath = iAPath[0];
		for (int i = 1; i < iAPath.length; i++) {
			iPath += "/" + iAPath[i];
		}
		this.fileImg = new File(iPath);
	}

	public Importer(File fileFly, File fileImg) {
		this.fileFly = fileFly;
		this.fileImg = fileImg;
	}

	// FUNCTIONS
	public Fly load() throws IOException {

		Fly fly = new Fly();
		ArrayList<String> cStr = new ArrayList<String>();
		ArrayList<String> gvStr = new ArrayList<String>();
		ArrayList<String> gdStr = new ArrayList<String>();
		ArrayList<String> tvStr = new ArrayList<String>();

		try {

			boolean fTit = false;
			boolean fImgTit = false;
			boolean fNotes = false;
			boolean fComps = false;
			boolean fGV = false;
			boolean fGD = false;
			boolean fTV = false;

			BufferedReader br = new BufferedReader(new FileReader(fileFly));
			String line = "start";

			while (succLoad == false) {
				line = br.readLine();
				// switches
				if (line.contains(":Title"))
					fTit = true;
				if (line.contains(":Img")) {
					fTit = false;
					fImgTit = true;
				}
				if (line.contains(":Notes")) {
					fImgTit = false;
					fNotes = true;
				}
				if (line.contains(":Components")) {
					fNotes = false;
					fComps = true;
				}
				if (line.contains(":GraphV")) {
					fComps = false;
					fGV = true;
				}
				if (line.contains(":GraphD")) {
					fGV = false;
					fGD = true;
				}
				if (line.contains(":TrueV")) {
					fGD = false;
					fTV = true;
				}
				if (line.contains("END")) {
					fTV = false;
					succLoad = true;
				}

				if (fTit == true)
					fly.setTitle(line);
				if (fImgTit == true) {
					fly.setImgTitle(line);
					imgT = true;
					imgN=line;
				}
				if (fNotes == true)
					fly.setNotes(line);
				if (fComps == true) {
					cStr.add(line);
				}
				if (fGV == true) {
					gvStr.add(line);
				}
				if (fGD == true) {
					gdStr.add(line);
				}
				if (fTV == true) {
					tvStr.add(line);
				}
			}
			br.close();

		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("File was not found.");
		}

		// add image
		if (imgT == true) {

			String iPath = fileFly.getPath();
			String[] iAPath = iPath.split("/");
			iAPath[iAPath.length - 1] = imgN;
			iPath = iAPath[0];
			for (int i = 1; i < iAPath.length; i++) {
				iPath += "/" + iAPath[i];
			}
			this.fileImg = new File(iPath);
			try {
				BufferedImage image = ImageIO.read(fileImg);
				fly.setImg(image);
				// Do something with the image.
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		// add components
		if (cStr.size() > 2) {

			int[][] comps = new int[cStr.size() - 1][];

			for (int i = 1; i < cStr.size(); i++) {

				String inter = cStr.get(i);
				String[] strArr = inter.split(",");
				int[] coord = new int[strArr.length];

				for (int j = 0; j < strArr.length; j++) {
					coord[j] = Integer.parseInt(strArr[j]);
				}
				comps[i - 1] = coord;
			}
			fly.setComponents(comps);
		}

		// add graphVals
		if (gvStr.size() > 2) {

			short[][] gv = new short[gvStr.size() - 1][];

			for (int i = 1; i < gvStr.size(); i++) {

				String inter = gvStr.get(i);
				String[] strArr = inter.split(",");
				short[] pix = new short[strArr.length];

				for (int j = 0; j < strArr.length; j++) {
					pix[j] = Short.parseShort(strArr[j]);
				}
				gv[i - 1] = pix;
			}
			fly.setGraphVals(gv);
		}

		// add graphData
		if (gdStr.size() > 2) {

			short[][] gd = new short[gdStr.size() - 1][];

			for (int i = 1; i < gdStr.size(); i++) {

				String inter = gdStr.get(i);
				String[] strArr = inter.split(",");
				short[] pix = new short[strArr.length];

				for (int j = 0; j < strArr.length; j++) {
					pix[j] = Short.parseShort(strArr[j]);
				}
				gd[i - 1] = pix;
			}
			fly.setGraphData(gd);
		}

		// add graphData
		if (tvStr.size() > 2) {

			short[][] tv = new short[tvStr.size() - 1][];

			for (int i = 1; i < tvStr.size(); i++) {

				String inter = tvStr.get(i);
				String[] strArr = inter.split(",");
				short[] pix = new short[strArr.length];

				for (int j = 0; j < strArr.length; j++) {
					pix[j] = Short.parseShort(strArr[j]);
				}
				tv[i - 1] = pix;
			}
			fly.setTrueVals(tv);
		}

		// fly.getSummary();
		System.out.println("IMPORTER: " + fly.getTitle() + " loaded.");
		if (succLoad == false)
			System.out.println("IMPORTER: Warning. File may be corrupted.");

		return fly;

	}

	// GETTERS
	public boolean getStatus() {
		return succLoad;
	}
}
