package features;

import windowdata.IWindowData;

public class MyFeatures implements IFeatures {
	
	public float[] getFeatures(IWindowData windowData){
		float[] data = windowData.getData();
		float mean = mean(data);
		float stdev = stDev(mean, data);
		
		return new float[]{mean, stdev};
	}
	
	private float stDev(float mean, float[] data){
		double sum = 0;
		for(float d : data){
			sum += Math.pow(d - mean, 2);
		}
		return (float) Math.sqrt(sum/mean);
	}
	
	private float mean(float[] data){
		float sum = 0;
		for(float d : data){
			sum += d;
		}
		return sum/data.length;
	}
}