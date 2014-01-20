package features;

import com.example.activityregistrator.ActivityRegistrator;

import windowdata.IWindowData;
import features.fft.*;


/**
 * input:
 *     length of linearAcceleration vector [0] and prev state[1]
 * features:
 *    mean of row data
 *    standar deviation of row data
 *    mean of FFT
 *    standar deviation of FFT
 *    prev state {NOT_MOVING, CRUISE, ACCELERATING, BREAKING}
 */
public class MyFeatures3 implements IFeatures {
	
	public final static int FEATURES_TYPE = 3;
	public final static int FLOATS_PER_WINDOW_DATA = 2;
	
	public float[] getFeatures(IWindowData windowData){
		float[] data = windowData.getData(0);
		float mean = mean(data);
		float stdev = stDev(mean, data);
		
		float[] fftMag = getFFTMag(data);
		float meanFftMag = mean(fftMag);
		float stDevFftMag = stDev(meanFftMag, fftMag);
		
		// prev state
		float notMoving=0, cruise=0, accelerating=0, breaking=0;
		int prevState = (int)(windowData.getData(1)[0]);
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
				mean, stdev, meanFftMag, stDevFftMag,
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
