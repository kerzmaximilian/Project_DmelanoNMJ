package UI;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFrame;
import javax.xml.bind.JAXBException;

import Util.Adjust;
import Util.DmelanoNMJ;
import Util.Exporter;
import Util.Fly;
import Util.Importer;

@SuppressWarnings("serial")
public class Display extends Canvas implements Runnable {

	public static final int WIDTH = 1200;
	public static final int HEIGHT = WIDTH / 12 * 7;
	public static final String NAME = "Dmelano Manager";

	private JFrame frame;
	private boolean run;
	// whatever is inserted into screen[] is displayed in img!!
	private BufferedImage img = new BufferedImage(WIDTH, HEIGHT,
			BufferedImage.TYPE_INT_RGB);
	private int[] screen = ((DataBufferInt) img.getRaster().getDataBuffer())
			.getData();
	private int tickCount = 0;
	private KKeyEvent KE = new KKeyEvent(this);
	@SuppressWarnings("unused")
	private KMouseEvent ME = new KMouseEvent(this);
	@SuppressWarnings("unused")
	private KScrollEvent SE = new KScrollEvent(this);

	public Display() {
		setMinimumSize(new Dimension(WIDTH, HEIGHT));
		setMaximumSize(new Dimension(WIDTH, HEIGHT));
		setPreferredSize(new Dimension(WIDTH, HEIGHT));

		frame = new JFrame(NAME);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.add(this, BorderLayout.CENTER);
		frame.pack();

		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

	}

	public synchronized void start() {
		run = true;
		new Thread(this).start();
	}

	public synchronized void stop() {

		run = false;
	}

	public void run() {

		long lastTime = System.nanoTime();
		long lastTimer = System.currentTimeMillis();
		double nsPerTick = 1000000000D / 120D;
		int ticks = 0;
		int frames = 0;
		double delta = 0;

		// sets timing for tick() and render()
		while (run) {

			long now = System.nanoTime();
			delta += (now - lastTime) / nsPerTick;
			lastTime = now;
			// set to FALSE if ticks should equal render rate
			boolean shouldRender = false;

			while (delta >= 1) {
				ticks++;
				tick();
				delta -= 1;
				shouldRender = true;
			}

			if (shouldRender) {
				frames++;
				render();
			}
			if (System.currentTimeMillis() - lastTimer >= 1000) {
				lastTimer += 1000;
				System.out.println(ticks + " Ticks, " + frames + " Frames");
				ticks = 0;
				frames = 0;
			}
		}
	}

	// UPDATE
	public void tick() {
		tickCount++;
		background();

		sendToScreen();

	}

	// RENDER
	public void render() {
		BufferStrategy bs = getBufferStrategy();
		if (bs == null) {
			createBufferStrategy(3);
			return;
		}

		Graphics2D g = (Graphics2D) bs.getDrawGraphics();
		g.drawImage(img, 0, 0, getWidth(), getHeight(), null);
		g.dispose();
		bs.show();
	}

	// Other
	private void background() {

		if (background == true) {
			for (int i = 0; i < screen.length; i++) {
				screen[i] = KGraphics.getBGColorInt();
			}
		}
	}

	// SEND TO SCREEN
	// windows
	private boolean background = true;
	private boolean startUp = true;
	private boolean loadContent = true;
	private boolean projectWindow = false;
	private boolean flyWindow = false;
	private boolean graphWindow = false;
	private boolean selectComponents = false;
	private boolean sideBar = false;
	private boolean analyseFly = false;
	private boolean importAll = false;

	// Directory Related
	private String subPath = DmelanoNMJ.submissionPath;
	private String proPath = DmelanoNMJ.projectPath;
	// private String srcPath = DmelanoNMJ.xsrcPath;
	private String[] projects;
	private volatile Fly[][] flies;
	private Thread thread;

	// Colors
	private Color dColor = KGraphics.getDefaultColor();

	// Windows
	private int menuH;
	private KRaster proDisplay = new KRaster(20, 100, WIDTH / 4 * 3 + 20,
			HEIGHT - 120);
	private KButton view = new KButton(WIDTH - WIDTH / 8 - 60, HEIGHT - HEIGHT
			/ 8, 120, 25);
	private KButton add = new KButton(WIDTH - WIDTH / 8 - 60, HEIGHT - HEIGHT
			/ 8, 120, 25);
	private KButton proDelete = new KButton(WIDTH - 120, 70, 100, 25);
	private KWrapper note = new KWrapper(WIDTH / 4 * 3 + 20, HEIGHT / 32 * 6,
			WIDTH / 4 - 40, HEIGHT / 24 * 16);
	private KButton addAll = new KButton(WIDTH - 140, HEIGHT - HEIGHT / 8, 120,
			25);
	private String[] pFlyNo;
	private String[] proNotes;
	private int selTemp = -1;
	private volatile String newProName = "";
	private volatile String newProNotes = "";
	private KRaster flyDisplay = new KRaster(20, 100, WIDTH / 4 * 3 + 20,
			HEIGHT - 120);
	private KImage[] images;
	private String[] caption;
	private KList submList;
	private String[] fileNames;
	private String[] imgNames;
	private String fileName;
	private String imgName;
	private DmelanoNMJImporter[] imp;

	private int[][] components;
	private KGraph[] graph;
	private int graphWidth = 400;
	private int graphHeight = 100;
	private int pLine = (WIDTH / 4 * 3 + 10) / (graphWidth + 30);
	private String perLine = String.valueOf(pLine);
	private String gWidth = "400";
	private String gHeight = "100";
	private KRaster graphRaster;
	private KList exList1;
	private KList exList2;
	private KImage preView;
	private KImage deselectImg;
	private KFont descript;
	private int[] deselected = new int[0];

	private boolean graphAllowed = true;
	private volatile File txtFile;
	private volatile File imgFile;
	private volatile int baseLineInterval = 8000;
	private static int progBarMax = WIDTH / 4 * 3;
	private volatile static int progBarStatus = 0;
	private KLine progBar = new KLine(0, menuH + 2, 1, menuH + 2);
	private KFont percent = new KFont(KGraphics.getDefaultFont(), 40);

	private int noOfAllJobs = 0;
	private int noOfJobs = 0;
	private int inputIt = 0;
	private int runBar = 0;
	private int run1Max = 0;
	// private int run2Max = 0;
	// private int run3Max = 0;
	private int run1 = 0;

	// private int run2 = 0;
	// private int run3 = 0;

	private void sendToScreen() {

		importAll();
		analyseFly();

		startUp();
		projectWindow();
		flyWindow();
		graphWindow();
		sidebar();
		progressBar();

	}

	// WINDOWS
	private void startUp() {

		if (startUp == true) {
			KFont title = new KFont(KGraphics.getDefaultFont(), 40);
			title.print("DmelanoNMJ Manager");
			title.translate(WIDTH / 2 - title.getStringWidth() / 2, HEIGHT / 2
					- title.getStringHeight());
			title.setFontColor(dColor);
			screen = title.add(screen);
			KFont update = new KFont();
			update.print(WIDTH / 2 - title.getStringWidth() / 2 + 5,
					HEIGHT / 2, "Loading content...");
			screen = update.add(screen);
			if (loadContent == true) {
				thread = new Thread(new Runnable() {
					public void run() {
						loadContent();
					}
				});
				thread.start();
			}
			if ((thread.isAlive() == false) && (tickCount > 80)) {
				startUp = false;
				projectWindow = true;
				proDisplay.setFiles(projects);
				proDisplay.setCaption(pFlyNo);
			}
		}
	}

	private void projectWindow() {
		if (projectWindow == true) {

			// Title
			KFont title = new KFont(KGraphics.getDefaultFont(), 40);
			title.print(20, 10, "Projects");
			screen = title.add(screen);
			menuH = title.getStringHeight() + 10;
			KLine line = new KLine(0, menuH, WIDTH,
					10 + title.getStringHeight());
			screen = line.add(screen);

			// Project viewer
			proDisplay.addActionListener();
			screen = proDisplay.add(screen);

			// Project info
			if ((proDisplay.getSelected() > -1) || (proDisplay.addNew())) {

				title = new KFont(KGraphics.getDefaultFont(), 20);

				// view project
				if ((proDisplay.getSelected() >= 0)
						&& (proDisplay.addNew() == false)) {
					title.print(WIDTH / 4 * 3 + 20, menuH + 10,
							projects[proDisplay.getSelected()]);
					note.setTextToNull();
					proNotes[proDisplay.getSelected()] = note.setText(
							proNotes[proDisplay.getSelected()], KE);
					note.translate(WIDTH / 4 * 3 + 20, HEIGHT / 32 * 6);
					note.setDimensions(WIDTH / 4 - 40, HEIGHT / 24 * 16);
					note.setBorder(true);
					note.setLineWidth(1);
					note.setCursor(true);
					note.addActionListener();
					screen = note.add(screen);

					view.setTitle("View");
					view.addActionListener();
					screen = view.add(screen);

					proDelete.setTitle("Delete");
					proDelete.addActionListener();
					screen = proDelete.add(screen);

					if (view.getStatus() == true) {
						view.setStatus(false);
						images = null;
						flyWindow = true;
						projectWindow = false;
					}

					if (proDelete.getStatus() == true) {
						proDelete.setStatus(false);
						remove(proDisplay.getSelected(), "PROJECT");
						selTemp = -1;
					}
				}

				// add project
				if (proDisplay.addNew() == true) {
					title.print(WIDTH / 4 * 3 + 20, menuH + 10, "New Project:");

					KFont label = new KFont();
					label.print(WIDTH / 4 * 3 + 20, menuH + 50, "Project Name:");
					screen = label.add(screen);
					KWrapper name = new KWrapper(WIDTH / 4 * 3 + 20,
							menuH + 74, WIDTH / 4 - 40, 20);
					name.setCursor(true);
					name.setBorder(true);
					name.setLineWidth(1);
					name.addActionListener();
					newProName = name.setText(newProName, KE);
					screen = name.add(screen);

					label.print(WIDTH / 4 * 3 + 20, menuH + 120, "Add Notes:");
					screen = label.add(screen);
					name = new KWrapper(WIDTH / 4 * 3 + 20, menuH + 144,
							WIDTH / 4 - 40, HEIGHT / 8 * 4);
					name.setCursor(true);
					name.setBorder(true);
					name.setLineWidth(1);
					name.addActionListener();
					newProNotes = name.setText(newProNotes, KE);
					screen = name.add(screen);

					add.setTitle("Add");
					add.addActionListener();
					screen = add.add(screen);

					if ((add.getStatus() == true) && (newProName.equals(""))) {
						KFont warn = new KFont();
						warn.setFontColor(250, 70, 0);
						warn.print(WIDTH / 4 * 3 + 115, menuH + 50,
								"Enter a Project Name!");
						screen = warn.add(screen);
					}

					if ((add.getStatus() == true) && (!newProName.equals(""))) {
						add.setStatus(false);
						view.setStatus(false);
						addProject(newProName, newProNotes);
						newProName = "";
						newProNotes = "";
					}
				}
				screen = title.add(screen);
				line = new KLine(WIDTH / 4 * 3 + 20, menuH + 10
						+ title.getStringHeight(), WIDTH / 4 * 3 + 20
						+ title.getStringWidth(), menuH + 10
						+ title.getStringHeight());
				screen = line.add(screen);

				// save notes
				if ((proDisplay.getSelected() != selTemp) && (selTemp >= 0)) {
					File file = new File(proPath + "/" + projects[selTemp]
							+ "/_PROJECT.txt");
					writeToFile(proNotes[selTemp], file);
				}
				if (selTemp < 0)
					selTemp = proDisplay.getSelected();
			}

		}
	}

	public void flyWindow() {
		if (flyWindow == true) {

			// Title
			KFont title = new KFont(KGraphics.getDefaultFont(), 40);
			title.print(20, 10,
					"Projects > " + projects[proDisplay.getSelected()]);
			screen = title.add(screen);
			menuH = title.getStringHeight() + 10;
			KLine line = new KLine(0, menuH, WIDTH,
					10 + title.getStringHeight());
			screen = line.add(screen);

			// display Raster
			if ((images == null) && (flies[proDisplay.getSelected()] != null)) {

				images = new KImage[flies[proDisplay.getSelected()].length];
				caption = new String[flies[proDisplay.getSelected()].length];
				for (int i = 0; i < images.length; i++) {

					BufferedImage img = (BufferedImage) flies[proDisplay
							.getSelected()][i].getImg();
					images[i] = new KImage(img, 0, 0, 100);
					caption[i] = flies[proDisplay.getSelected()][i].getTitle();
				}
				// set images and caption
				if (images.length > 0) {
					flyDisplay.setImages(images);
					flyDisplay.setFlyTitles(caption);
				}

				// reset possible present images
				if (images.length == 0) {
					flyDisplay.reset();
				}
			}

			flyDisplay.addActionListener();
			screen = flyDisplay.add(screen);

			// view and add functions
			// Project info
			if ((flyDisplay.getSelected() > -1) || (flyDisplay.addNew())) {

				title = new KFont(KGraphics.getDefaultFont(), 20);

				// view project
				if ((flyDisplay.getSelected() >= 0)
						&& (flyDisplay.addNew() == false)) {
					title.print(WIDTH / 4 * 3 + 20, menuH + 10,
							caption[flyDisplay.getSelected()]);

					note.setTextToNull();
					// Write notes from and to Fly
					flies[proDisplay.getSelected()][flyDisplay.getSelected()]
							.setNotes(note.setText(flies[proDisplay
									.getSelected()][flyDisplay.getSelected()]
									.getNotes(), KE));
					note.translate(WIDTH / 4 * 3 + 20, menuH + HEIGHT / 2);
					note.setDimensions(WIDTH / 4 - 40, HEIGHT / 8 * 2);
					note.setBorder(true);
					note.setLineWidth(1);
					note.setCursor(true);
					note.addActionListener();
					screen = note.add(screen);

					KImage image = new KImage(
							flies[proDisplay.getSelected()][flyDisplay.getSelected()]
									.getImg(), WIDTH / 4 - 40);
					components = flies[proDisplay.getSelected()][flyDisplay
							.getSelected()].getComponents();
					image.setComponents(components);
					image.translate(WIDTH / 4 * 3 + 20, menuH + 50);
					screen = image.add(screen);

					KFont label = new KFont();
					label.print(WIDTH / 4 * 3 + 20,
							menuH + 54 + WIDTH / 4 - 40, components.length
									+ " Components");
					label.setFontColor(dColor);
					screen = label.add(screen);

					view.setTitle("View");
					view.addActionListener();
					screen = view.add(screen);

					proDelete.setTitle("Delete");
					proDelete.addActionListener();
					screen = proDelete.add(screen);

					if ((view.getStatus() == true) && (graphAllowed == true)) {
						view.setStatus(false);
						flyWindow = false;
						graphWindow = true;
					}

					if (graphAllowed == false) {
						KFont status = new KFont();
						status.setFontColor(250, 70, 0);
						status.print("UNABLE TO SHOW GRAPH");
						status.translate(WIDTH - 150 - status.getStringWidth()
								/ 2, HEIGHT - HEIGHT / 8 + 40);
						screen = status.add(screen);
						status.print("UNTIL IMPORT COMPLETED");
						status.translate(WIDTH - 150 - status.getStringWidth()
								/ 2, HEIGHT - HEIGHT / 8 + 60);
						screen = status.add(screen);
					}

					if (proDelete.getStatus() == true) {
						proDelete.setStatus(false);
						remove(flyDisplay.getSelected(), "FLY");
					}
				}

				// add project
				if (flyDisplay.addNew() == true) {
					title.print(WIDTH / 4 * 3 + 20, menuH + 10, "New Fly:");

					KFont label = new KFont();
					label.print(WIDTH / 4 * 3 + 20, menuH + 50, "Fly Name:");
					screen = label.add(screen);
					KWrapper name = new KWrapper(WIDTH / 4 * 3 + 20,
							menuH + 74, WIDTH / 4 - 40, 20);
					name.setCursor(true);
					name.setBorder(true);
					name.setLineWidth(1);
					name.addActionListener();
					newProName = name.setText(newProName, KE);
					screen = name.add(screen);

					label.print(WIDTH / 4 * 3 + 20, menuH + 120, "Add Notes:");
					screen = label.add(screen);
					name = new KWrapper(WIDTH / 4 * 3 + 20, menuH + 144,
							WIDTH / 4 - 40, HEIGHT / 8);
					name.setCursor(true);
					name.setBorder(true);
					name.setLineWidth(1);
					name.addActionListener();
					newProNotes = name.setText(newProNotes, KE);
					screen = name.add(screen);

					label.print(WIDTH / 4 * 3 + 20, menuH + 170 + HEIGHT / 8,
							"Select from Submissions:");
					screen = label.add(screen);

					if (submList == null) {

						// selects files found in submission folder
						File removeDS = new File(subPath + "/.DS_Store");
						removeDS.delete();
						File projectFolder = new File(subPath);
						File[] fList = projectFolder.listFiles();

						fileNames = new String[fList.length / 2];
						int itter = 0;
						for (int i = 0; i < fList.length; i++) {
							if ((fList[i].getName()).contains(".txt")) {
								fileNames[itter] = fList[i].getName();
								itter++;
							}
						}

						imgNames = new String[fileNames.length];
						itter = 0;
						for (int i = 0; i < fList.length; i++) {
							if ((fList[i].getName()).contains(".jpg")) {
								imgNames[itter] = fList[i].getName();
								itter++;
							}
						}

						String[] showInList = new String[fileNames.length];
						for (int i = 0; i < showInList.length; i++) {
							showInList[i] = fileNames[i] + "  &  "
									+ imgNames[i];
						}

						submList = new KList(showInList, WIDTH / 4 * 3 + 20,
								menuH + 194 + HEIGHT / 8, WIDTH / 4 - 40,
								HEIGHT / 8 * 3);
					}
					submList.setLineWidth(1);
					submList.addActionListener();
					screen = submList.add(screen);

					add.setTitle("Import");
					if (fileNames.length > 1)
						add.translate(WIDTH / 4 * 3 + 20, HEIGHT - HEIGHT / 8);
					add.addActionListener();
					screen = add.add(screen);

					// option to import all
					if (fileNames.length > 1) {
						addAll.setTitle("Import All");
						addAll.addActionListener();
						screen = addAll.add(screen);
					}

					if (((addAll.getStatus() == true) || (add.getStatus() == true))
							&& (newProName.equals(""))) {
						KFont warn = new KFont();
						warn.setFontColor(250, 70, 0);
						warn.print(WIDTH / 4 * 3 + 90, menuH + 50,
								"Enter a Fly Name!");
						screen = warn.add(screen);
					}

					if ((add.getStatus() == true)
							&& (submList.getSelected() == -1)) {
						KFont warn = new KFont();
						warn.setFontColor(250, 70, 0);
						warn.print(WIDTH / 4 * 3 + 20,
								menuH + 150 + HEIGHT / 8, "Select Input File!");
						screen = warn.add(screen);
					}

					if ((add.getStatus() == true) && (!newProName.equals(""))
							&& (submList.getSelected() > -1)) {
						add.setStatus(false);
						view.setStatus(false);

						fileName = fileNames[submList.getSelected()];
						imgName = imgNames[submList.getSelected()];

						analyseFly = true;
						progBarStatus = 4;

						submList = null;
						images = null;
						flyDisplay.reset();
						flyDisplay.highlight(-1);
						graphWindow = false;
					}

					if (graphAllowed == false) {
						KFont status = new KFont();
						status.setFontColor(250, 70, 0);
						status.print("UNABLE TO ADD FLY");
						status.translate(WIDTH - 150 - status.getStringWidth()
								/ 2, HEIGHT - HEIGHT / 8 + 40);
						screen = status.add(screen);
						status.print("UNTIL IMPORT COMPLETED");
						status.translate(WIDTH - 150 - status.getStringWidth()
								/ 2, HEIGHT - HEIGHT / 8 + 60);
						screen = status.add(screen);
					}

					// toggle import all option
					if ((addAll.getStatus() == true)
							&& (!newProName.equals(""))) {
						add.setStatus(false);
						importAll = true;
						noOfAllJobs = fileNames.length;
						noOfJobs = fileNames.length;
					}
				}
				screen = title.add(screen);
				line = new KLine(WIDTH / 4 * 3 + 20, menuH + 10
						+ title.getStringHeight(), WIDTH / 4 * 3 + 20
						+ title.getStringWidth(), menuH + 10
						+ title.getStringHeight());
				screen = line.add(screen);

			}

			// return to Projects Window
			if (KMouseEvent.mouseIn(0, 0, 160, 60)) {
				flyWindow = false;
				projectWindow = true;
				images = null;
				flyDisplay.reset();
				flyDisplay.highlight(-1);
				proDisplay.highlight(-1);
				graph = null;
			}
		}
	}

	public void graphWindow() {
		if ((graphAllowed == true) && (graphWindow == true)) {

			sideBar = true;

			// Title
			KFont title = new KFont(KGraphics.getDefaultFont(), 40);
			title.print(
					20,
					10,
					"Projects > "
							+ projects[proDisplay.getSelected()]
							+ " > "
							+ flies[proDisplay.getSelected()][flyDisplay
									.getSelected()].getTitle());
			screen = title.add(screen);
			menuH = title.getStringHeight() + 10;
			KLine line = new KLine(0, menuH, WIDTH,
					10 + title.getStringHeight());
			screen = line.add(screen);

			if (graph == null) {

				flies[proDisplay.getSelected()][flyDisplay.getSelected()]
						.compressGraph(graphWidth);
				int[][] graphData = flies[proDisplay.getSelected()][flyDisplay
						.getSelected()].getCompressedInt();

				graph = new KGraph[components.length];

				int itter = 0;
				for (int i = 0; i < components.length; i++) {

					if (deselected.length > 0) {
						for (int j = 0; j < deselected.length; j++) {
							if (deselected[j] == i) {
								itter++;
								System.out.println(itter + " " + i);
								break;
							}
						}
					}

					graph[i] = new KGraph("Component "
							+ String.valueOf(i + 1 + itter), graphData[i
							+ itter], 0, 0, graphWidth, graphHeight);
					graph[i].setFontColor(dColor);
				}
				graphRaster = new KRaster(graph, 70, 100, WIDTH / 4 * 3 + 50,
						HEIGHT - 20);
			}

			if (graphRaster != null) {
				graphRaster.addActionListener();
				screen = graphRaster.add(screen);
			}

			// return to Fly Window
			if (KMouseEvent.mouseIn(160, 0, 200, 60)) {
				sideBar = false;
				flyWindow = true;
				graphWindow = false;
				projectWindow = false;
				graph = null;
				images = null;
				preView = null;
				flyDisplay.highlight(-1);
			}
			// return to Projects Window
			if (KMouseEvent.mouseIn(0, 0, 160, 60)) {
				sideBar = false;
				flyWindow = false;
				graphWindow = false;
				projectWindow = true;
				graph = null;
				images = null;
				preView = null;
				flyDisplay.highlight(-1);
				proDisplay.highlight(-1);
			}
		}
	}

	public void sidebar() {

		if (sideBar == true) {

			// Sidebar
			KRect sideBar = new KRect(WIDTH - 160, 80, 140, 595);
			sideBar.noFill(new Color(KGraphics.getFontColorInt()));
			screen = sideBar.add(screen);

			// title
			KFont sideBT = new KFont();
			sideBT.setFontColor(dColor);
			sideBT.print(WIDTH - 140, 90, "Graph  Display");
			screen = sideBT.add(screen);
			sideBT.print("Options");
			sideBT.translate(WIDTH - 90 - sideBT.getStringWidth() / 2, 108);
			screen = sideBT.add(screen);

			// component selection via image
			if (preView == null) {
				preView = new KImage(
						flies[proDisplay.getSelected()][flyDisplay.getSelected()]
								.getImg(), 100);
				preView.translate(WIDTH - 140, 140);
			}
			preView.setComponents(components);
			screen = preView.add(screen);

			KButton reset = new KButton(WIDTH - 140, 250, 100, 25);
			reset.setTitle("Reset");
			reset.noFill(dColor);
			screen = reset.add(screen);

			KLine border = new KLine(WIDTH - 150, 290, WIDTH - 30, 290);
			border.fill(new Color(KGraphics.getFontColorInt()));
			screen = border.add(screen);

			// updating graph dimensions
			KFont label = new KFont();
			label.print("Graphs/Line:");
			label.translate(WIDTH - 90 - label.getStringWidth() / 2, 300);
			screen = label.add(screen);
			KWrapper graphOpt = new KWrapper(WIDTH - 130, 325, 80, 20);
			graphOpt.setWrapToLine(true);
			graphOpt.setCursor(true);
			graphOpt.setBorder(true);
			graphOpt.setLineWidth(1);
			graphOpt.addActionListener();
			perLine = graphOpt.setNo(perLine, KE);
			screen = graphOpt.add(screen);

			label.print("Graph Width:");
			label.translate(WIDTH - 90 - label.getStringWidth() / 2, 365);
			screen = label.add(screen);
			KWrapper graphOpt1 = new KWrapper(WIDTH - 130, 390, 80, 20);
			graphOpt1.setWrapToLine(true);
			graphOpt1.setCursor(true);
			graphOpt1.setBorder(true);
			graphOpt1.setLineWidth(1);
			graphOpt1.addActionListener();
			gWidth = graphOpt1.setNo(gWidth, KE);
			screen = graphOpt1.add(screen);

			label.print("Graph Height:");
			label.translate(WIDTH - 90 - label.getStringWidth() / 2, 430);
			screen = label.add(screen);
			KWrapper graphOpt2 = new KWrapper(WIDTH - 130, 455, 80, 20);
			graphOpt2.setWrapToLine(true);
			graphOpt2.setCursor(true);
			graphOpt2.setBorder(true);
			graphOpt2.setLineWidth(1);
			graphOpt2.addActionListener();
			gHeight = graphOpt2.setNo(gHeight, KE);
			screen = graphOpt2.add(screen);

			KButton set = new KButton(WIDTH - 140, 495, 100, 25);
			set.setTitle("Set");
			set.addActionListener();
			screen = set.add(screen);

			border = new KLine(WIDTH - 150, 535, WIDTH - 30, 535);
			border.fill(new Color(KGraphics.getFontColorInt()));
			screen = border.add(screen);

			// export options
			sideBT.print("Export Options");
			sideBT.translate(WIDTH - 90 - sideBT.getStringWidth() / 2, 545);
			screen = sideBT.add(screen);

			String[] option1 = {
					flies[proDisplay.getSelected()][flyDisplay.getSelected()]
							.getGraphRatio() + " Pxls", "1:1 Pxls" };
			String[] option2 = { ".csv", ".xml" };

			if (exList1 == null) {
				exList1 = new KList(option1, WIDTH - 140, 575, 65, 45);
				exList2 = new KList(option2, WIDTH - 73, 575, 35, 45);
			}
			exList1.setLineWidth(1);
			exList1.addActionListener();
			screen = exList1.add(screen);
			exList2.setLineWidth(1);
			exList2.addActionListener();
			screen = exList2.add(screen);

			KButton export = new KButton(WIDTH - 140, 635, 100, 25);
			export.setTitle("Export");
			export.addActionListener();
			screen = export.add(screen);

			if (export.getStatus() == true) {
				export.setStatus(false);

				int opt1 = exList1.getSelected();
				int opt2 = exList2.getSelected();

				if (opt2 == 0) {

					File desktop = new File(
							System.getProperty("user.home")
									+ "/Desktop/"
									+ flies[proDisplay.getSelected()][flyDisplay
											.getSelected()].getTitle() + ".csv");
					Exporter ex = new Exporter(desktop);

					try {
						if (opt1 == 0)
							ex.csv(flies[proDisplay.getSelected()][flyDisplay
									.getSelected()], Adjust.CMPRSSD);
						if (opt1 == 1)
							ex.csv(flies[proDisplay.getSelected()][flyDisplay
									.getSelected()], Adjust.ALL);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				if (opt2 == 1) {

					File desktop = new File(
							System.getProperty("user.home")
									+ "/Desktop/"
									+ flies[proDisplay.getSelected()][flyDisplay
											.getSelected()].getTitle() + ".xml");
					Exporter ex = new Exporter(desktop);

					try {
						ex.xml(flies[proDisplay.getSelected()][flyDisplay
								.getSelected()]);
					} catch (JAXBException e) {
						e.printStackTrace();
					}
				}
			}

			if (set.getStatus() == true) {
				set.setStatus(false);
				// calculates graph width and perLine as well as sets graph
				// width,
				// height and line
				// continued by setting graph to null, thus causing the
				// graphwindow
				// to
				// reinitiate
				// the graphs with the updated graph dimensions as well as with
				// the
				// appropriate
				// compression ratio for graphData.
				try {
					if (Integer.valueOf(gHeight) < HEIGHT - 120) {
						graphHeight = Integer.valueOf(gHeight);
					}
				} catch (NumberFormatException e) {

				}

				try {
					if ((Integer.valueOf(gWidth) != graphWidth)
							&& (Integer.valueOf(gWidth) > 99)
							&& (Integer.valueOf(gWidth) < WIDTH / 4 * 3 + 10)) {
						graphWidth = Integer.valueOf(gWidth);
						pLine = (WIDTH / 4 * 3 + 10) / (graphWidth + 30);
						perLine = String.valueOf(pLine);
					}
				} catch (NumberFormatException e) {

				}

				try {
					if (((Integer.valueOf(perLine)) != pLine)
							&& (Integer.valueOf(perLine) > 0)
							&& (Integer.valueOf(perLine) < 6)) {
						pLine = Integer.valueOf(perLine);
						graphWidth = ((WIDTH / 4 * 3 + 10) - (31 * pLine))
								/ pLine;
						gWidth = String.valueOf(graphWidth);
					}
				} catch (NumberFormatException e) {

				}

				graph = null;
				exList1 = null;
			}

			if (KMouseEvent.mouseIn(WIDTH - 120, 140, 100, 100)) {
				selectComponents = true;
			}

			// select Components
			if (selectComponents == true) {

				background = false;
				graphWindow = false;

				KRect deselectRect = new KRect(WIDTH / 2 - 240,
						HEIGHT / 2 - 240, 480, 530);
				screen = deselectRect.add(screen);

				if (deselectImg == null) {
					deselectImg = new KImage(
							flies[proDisplay.getSelected()][flyDisplay.getSelected()]
									.getImg(), 440);
					deselectImg.translate(WIDTH / 2 - 220, HEIGHT / 2 - 200);
				}
				deselectImg.setComponents(components);
				screen = deselectImg.add(screen);

				if (descript == null) {
					descript = new KFont(KGraphics.getDefaultFont(), 20);
					descript.setFontColor(dColor);
					descript.print(WIDTH / 2 - 220, HEIGHT / 2 - 226,
							"Deselect graphs by clicking on AOI:");
				}
				screen = descript.add(screen);

				if (KMouseEvent.released()) {
					components = deselectImg.deselect(KMouseEvent.mousePosX(),
							KMouseEvent.mousePosY());
				}

				reset.setTitle("Reset");
				reset.translate(WIDTH / 2 - 220, HEIGHT / 2 + 245);
				reset.setDimensions(440, 25);
				screen = reset.add(screen);

				// Exit

				if ((KMouseEvent.pressed() == true)
						&& (KMouseEvent.mouseIn(WIDTH / 2 - 240,
								HEIGHT / 2 - 240, 480, 530) == false)) {
					deselected = deselectImg.getDeselect();
					selectComponents = false;
					graph = null;
					exList1 = null;
					deselectImg = null;
					background = true;
					graphWindow = true;
				}

			}

			reset.addActionListener();
			screen = reset.add(screen);

			// Reset components
			if (reset.getStatus() == true) {
				reset.setStatus(false);
				// deselectImg.resetDeselect();
				deselected = new int[0];
				if (deselectImg != null)
					deselectImg.resetDeselect();
				components = flies[proDisplay.getSelected()][flyDisplay
						.getSelected()].getComponents();
				graph = null;
			}
		}
	}

	public void progressBar() {
		if (progBarStatus > 0) {
			if (progBarStatus < progBarMax) {
				progBarStatus++;
				int percentage = (int) ((float) (progBarStatus) / WIDTH * 100);
				percent.print(String.valueOf(percentage) + "%");
				percent.translate(WIDTH - 20 - percent.getStringWidth(), 10);
				progBar.fill(new Color(0, 200, 0));
				progBar.translate(0, menuH + 2, progBarStatus, menuH + 2);
				progBar.setLineWidth(5);
			}
			screen = progBar.add(screen);
			screen = percent.add(screen);

			if (progBarStatus >= WIDTH - 1) {
				progBarStatus = 0;
			}

		}
	}

	public void importAll() {
		if (importAll == true) {

			submList = null;
			images = null;
			flyDisplay.reset();
			flyDisplay.highlight(-1);
			graphWindow = false;
			flyWindow = false;
			projectWindow = false;
			progBarStatus = 0;

			// Title
			KFont title = new KFont(KGraphics.getDefaultFont(), 40);
			title.print(20, 10, "Import All");
			screen = title.add(screen);
			menuH = title.getStringHeight() + 10;
			KLine line = new KLine(0, menuH, WIDTH,
					10 + title.getStringHeight());
			screen = line.add(screen);

			if (imp == null) {
				imp = new DmelanoNMJImporter[1];
			}

			if ((imp[0] == null) && (inputIt < noOfAllJobs)) {
				imp[0] = new DmelanoNMJImporter(fileNames[inputIt],
						imgNames[inputIt], projects[proDisplay.getSelected()],
						inputIt % 3);
				imp[0].setIdentity(newProName + String.valueOf(inputIt + 1),
						newProNotes);
				imp[0].setBaseLineInterval(baseLineInterval);
				imp[0].addFly();
				inputIt++;
			}

			if (imp[0] != null)
				run1Max = (int) (360f / 100 * imp[0].getProgress());
			if (run1Max > 340)
				run1Max = 361;

			boolean job1 = true;
			boolean exit = false;

			if (noOfJobs < 1) {
				exit = true;
				imp = null;
				newProName = "";
				newProNotes = "";
			}

			// start
			if ((job1 == true) && (run1 <= run1Max)) {
				run1++;
				runBar++;
			}

			// job done
			if (run1 > 360) {
				run1 = 0;
				noOfJobs--;
				imp[0] = null;
			}

			int runAll = 1;
			float wi = (float) (WIDTH - 200) / (noOfAllJobs * 360);
			runAll = (int) (wi * runBar);

			KRect status = new KRect(100, HEIGHT / 2 - 15, runAll, 30);
			status.fill(new Color(0, 200, 0));
			screen = status.add(screen);

			KCircle circ1 = new KCircle(WIDTH / 2, HEIGHT / 2, 170);

			if (job1 == true) {
				circ1.setRender(6);
				circ1.setLineWidth(15);
				circ1.setAngle(0, run1);
				screen = circ1.add(screen);
			}

			KFont smPercent = new KFont(KGraphics.getDefaultFont(), 40);
			if (job1 == true) {
				smPercent
						.print(String.valueOf((int) (100f / 360 * run1)) + "%");
				smPercent.translate(WIDTH / 2 - smPercent.getStringWidth() / 2,
						HEIGHT / 2 - 240);
				smPercent.setFontColor(dColor);
				screen = smPercent.add(screen);
			}

			KFont laPercent = new KFont(KGraphics.getDefaultFont(), 100);
			laPercent.print(String
					.valueOf((int) (100f / (WIDTH - 200) * runAll)) + "%");
			laPercent.translate(WIDTH / 2 - laPercent.getStringWidth() / 2,
					HEIGHT / 2 - laPercent.getStringHeight() / 2);
			screen = laPercent.add(screen);

			if (imp != null) {
				if (imp[0] != null) {
					KFont updateString = new KFont();
					updateString.print(imp[0].getUpdate());
					updateString.translate(
							WIDTH / 2 - updateString.getStringWidth() / 2,
							HEIGHT / 2 + 200);
					screen = updateString.add(screen);
				}
			}

			if (exit == true) {
				importAll = false;
				startUp = true;
				loadContent = true;
				tickCount = 0;
				addAll.setStatus(false);

				run1 = 0;
				runBar = 0;
			}
		}
	}

	// BACKGROUND OPERATIONS
	private void loadContent() {

		loadContent = false;

		// projects
		File removeDS = new File(DmelanoNMJ.projectPath + "/.DS_Store");
		removeDS.delete();
		File projectFolder = new File(proPath);
		File[] fList = projectFolder.listFiles();
		projects = new String[fList.length];
		pFlyNo = new String[fList.length];
		for (int i = 0; i < fList.length; i++) {
			projects[i] = fList[i].getName();
		}
		flies = new Fly[projects.length][];
		proNotes = new String[projects.length];

		// files of project folder and import flies
		for (int i = 0; i < projects.length; i++) {
			removeDS = new File(DmelanoNMJ.projectPath + "/.DS_Store");
			removeDS.delete();
			projectFolder = new File(proPath + "/" + projects[i] + "/");
			fList = projectFolder.listFiles();
			int positiveFly = 0;
			for (File f : fList) {
				if ((f.getPath()).contains(".fly"))
					positiveFly++;
			}
			flies[i] = new Fly[positiveFly];
			int flyNo = 0;
			for (int j = 0; j < fList.length; j++) {
				if ((fList[j].getPath()).contains(".fly")) {
					flyNo++;
					Importer imp = new Importer(fList[j]);
					try {
						flies[i][flyNo - 1] = imp.load();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if ((fList[j].getName()).equals("_PROJECT.txt")) {
					try {
						@SuppressWarnings("resource")
						BufferedReader reader = new BufferedReader(
								new FileReader(fList[j]));
						try {
							proNotes[i] = reader.readLine();
						} catch (IOException e) {
							e.printStackTrace();
						}

					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
			pFlyNo[i] = String.valueOf(flyNo);
		}
	}

	public void remove(int selected, String opt) {

		if (opt.equals("PROJECT")) {
			File file = new File(proPath + "/" + projects[selected]);
			try {
				delete(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
			proDisplay.highlight(-1);

			String[] inter = new String[projects.length - 1];
			for (int i = 0; i < selected; i++) {
				inter[i] = projects[i];
			}
			for (int i = selected; i < inter.length; i++) {
				inter[i] = projects[i + 1];
			}
			projects = inter;
			proDisplay.setFiles(projects);

			inter = new String[proNotes.length - 1];
			for (int i = 0; i < selected; i++) {
				inter[i] = proNotes[i];
			}
			for (int i = selected; i < inter.length; i++) {
				inter[i] = proNotes[i + 1];
			}
			proNotes = inter;

			inter = new String[pFlyNo.length - 1];
			for (int i = 0; i < selected; i++) {
				inter[i] = pFlyNo[i];
			}
			for (int i = selected; i < inter.length; i++) {
				inter[i] = pFlyNo[i + 1];
			}
			pFlyNo = inter;
			proDisplay.setCaption(pFlyNo);

			Fly[][] inter1 = new Fly[flies.length - 1][];
			for (int i = 0; i < selected; i++) {
				inter1[i] = flies[i];
			}
			for (int i = selected; i < inter1.length; i++) {
				inter1[i] = flies[i + 1];
			}
			flies = inter1;
		}

		if (opt.equals("FLY")) {

			File fileFly = new File(proPath + "/"
					+ projects[proDisplay.getSelected()] + "/"
					+ flies[proDisplay.getSelected()][selected].getTitle()
					+ ".fly");
			File fileImg = new File(proPath + "/"
					+ projects[proDisplay.getSelected()] + "/"
					+ flies[proDisplay.getSelected()][selected].getTitle()
					+ ".jpg");
			try {
				delete(fileFly);
				delete(fileImg);
			} catch (IOException e) {
				e.printStackTrace();
			}

			int pNo = Integer.parseInt(pFlyNo[proDisplay.getSelected()]);
			pFlyNo[proDisplay.getSelected()] = String.valueOf(pNo - 1);
			proDisplay.setCaption(pFlyNo);

			Fly[] inter = new Fly[flies[proDisplay.getSelected()].length - 1];
			for (int i = 0; i < selected; i++) {
				inter[i] = flies[proDisplay.getSelected()][i];
			}
			for (int i = selected; i < inter.length; i++) {
				inter[i] = flies[proDisplay.getSelected()][i + 1];
			}
			flies[proDisplay.getSelected()] = inter;

			flyDisplay.reset();
			flyDisplay.highlight(-1);
			images = null;
		}
	}

	public static void delete(File file) throws IOException {

		if (file.isDirectory()) {

			// directory is empty, then delete it
			if (file.list().length == 0) {

				file.delete();
				System.out.println("Directory is deleted : "
						+ file.getAbsolutePath());

			} else {

				// list all the directory contents
				String files[] = file.list();

				for (String temp : files) {
					// construct the file structure
					File fileDelete = new File(file, temp);

					// recursive delete
					delete(fileDelete);
				}

				// check the directory again, if empty then delete it
				if (file.list().length == 0) {
					file.delete();
					System.out.println("Directory is deleted : "
							+ file.getAbsolutePath());
				}
			}

		} else {
			// if file, then delete it
			file.delete();
			System.out.println("File is deleted : " + file.getAbsolutePath());
		}
	}

	public void addProject(String name, String notes) {
		File file = new File(proPath + "/" + name + "/");
		file.mkdir();

		String[] inter = new String[projects.length + 1];
		for (int i = 0; i < projects.length; i++) {
			inter[i] = projects[i];
		}
		inter[inter.length - 1] = name;
		projects = inter;
		proDisplay.setFiles(projects);

		Fly[][] flyInter = new Fly[flies.length + 1][];
		for (int i = 0; i < flies.length; i++) {
			if (flies[i] != null) {
				flyInter[i] = new Fly[flies[i].length];
				for (int j = 0; j < flies[i].length; j++) {
					flyInter[i][j] = flies[i][j];
				}
			}
		}
		flies = flyInter;
		flies[flies.length - 1] = null;

		String[] flyNoInter = new String[pFlyNo.length + 1];
		for (int i = 0; i < pFlyNo.length; i++) {
			flyNoInter[i] = pFlyNo[i];
		}
		flyNoInter[flyNoInter.length - 1] = String.valueOf(0);
		pFlyNo = flyNoInter;
		proDisplay.setCaption(pFlyNo);

		String[] noteInter = new String[proNotes.length + 1];
		for (int i = 0; i < proNotes.length; i++) {
			noteInter[i] = proNotes[i];
		}
		noteInter[noteInter.length - 1] = notes;
		proNotes = noteInter;

		writeToFile(notes,
				new File(proPath + "/" + name + "/" + "_PROJECT.txt"));
	}

	public void writeToFile(String text, File file) {
		File fil = file;
		try {
			fil.createNewFile();
		} catch (IOException e2) {
		}
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(fil));
			if (text != null)
				writer.write(text);
			writer.flush();
			writer.close();
		} catch (IOException e) {

		}
		selTemp = proDisplay.getSelected();
	}

	private void analyseFly() {

		if (analyseFly == true) {

			final int interSel = proDisplay.getSelected();
			graphAllowed = false;
			graphWindow = false;
			flyWindow = true;
			flyDisplay.highlight(-1);

			if (imp == null) {
				imp = new DmelanoNMJImporter[1];
				imp[0] = new DmelanoNMJImporter(fileName, imgName,
						projects[interSel]);
				imp[0].setIdentity(newProName, newProNotes);
				imp[0].setBaseLineInterval(baseLineInterval);
				imp[0].addFly();
			}

			newProName = "";
			newProNotes = "";

			if (imp[0].getComplete() == false) {
				progBarMax = WIDTH / 100 * imp[0].getProgress();
			}

			if (imp[0].getComplete()) {

				imp = null;
				proDisplay.highlight(-1);
				flyDisplay.highlight(-1);
				projectWindow = true;
				flyWindow = false;
				graphWindow = false;

				loadContent();

				progBarMax = WIDTH / 100 * 98;

				pFlyNo[interSel] = String.valueOf(flies[interSel].length);
				proDisplay.setCaption(pFlyNo);
				flyDisplay.reset();

				System.out.println("THREAD: Analysis complete.");
				graphAllowed = true;
				analyseFly = false;
				progBarMax = WIDTH;

			}

		}
	}

	// SETTERS
	public static void setProgBar(int s) {
		progBarMax += WIDTH / 100 * s;
	}

	// GETTERS

	public File getTxtFile() {
		return txtFile;
	}

	public File getImgFile() {
		return imgFile;
	}

}
