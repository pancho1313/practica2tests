package features;

import activityrecognition.SvmRecognizerIntentService;
import android.util.Log;
import myutil.MyUtil;

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
	
	public int getFeaturesType(){
		return FEATURES_TYPE;
	}
	
	public float[] getFeatures(IWindowData windowData){
		float[] data = windowData.getData(0);
		float mean = mean(data);
		float stdev = stDev(mean, data);
		
		float[] fftMag = getFFTMag(data);
		float meanFftMag = mean(fftMag);
		float stDevFftMag = stDev(meanFftMag, fftMag);
		
		// prev state
		float bikeNotMoving=0, bikeCruise=0, bikeAccelerating=0, bikeBreaking=0;
		float carNotMoving=0, carCruise=0, carAccelerating=0, carBreaking=0;
		float prev = (windowData.getData(1)[0]);
		int prevState = (int)prev;
		float prevProb = prev - prevState;
		if(prevProb<0.1f)
			prevProb = 1;
		if(prevState == SvmRecognizerIntentService.BIKE_NOT_MOVING){
			bikeNotMoving = prevProb;
		}else if(prevState == SvmRecognizerIntentService.BIKE_CRUISE){
			bikeCruise = prevProb;
		}else if(prevState == SvmRecognizerIntentService.BIKE_ACCELERATING){
			bikeAccelerating = prevProb;
		}else if(prevState == SvmRecognizerIntentService.BIKE_BREAKING){
			bikeBreaking = prevProb;
		}
		
		else if(prevState == SvmRecognizerIntentService.CAR_NOT_MOVING){
			carNotMoving = prevProb;
		}else if(prevState == SvmRecognizerIntentService.CAR_CRUISE){
			carCruise = prevProb;
		}else if(prevState == SvmRecognizerIntentService.CAR_ACCELERATING){
			carAccelerating = prevProb;
		}else if(prevState == SvmRecognizerIntentService.CAR_BREAKING){
			carBreaking = prevProb;
		}
		
		else{ // eg: prevState < 0
			bikeNotMoving = prevProb;
			carNotMoving = prevProb;
		}
		
		return new float[]{
				mean, stdev, meanFftMag, stDevFftMag,
				bikeNotMoving, bikeCruise, bikeAccelerating, bikeBreaking,
				carNotMoving, carCruise, carAccelerating, carBreaking};
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
	
	public static float[] getDataForWindowData(float[] linearAccel, float prevState){
		
		float[] data = new float[]{(float)MyUtil.vecLength(linearAccel), prevState};
		return data;
	}
}
