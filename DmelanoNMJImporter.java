package UI;

import ij.ImagePlus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import Util.Adjust;
import Util.CaReleaseMap;
import Util.DmelanoNMJ;
import Util.Exporter;
import Util.Fly;
import Util.JPEGopener;
import Util.ShortStack;
import Util.StackOpener;

public class DmelanoNMJImporter {

	private File txtF;
	private File imgF;
	private int importQ;

	private String name;
	private String notes;
	private String project;
	private int baseLineInterval = 8000;

	private int progress = 1;
	private String update = "Preparing...";

	private boolean done = false;

	public DmelanoNMJImporter(String txtFile, String imgFile, String project) {
		txtF = new File(DmelanoNMJ.submissionPath + "/" + txtFile);
		imgF = new File(DmelanoNMJ.submissionPath + "/" + imgFile);
		this.project = project;
	}

	public DmelanoNMJImporter(String txtFile, String imgFile, String project,
			int importQueue) {
		txtF = new File(DmelanoNMJ.submissionPath + "/" + txtFile);
		imgF = new File(DmelanoNMJ.submissionPath + "/" + imgFile);
		this.project = project;
		importQ = importQueue;
	}

	// FUNCTIONS
	public void addFly() {

		new Thread(new Runnable() {

			public void run() {

				File file = imgF;
				JPEGopener op = new JPEGopener(file.getPath());
				ImagePlus img = op.getImage();
				progress = 6;
				update = "Loading Image...";

				CaReleaseMap caMap = new CaReleaseMap(img, DmelanoNMJ.LOWRES);
				int[][] components = caMap.getComponents();
				progress = 20;
				update = "Determining Connected Components...";

				file = txtF;
				StackOpener stOp = new StackOpener(file.getPath());
				progress = 24;
				ShortStack stack = stOp.getShortStack();
				stack.setComponents(components);
				// stack.setBaseInterval(baseLineInterval);
				stack.setThreads(1);
				stack.adjust(Adjust.ACC);
				progress = 35;
				update = "Generate Graph Data...";

				notes = "(Graph adjusted using a Median-Window size of "
						+ String.valueOf(stack.getBaseInterval()) + " frames). " + notes;
				Fly fly = new Fly(name, components, stack.getGraphVals());
				fly.setNotes(notes);
				fly.setImgTitle(fly.getTitle() + ".jpg");
				progress = 38;
				update = "Creating fly...";

				// save .fly file
				progress = 70;
				update = "Saving .fly File...";

				file = new File(DmelanoNMJ.projectPath + "/" + project + "/"
						+ fly.getTitle() + ".fly");
				Exporter ex = new Exporter(file);
				try {
					ex.save(fly);
				} catch (IOException e) {
					e.printStackTrace();
				}

				// save image t0 project folder
				File source = imgF;
				File dest = new File(DmelanoNMJ.projectPath + "/" + project
						+ "/" + fly.getImgTitle());
				FileChannel sourceChannel = null;
				FileChannel destChannel = null;
				try {
					sourceChannel = new FileInputStream(source).getChannel();
					destChannel = new FileOutputStream(dest).getChannel();
					destChannel.transferFrom(sourceChannel, 0,
							sourceChannel.size());
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						sourceChannel.close();
						destChannel.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				progress = 80;
				update = "Copying Image Files...";

				System.out.println("DmNMJIMPORTER: Image copied.");

				// delete from submission folder
				try {
					Display.delete(imgF);
					Display.delete(txtF);
				} catch (IOException e) {
					e.printStackTrace();
				}

				progress = 95;
				update = "Updating Submission Folder...";

				System.out.println("DmNMJIMPORTER: Analysis complete.");
				done = true;

			}
		}).start();

	}

	// SETTERS
	public void setIdentity(String name, String note) {
		this.name = name;
		this.notes = note;

	}

	public void setBaseLineInterval(int interval) {
		baseLineInterval = interval;
	}

	// GETTERS

	public int getProgress() {
		return progress;
	}

	public int getImportQueue() {
		return importQ;
	}

	public boolean getComplete() {
		return done;
	}

	public String getUpdate() {
		return update;
	}
}
