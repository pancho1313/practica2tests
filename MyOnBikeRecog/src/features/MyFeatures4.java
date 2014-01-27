package features;

import myutil.MyUtil;

import activityrecognition.SvmRecognizerIntentServiceInterface;


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
	
	public int getFeaturesType(){
		return FEATURES_TYPE;
	}
	
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
		if(prevState == SvmRecognizerIntentServiceInterface.NOT_MOVING){
			notMoving = 1;
		}else if(prevState == SvmRecognizerIntentServiceInterface.CRUISE){
			cruise = 1;
		}else if(prevState == SvmRecognizerIntentServiceInterface.ACCELERATING){
			accelerating = 1;
		}else if(prevState == SvmRecognizerIntentServiceInterface.BREAKING){
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
	
	public static float[] getDataForWindowData(float[] linearAccel, float[] gData, float prevState){
		//float[] gData = {scanSD.nextFloat(), scanSD.nextFloat(), scanSD.nextFloat()};
		
		// get the length horizontal and vertical (global) components of linear acceleration
		float[] lAccHorizontal = new float[3];
		float[] lAccVertical = new float[3];
		
			// scale factor for gData
		double dividend = (Math.pow(gData[0], 2) + Math.pow(gData[1], 2) + Math.pow(gData[2], 2));
		if(dividend == 0){
			dividend = 0.0000001;
		}
		float c = (float) (-1 * (
				(gData[0]*linearAccel[0] + gData[1]*linearAccel[1] + gData[2]*linearAccel[2])
				/
				dividend
				));
		
		lAccVertical[0] = gData[0] * c;
		lAccVertical[1] = gData[1] * c;
		lAccVertical[2] = gData[2] * c;
		
		lAccHorizontal[0] = lAccVertical[0] + linearAccel[0];
		lAccHorizontal[1] = lAccVertical[1] + linearAccel[1];
		lAccHorizontal[2] = lAccVertical[2] + linearAccel[2];
		
		float[] data = new float[]{
				(float)MyUtil.vecLength(lAccHorizontal),
				(float)MyUtil.vecLength(lAccVertical),
				prevState
				};
		
		return data;
	}
}
