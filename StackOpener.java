package Util;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import ij.*;
import ij.process.ShortProcessor;

public class StackOpener {

	private File file;
	private String[] header; // name, description, dimensions
	private String[] dimensions;// width, height, size
	private int width;
	private int height;
	private int size;
	private short[] pixels;

	public StackOpener(String path) {
		this.file = new File(path);

		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;

			for (int s = 0; s < size + 1; s++) {
				line = br.readLine();
				// header
				if (line.contains("DmelanoNMJ")) {
					header = line.split("Stack Size.");
					dimensions = header[1].split(",");
					width = Integer.parseInt(dimensions[0]);
					height = Integer.parseInt(dimensions[1]);
					size = Integer.parseInt(dimensions[2]);
					pixels = new short[width * height];
					break;
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// getters

	public ImageStack getStack() {
		
		ImageStack stack = new ImageStack(width, height);
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;

			for (int s = 0; s < size + 1; s++) {
				line = br.readLine();
				// header
				if (line.contains("DmelanoNMJ")) {

				} else {

					String[] inter = line.split(",");
					for (int i = 0; i < inter.length; i++) {
						pixels[i] = Short.parseShort(inter[i]);
					}

					ShortProcessor sp = new ShortProcessor(width, height,
							pixels, null);

					stack.addSlice(Integer.toString(s), sp);
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stack;
	}

	public ShortStack getShortStack() {

		ShortStack stacks = new ShortStack(size, width, height);

		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;

			for (int s = -1; s < size; s++) {
				line = br.readLine();

				if (s > -1) {
					// header
					//System.out.println(size+" "+s+"\t"+line);
					pixels = new short[width * height];
					String[] inter = line.split(",");
					for (int i = 0; i < inter.length; i++) {
						pixels[i] = Short.parseShort(inter[i]);
					}
					stacks.fill(s, pixels);
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return stacks;
	}
}
