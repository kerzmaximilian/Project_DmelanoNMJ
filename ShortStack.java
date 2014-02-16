package Util;

public class ShortStack extends Thread {

	private short[][] shortArray;
	private volatile short[][] adjustArray;
	private int[][] components;
	private volatile int[][] compList;
	private boolean trueValue = false;
	private boolean baseline = false;
	private int resX;
	private int resY;
	private int pix;
	private int size;
	private int bLength = 3600;
	private int bLPer = 10;
	private int compPix;
	private volatile int setter = 0;
	private volatile int percent = 0;
	private volatile int div;
	private volatile int remain;
	private volatile int multi;
	private volatile int threadNo = 6;
	private volatile int threadFin = 0;

	private Thread[] threads;

	public ShortStack(int size, int resX, int resY) {
		this.resX = resX;
		this.resY = resY;
		this.pix = resY * resX;
		this.size = size;
		if (size < bLength)
			setBaseInterval(600);
		this.shortArray = new short[size][];
		this.adjustArray = new short[size][];
	}

	public void fill(int imgNo, short[] pixVal) {
		if (pix != pixVal.length) {
			System.err
					.println("ShortStack: ShortArray parsed does not have the required length.");
		} else {
			if ((imgNo < size) && (imgNo > -1)) {
				shortArray[imgNo] = pixVal;
			} else {
				System.err
						.println("ShortStack: OutOfBounceException: " + imgNo);
			}
		}
	}

	public void adjust(Adjust adj) {

		adjustArray = shortArray;

		if (adj == Adjust.ALL) {

		}

		if ((adj == Adjust.ACC) && (checkComp() == true)) {

			div = compList.length / threadNo;
			remain = compList.length % threadNo;
			threads = new Thread[threadNo - 1];

			for (int i = 0; i < threadNo - 1; i++) {
				threads[i] = new Thread(new Runnable() {
					public void run() {
						adjustAlgr("THREAD");
					}
				});
			}

			for (int i = 0; i < threadNo - 1; i++) {
				multi = i * div;
				threads[i].start();
			}

			adjustAlgr("MAIN");

			if (remain > 0) {
				adjustAlgr("REMAIN");
			}

			for (int i = 0; i < threadNo - 1; i++) {
				try {
					threads[i].join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			baseline = true;
			
			System.out.println("SHORTSTACK: Pixel Values adjusted.");
		} else {
			System.err
					.println("ShortStack: Unable to perform task, due to lack of components.");
		}
	}

	// others

	private void adjustAlgr(String opt) {

		boolean thread = false;
		boolean main = false;
		boolean remain = false;
		int itter = div;
		int adjustL = adjustArray.length;
		int indet = 0;
		int mainMulti = div * (threadNo - 1);
		int mainRemain = compList.length - 1 - this.remain;

		if (opt.equals("THREAD"))
			thread = true;
		if (opt.equals("MAIN"))
			main = true;
		if (opt.equals("REMAIN")) {
			remain = true;
			itter = this.remain;
		}

		for (int i = 0; i < itter; i++) {

			if (thread == true)
				indet = i + multi;
			if (main == true)
				indet = i + mainMulti;
			if (remain == true)
				indet = i + mainRemain;

			int pixel = compList[indet][0] * 31 + compList[indet][1];
			short[] pixLine = new short[adjustL];
			// fill pixLine with the same pixel of all
			// images
			for (int k = 0; k < adjustL; k++) {
				pixLine[k] = adjustArray[k][pixel];
			}

			RollingMedian rm = new RollingMedian(bLength);
			// populate
			for (int j = 0; j < bLength; j++) {
				short pix = pixLine[j];
				if (pix < 0)
					pix = (short) -pix;
				rm.insert(pix);
			}

			for (int k = bLength / 2 + 1; k < adjustL - (bLength / 2 - 1); k++) {
				short pix = pixLine[k];
				if (pix < 0)
					pix = (short) -pix;
				rm.insert(pix);
				//get 100/bLPer element
				short bline = (short) rm.getElement((int)(bLength*100/bLPer));
				short aArray = adjustArray[k][i];
				if(aArray <0)
					aArray =(short) -aArray;
				adjustArray[k][pixel] = (short) (aArray - bline);
			}

			setter++;
			percent = (int) ((100f / compPix) * setter);
		}
		threadFin++;
	}

	private boolean checkComp() {
		boolean checkComp = false;
		if (components != null) {
			checkComp = true;
		} else {
			System.err.println("ShortStack: Components were not set.");
		}
		return checkComp;
	}

	public boolean checkThreads() {
		boolean threadEnd = false;
		if (threadNo == threadFin) {
			threadEnd = true;
		}
		return threadEnd;

	}

	// getters

	public short[] getImg(int imgNo) {
		if ((imgNo < size) && (imgNo > -1)) {
			if ((trueValue == false) && (baseline == true)) {
				return adjustArray[imgNo];
			} else {
				return shortArray[imgNo];
			}
		} else {
			System.err.println("ShortStack: OutOfBounceException: " + imgNo);
			return null;
		}
	}

	@SuppressWarnings("null")
	public short getPixVal(int imgNo, int pixNo) {
		if ((imgNo < size) && (imgNo > -1) && (pixNo < pix) && (pixNo > -1)) {
			if ((trueValue == false) && (baseline == true)) {
				return adjustArray[imgNo][pixNo];
			} else {
				return shortArray[imgNo][pixNo];
			}
		} else {
			System.err.println("ShortStack: OutOfBounceException: " + imgNo
					+ ", " + pixNo);
			return (Short) null;
		}
	}

	@SuppressWarnings("null")
	public short getPixVal(int imgNo, int coY, int coX) {
		if ((imgNo < size) && (imgNo > -1) && (coX < resX) && (coY < resY)) {
			if ((trueValue == false) && (baseline == true)) {
				return adjustArray[imgNo][coX * resY + coY];
			} else {
				return shortArray[imgNo][coX * resY + coY];
			}

		} else {
			System.err.println("ShortStack: OutOfBounceException: " + imgNo
					+ ", " + coX + ", " + coY);
			return (Short) null;
		}
	}

	public short[][] getGraphVals() {
		if (baseline == true) {

			short[][] graphVals = new short[compList.length][adjustArray.length
					- bLength];

			for (int i = 0; i < graphVals.length; i++) {
				int pixel = compList[i][0] * 31 + compList[i][1];
				for (int j = bLength / 2; j < adjustArray.length
						- (bLength / 2)-1; j++) {
					graphVals[i][j - bLength / 2] = adjustArray[j][pixel];
				}
			}

			return graphVals;
		}
		return null;
	}

	public short[][] getTrueValsComp() {
		if (checkComp() == true) {

			short[][] shortVals = new short[compList.length][shortArray.length
					- bLength];

			for (int i = 0; i < shortVals.length; i++) {
				int pixel = compList[i][0] * 31 + compList[i][1];
				for (int j = bLength / 2; j < shortVals[i].length
						- (bLength / 2 - 1); j++) {
					shortVals[i][j - bLength / 2] = shortArray[j][pixel];
				}
			}

			return shortVals;
		}
		return null;
	}

	public int size() {
		return size;
	}

	public int imgSize() {
		return pix;
	}

	public int getStatus() {
		// in percentage
		return percent;
	}
	
	public int getBaseInterval(){
		return bLength;
	}

	// setters
	public void setReturnTrueValue(boolean x) {
		trueValue = x;
	}

	public void setBaseInterval(int length) {
		// must be odd (for median selection)
		int length2 = length / 2;
		length2 = length2 * 2 - 1;
		bLength = length2;
	}

	public void setComponents(int[][] comp) {
		this.components = comp;
		// no of pix
		int z = 0;
		for (int i = 0; i < comp.length; i++) {
			z += comp[i].length / 2;
		}
		compPix = z;
		compList = new int[compPix][2];
		// fill compList
		int set = 0;
		for (int i = 0; i < components.length; i++) {
			for (int j = 0; j < components[i].length / 2; j++) {
				compList[set][0] = components[i][j * 2];
				compList[set][1] = components[i][j * 2 + 1];
				set++;
			}
		}
	}

	public void setThreads(int threads) {
		this.threadNo = threads;
	}
	
	public void setBaseIntervalPercentage(int x){
		bLPer = x;
	}

}
