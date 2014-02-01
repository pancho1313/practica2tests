package features;

import myutil.MyUtil;
import windowdata.IWindowData;
import features.fft.*;


/**
 * input:
 *     length of linearAcceleration vector
 * features:
 *    mean of row data
 *    standar deviation of row data
 *    mean of FFT
 *    standar deviation of FFT
 */
public class MyFeatures1 implements IFeatures {
	
	public final static int FEATURES_TYPE = 1;
	public final static int FLOATS_PER_WINDOW_DATA = 1;
	
	public float[] getFeatures(IWindowData windowData){
		float[] data = windowData.getData(0);
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
	
	/**
	 * FFTmag explained in http://www.ict.griffith.edu.au/~vlad/PhDthesis/joanne_thesis_final.pdf
	 * size of windowdata must be a power of 2, uses features.fft.InplaceFFT
	 * Cooley-Tukey FFT non recursive O(n log n) http://introcs.cs.princeton.edu/java/97data/InplaceFFT.java.html
	 * @param data
	 * @return
	 */
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
	
	public static float[] getDataForWindowData(float[] linearAccel){
		float[] data = new float[]{(float)MyUtil.vecLength(linearAccel)};
		return data;
	}
}
