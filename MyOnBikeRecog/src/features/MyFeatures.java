package features;

import windowdata.IWindowData;
import features.fft.*;

public class MyFeatures implements IFeatures {
	
	public float[] getFeatures(IWindowData windowData){
		float[] data = windowData.getData();
		float mean = mean(data);
		float stdev = stDev(mean, data);
		
		float[] fftMag = getFFTMag(data);
		float meanFftMag = mean(fftMag);
		float stDevFftMag = stDev(meanFftMag, fftMag);
		
		return new float[]{mean, stdev, meanFftMag, stDevFftMag};
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
	
	private float[] getFFTMag(float[] data){
		Complex[] x = new Complex[data.length];
		for(int i = 0; i < x.length; i++){
			x[i] = new Complex(data[i],0);
		}
		
		float[] fftMag = new float[(x.length/2)+1];
		
		InplaceFFT.fft(x);
        for (int i = 0; i < fftMag.length; i++)
        	fftMag[i] = (float) x[i].conjugate().times(x[i]).re();
        
        return fftMag;
	}
}
