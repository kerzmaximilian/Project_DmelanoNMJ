package Util;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class Exporter {

	private File file;
	private boolean graphValsBoo = false;
	private boolean trueValsBoo = false;
	private float percent=0;
	public Exporter(File file) {

		this.file = file;
	}

	public void xml(Fly fly) throws JAXBException {

		// XML export
		JAXBContext context = JAXBContext.newInstance(Fly.class);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		m.marshal(fly, file);
		System.out
				.println("EXPORTER: " + fly.getTitle() + " exported as .xml.");
	}

	public void save(Fly fly) throws IOException {
		if (file.getPath().contains(".fly")) {

			int[][] comps = fly.getComponents();
			short[][] gV=new short[0][];
			if (graphValsBoo == true)
				gV = fly.getGraphVals();
			short[][] gD = fly.getGraphData();
			short[][] tV= new short[0][];
			if (trueValsBoo == true)
				tV = fly.getTrueVals();
			float multiplier = (float)(100f/(gV.length+gD.length+tV.length+4f));

			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write("FLAG:Title");
			percent+=multiplier;
			bw.newLine();
			bw.write(fly.getTitle());
			bw.newLine();
			bw.write("FLAG:Img");
			percent+=multiplier;
			bw.newLine();
			if (fly.getImg() != null) {
				bw.write(fly.getImgTitle());
				bw.newLine();
			}
			bw.write("FLAG:Notes");
			percent+=multiplier;
			bw.newLine();
			if (fly.getNotes() != null) {
				bw.write(fly.getNotes());
				bw.newLine();
			}
			bw.write("FLAG:Components");
			percent+=multiplier;
			bw.newLine();
			for (int i = 0; i < comps.length; i++) {
				String line = Integer.toString(comps[i][0]);
				for (int j = 1; j < comps[i].length; j++) {
					line += "," + Integer.toString(comps[i][j]);
				}
				bw.write(line);
				bw.newLine();
			}
			bw.write("FLAG:GraphVals");
			bw.newLine();
			if (graphValsBoo == true) {
				for (int i = 0; i < gV.length; i++) {
					String line = Integer.toString(gV[i][0]);
					for (int j = 1; j < gV[i].length; j++) {
						line += "," + Integer.toString(gV[i][j]);
					}
					bw.write(line);
					bw.newLine();
					percent+=multiplier;
				}
			}
			bw.write("FLAG:GraphData");
			bw.newLine();
			for (int i = 0; i < gD.length; i++) {
				String line = Integer.toString(gD[i][0]);
				for (int j = 1; j < gD[i].length; j++) {
					line += "," + Integer.toString(gD[i][j]);
				}
				bw.write(line);
				bw.newLine();
				percent+=multiplier;
			}
			bw.write("FLAG:TrueVals");
			bw.newLine();
			if (trueValsBoo == true) {
				for (int i = 0; i < tV.length; i++) {
					String line = Integer.toString(tV[i][0]);
					for (int j = 1; j < tV[i].length; j++) {
						line += "," + Integer.toString(tV[i][j]);
					}
					bw.write(line);
					bw.newLine();
					percent+=multiplier;
				}
			}
			bw.write("FLAG:END");
			bw.flush();
			bw.close();
			System.out.println("EXPORTER: " + fly.getTitle() + " saved.");
		} else {
			throw new IllegalArgumentException(
					"Wrong file extension (Use .fly).");
		}
	}

	public void csv(Fly fly, Adjust adj) throws IOException {

		short[][] pixel;
		String compLine = "0,";
		if (adj == Adjust.ALL) {
			pixel = fly.getGraphData();
		} else {
			pixel = fly.getCompressed();

			for (int i = 0; i < pixel.length; i++) {
				compLine += "0";
				if (i < pixel.length - 2)
					compLine += ",";
			}
		}

		String path = file.getPath();
		path = path.substring(0, path.length() - 3) + "csv";
		File f = new File(path);
		FileWriter fw = new FileWriter(f);
		BufferedWriter bw = new BufferedWriter(fw);

		for (int i = 0; i < pixel[0].length; i++) {
			String line = pixel[0][i] + ",";
			for (int j = 1; j < pixel.length; j++) {
				line += pixel[j][i];
				if (j < pixel.length - 1)
					line += ",";
			}

			if (!line.equals(compLine)) {
				bw.write(line);
				bw.newLine();
			}
		}
		bw.flush();
		bw.close();
		System.out
				.println("EXPORTER: " + fly.getTitle() + " exported as .csv.");
	}

	public void log() {

		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					DmelanoNMJ.xsrcPath + "/log.txt"));

		} catch (FileNotFoundException e) {
			System.err.println("Could not export Log.");
			e.printStackTrace();
		}
	}

	//getters
	public int getStatus(){
		return (int)percent;
	}
	
	// setters

	public void setGraphValsExport(boolean boo) {
		graphValsBoo = boo;
	}

	public void setTrueValsExport(boolean boo) {
		trueValsBoo = boo;
	}

}
