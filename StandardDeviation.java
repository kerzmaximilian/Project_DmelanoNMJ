package Util;

public class StandardDeviation {
	private float[] sample;
	private float sum=0;
	private static float MEAN=0;
	private static float SD;
	private static float sdInter[];
	
	public StandardDeviation(){
	}
	
	public StandardDeviation (float[] sample) {
		this.sample=sample;
		for (int i=0; i<this.sample.length; i++){
			sum+=this.sample[i];
		}
		MEAN=(float)sum/this.sample.length;
		sum=0;
		
		sdInter= new float[this.sample.length];
		for(int i=0; i<this.sample.length;i++ ){
		sdInter[i]=(float)((MEAN-this.sample[i])*(MEAN-this.sample[i]));
		sum+=sdInter[i];
		}
		SD=(float) Math.sqrt((double)(sum/(this.sample.length-1)));
		sum=0;
	}
	
	public float getSD(){
		return SD;
	}
	
	public float getMean(){
		return MEAN;
	}
	
	public int getZScore(float dataPoint){
		int zScore=0;
		
		//rounded
		zScore=(int)((MEAN-dataPoint)/(SD*0.2));
	
		return zScore;
	}	



}
