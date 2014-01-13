package Util;
import java.awt.Image;
import java.util.Arrays;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(namespace = "Fly")
// @XmlType(propOrder = { "title", "imgTitle", "img", "notes", "components",
// "graphData" })
public class Fly {

	private String title;
	private Image highRes;
	private String imgTitle;
	private String notes;

	private int[][] components;
	private int compNo;
	private short[][] graphVals;
	private short[][] graphData;
	private short[][] compressed;
	private short[][] trueVals;

	private String[] summary = { "FLY: SUMMARY", "FLY: Title:\t",
			"FLY: " + "Comps.:\t", "FLY: Raw Graph:\t", "FLY: Graph:\t",
			"FLY: Raw Data:\t" };

	// constructors

	public Fly() {

	}

	public Fly(int[][] comps) {
		this.components = comps;
		compNo = 0;
		for (int i = 0; i < components.length; i++) {
			compNo += components[i].length / 2;
		}
		generateGraphData();
	}

	public Fly(String title, int[][] comps) {
		this.title = title;
		this.components = comps;
		compNo = 0;
		for (int i = 0; i < components.length; i++) {
			compNo += components[i].length / 2;
		}
		generateGraphData();
	}

	public Fly(String title, Image hRes, int[][] comps) {
		this.title = title;
		this.highRes = hRes;
		this.imgTitle = "MaxInt_" + title;
		this.components = comps;
		compNo = 0;
		for (int i = 0; i < components.length; i++) {
			compNo += components[i].length / 2;
		}
		generateGraphData();
	}

	public Fly(String title, int[][] comps, short[][] graphVals) {
		this.title = title;
		this.components = comps;
		this.graphVals = graphVals;
		compNo = 0;
		for (int i = 0; i < components.length; i++) {
			compNo += components[i].length / 2;
		}
		generateGraphData();
	}

	public Fly(String title, Image hRes, int[][] comps, short[][] graphVals) {
		this.title = title;
		this.highRes = hRes;
		this.components = comps;
		this.graphVals = graphVals;
		compNo = 0;
		for (int i = 0; i < components.length; i++) {
			compNo += components[i].length / 2;
		}
		generateGraphData();
	}

	public Fly(String title, Image hRes, String notes, int[][] comps,
			short[][] graphVals, short[][] trueVals) {
		this.title = title;
		this.highRes = hRes;
		this.notes = notes;
		this.components = comps;
		this.graphVals = graphVals;
		this.trueVals = trueVals;
		compNo = 0;
		for (int i = 0; i < components.length; i++) {
			compNo += components[i].length / 2;
		}
		generateGraphData();
	}

	// XXXXXXXXXXX Functions

	private void generateGraphData() {

		if ((graphVals != null) && (components != null)) {

			graphData = new short[components.length][graphVals[0].length];
			int iterator = 0;

			for (int i = 0; i < components.length; i++) {

				for (int k = 0; k < graphVals[0].length; k++) {

					short avg = 0;
					for (int j = 0; j < components[i].length / 2; j++) {
						avg += graphVals[j + iterator][k];
					}
					graphData[i][k] = (short) (avg / (components[i].length / 2));
				}
				iterator += components[i].length / 2;
			}
			System.out.println("FLY: Graph Data generated.");
		}
	}

	public void compressGraph(int pixDensity) {

		if (graphData != null) {
			int pixelDen = graphData[0].length / pixDensity + 1;
			compressed = new short[graphData.length][pixDensity];

			for (int i = 0; i < graphData.length; i++) {

				for (int j = 0; j < graphData[i].length / pixelDen; j++) {

					MathCalc math = new MathCalc(
							Arrays.copyOfRange(graphData[i], j * pixelDen, j
									* pixelDen + pixelDen));
					compressed[i][j] = math.getMean();
				}
			}

			System.out.println("FLY: Graph Data compressed to a 1:" + pixelDen
					+ " pixel ratio.");
		} else {
			System.err.println("FLY: Graph Data: NullPointerException.");

		}
	}

	// getters

	public String getTitle() {
		return title;
	}

	public Image getImg() {
		return highRes;
	}

	public String getImgTitle() {
		return imgTitle;
	}

	public String getNotes() {
		return notes;
	}

	public int[][] getComponents() {
		return components;
	}

	public short[][] getGraphVals() {
		return graphVals;
	}

	public short[][] getGraphData() {
		return graphData;
	}

	public short[][] getCompressed() {
		return compressed;
	}

	public short[][] getTrueVals() {
		return trueVals;
	}

	public void getSummary() {
		summary[1] += title;
		if (components != null) {
			summary[2] += components.length;
		} else {
			summary[2] += "-";
		}
		if (graphVals != null) {
			summary[3] += graphVals.length;
		} else {
			summary[3] += "-";
		}
		if (graphData != null) {
			summary[4] += graphData.length + ", " + graphData[0].length;
		} else {
			summary[4] += "-";
		}
		if (trueVals != null) {
			summary[5] += trueVals.length + ", " + trueVals[0].length;
		} else {
			summary[5] += "-";
		}
		for (String line : summary) {
			System.out.println(line);
		}
	}

	// setters

	public void setTitle(String title) {
		this.title = title;
	}

	public void setImg(Image hRes) {
		this.highRes = hRes;
	}

	public void setImgTitle(String title) {
		imgTitle = title;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public void setComponents(int[][] comps) {
		this.components = comps;
		compNo = 0;
		for (int i = 0; i < components.length; i++) {
			compNo += components[i].length / 2;
		}
		generateGraphData();
	}

	public void setGraphVals(short[][] vals) {
		this.graphVals = vals;
		generateGraphData();
	}

	public void setGraphData(short[][] vals) {
		this.graphData = vals;
	}

	public void setCompressed(short[][] comp) {
		this.compressed = comp;
	}

	public void setTrueVals(short[][] vals) {
		this.trueVals = vals;
	}

}
