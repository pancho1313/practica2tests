package features;

import com.example.activityregistrator.ActivityRegistrator;

import windowdata.IWindowData;
import features.fft.*;


/**
 * input:
 *     length of
 *         horizontal (lAccH = lAcc[0]) linear acceleration (global)
 *         vertical (lAccV = lAcc[1]) linear acceleration (global)
 *     prev. state
 * features:
 *    mean of lAccH
 *    standar deviation of lAccH
 *    mean of FFT(lAccH)
 *    standar deviation of FFT(lAccH)
 *    mean of lAccV
 *    standar deviation of lAccV
 *    mean of FFT(lAccV)
 *    standar deviation of FFT(lAccV)
 *    prev state {NOT_MOVING, CRUISE, ACCELERATING, BREAKING}
 */
public class MyFeatures4 implements IFeatures {
	
	public final static int FEATURES_TYPE = 4;
	public final static int FLOATS_PER_WINDOW_DATA = 3;
	
	public float[] getFeatures(IWindowData windowData){
		
		// horizontal linear acceleration
		float[] lAccH = windowData.getData(0);
		float meanLAccH = mean(lAccH);
		float stdevLAccH = stDev(meanLAccH, lAccH);
		
		float[] fftMagLAccH = getFFTMag(lAccH);
		float meanFftMagLAccH = mean(fftMagLAccH);
		float stDevFftMagLAccH = stDev(meanFftMagLAccH, fftMagLAccH);
		
		// vertical linear acceleration
		float[] lAccV = windowData.getData(1);
		float meanLAccV = mean(lAccV);
		float stdevLAccV = stDev(meanLAccV, lAccV);
		
		float[] fftMagLAccV = getFFTMag(lAccV);
		float meanFftMagLAccV = mean(fftMagLAccV);
		float stDevFftMagLAccV = stDev(meanFftMagLAccV, fftMagLAccV);
		
		// prev state
		float notMoving=0, cruise=0, accelerating=0, breaking=0;
		int prevState = (int)(windowData.getData(2)[0]);
		if(prevState == ActivityRegistrator.NOT_MOVING){
			notMoving = 1;
		}else if(prevState == ActivityRegistrator.CRUISE){
			cruise = 1;
		}else if(prevState == ActivityRegistrator.ACCELERATING){
			accelerating = 1;
		}else if(prevState == ActivityRegistrator.BREAKING){
			breaking = 1;
		}
		
		return new float[]{
				meanLAccH, stdevLAccH, meanFftMagLAccH, stDevFftMagLAccH,
				meanLAccV, stdevLAccV, meanFftMagLAccV, stDevFftMagLAccV,
				notMoving, cruise, accelerating, breaking};
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
