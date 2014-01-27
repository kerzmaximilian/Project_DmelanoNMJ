package Util;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.JAXBException;

import UI.Display;

public class DmelanoNMJ {

	public static int LOWRES = 32;
	public static char[] interMap;
	public static int[][] components;
	public static int medianWindow = 8000;
	public static double quantile = 0.2;

	public static String projectPath;
	public static String submissionPath;
	public static String xsrcPath;
	public static String projectSelected;
	public volatile static String[] submissions;
	public static String[] projects;

	public static void main(String[] args) throws IOException, JAXBException {

		// creating new directory and SETUP
		String userPath = System.getProperty("user.home");

		try {
			BufferedReader reader = new BufferedReader(new FileReader(userPath
					+ "/Documents/DmelanoNMJ/XSrc/log.txt"));
			reader.close();
		} catch (FileNotFoundException e) {

			@SuppressWarnings("unused")
			boolean projects = (new File(userPath
					+ "/Documents/DmelanoNMJ/Projects/")).mkdirs();
			@SuppressWarnings("unused")
			boolean submission = (new File(userPath
					+ "/Documents/DmelanoNMJ/Submission/")).mkdirs();
			@SuppressWarnings("unused")
			boolean resources = (new File(userPath
					+ "/Documents/DmelanoNMJ/XSrc/")).mkdirs();
			File file = new File(userPath
					+ "/Documents/DmelanoNMJ/XSrc/DmelanoNMJ.init");

			Date dNow = new Date();
			SimpleDateFormat ft = new SimpleDateFormat(
					"yyyy.MM.dd 'at' hh:mm:ss a zzz");

			file.createNewFile();
			FileWriter fw = null;
			fw = new FileWriter(file.getAbsoluteFile());

			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("DmelanoNMJ Parameters");
			bw.newLine();
			bw.write("Created on " + ft.format(dNow) + ".");
			bw.flush();
			bw.close();

		}

		// Set paths and check for submissions and projects
		submissionPath = userPath + "/Documents/DmelanoNMJ/Submission";
		projectPath = userPath + "/Documents/DmelanoNMJ/Projects/";
		xsrcPath = userPath + "/Documents/DmelanoNMJ/XSrc/";

		// Initialise UI
		
		
		Display d = new Display();
		d.start();
	
	}

}
