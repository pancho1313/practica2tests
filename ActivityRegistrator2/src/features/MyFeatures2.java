package features;

import windowdata.IWindowData;
import features.fft.*;


/**
 * input:
 *     length of horizontal (lAccH = lAcc[0]) and vertical (lAccV = lAcc[1]) linear acceleration (global)
 * features:
 *    mean of lAccH
 *    standar deviation of lAccH
 *    mean of FFT(lAccH)
 *    standar deviation of FFT(lAccH)
 *    mean of lAccV
 *    standar deviation of lAccV
 *    mean of FFT(lAccV)
 *    standar deviation of FFT(lAccV)
 */
public class MyFeatures2 implements IFeatures {
	
	public final static int FEATURES_TYPE = 2;
	public final static int FLOATS_PER_WINDOW_DATA = 2;
	
	public float[] getFeatures(IWindowData windowData){
		
		float[] lAccH = windowData.getData(0);
		float meanLAccH = mean(lAccH);
		float stdevLAccH = stDev(meanLAccH, lAccH);
		
		float[] fftMagLAccH = getFFTMag(lAccH);
		float meanFftMagLAccH = mean(fftMagLAccH);
		float stDevFftMagLAccH = stDev(meanFftMagLAccH, fftMagLAccH);
		
		
		float[] lAccV = windowData.getData(1);
		float meanLAccV = mean(lAccV);
		float stdevLAccV = stDev(meanLAccV, lAccV);
		
		float[] fftMagLAccV = getFFTMag(lAccV);
		float meanFftMagLAccV = mean(fftMagLAccV);
		float stDevFftMagLAccV = stDev(meanFftMagLAccV, fftMagLAccV);
		
		return new float[]{
				meanLAccH, stdevLAccH, meanFftMagLAccH, stDevFftMagLAccH,
				meanLAccV, stdevLAccV, meanFftMagLAccV, stDevFftMagLAccV};
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
