package activityrecognition;

import android.content.Context;
import android.util.Log;

/**
 * implements libsvm scale and predict using androidjni-libsvm api
 * @author job
 *
 */
public class SvmRecognizer implements IActivityRecognizer {

	private static String TAG = "SvmRecognizer";
	
    float scaleZero;
	float scaleDif;
	float[] featuresZero;
	float[] featuresDif;
	
    public SvmRecognizer(float scaleH, float scaleL, float[] featuresMin, float[] featuresMax, Context context){
        preCalculateScaleFeatures(scaleH, scaleL, featuresMin, featuresMax);
    }
	
    /**
     * set parameters to scale features before predict
     * @param scaleH
     * @param scaleL
     * @param featuresMin
     * @param featuresMax
     */
	private void preCalculateScaleFeatures(float scaleH, float scaleL, float[] featuresMin, float[] featuresMax){
		// pre-calculate data
		scaleZero = (scaleH + scaleL)/2;
		scaleDif = scaleH - scaleL;
		featuresZero = new float[featuresMax.length];
		featuresDif = new float[featuresMax.length];
		for(int i = 0; i < featuresMax.length; i++){
			featuresZero[i] = (featuresMax[i] + featuresMin[i])/2;
			featuresDif[i] = featuresMax[i] - featuresMin[i];
		}
	}
	
	/**
	 * Scale features using pre-calculated parameters before predict.
	 * Is necessary to perform the scale in the same way done when training libsvm,
	 * Libsvm scales test features reading the .range file generated when scaling the training file and generating the .model file.
	 * Here we do something similar reading the assets/range.128.4.properties file.
	 * The .range file must match with the type of feature extraction set in SvmBicycleCarRecognizer
	 * @param features
	 */
	private void scaleFeatures(float[] features){
		for(int i = 0; i < features.length; i++){
			features[i] = scaleZero + ((features[i] - featuresZero[i]) * scaleDif)/featuresDif[i];
		}
	}
	
	//////////////////////////////////////////////////////////////////////
	/*
	 * libsvm android-jni
	 * https://github.com/cnbuff410/Libsvm-androidjni
	 * */
	
	// svm native
    private native int trainClassifierNative(String trainingFile, int kernelType,
    		int cost, float gamma, int isProb, String modelFile);
    private native int doClassificationNative(float values[][], int indices[][],
    		int isProb, String modelFile, int labels[], double probs[]);
    
    // Load the native library
    static {
        try {
            System.loadLibrary("signal");
            Log.d("SvmRecognizer", "System.loadLibrary('signal');");
        } catch (UnsatisfiedLinkError ule) {
            Log.e(TAG, "Hey, could not load native library signal");
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * perfoms libsvm-androidjni predict
     */
    public boolean predict(float[] features, String modelFile, int label[], double probs[]){
    	//Log.d(TAG,"predict");
    	/**
    	 * here we include probability predict estimation (isProb = 1)
    	 * necessary for MyFeatures pre-calculation
    	 */
    	int isProb = 1;// 0 or 1 (default 0 (no probability predict))
    	
    	
    	int[] indices = new int[features.length];

    	// is necessary to scale features in the same way
    	//performed by libsvm training when generating the libsvm .model file
    	scaleFeatures(features);
    	
		for(int j = 0; j < indices.length; j++){
			indices[j] = j+1;
		}
		
    	/*
    	 * perform libsvm-androidjni predict
    	 */
    	if(doClassificationNative(new float[][]{features}, new int[][]{indices}, isProb, modelFile, label, probs) != 0){
    		// FAIL
    		return false;
    	}else{
    		// correct predict
    		Log.d(TAG,"predicted state: "+label[0]+"--------------------");
    		String[] sp = {"11", "13", "12", "14", "22", "23", "24", "21"};
    		for(int i  = 0; i < probs.length; i++){
    			Log.d(TAG,""+sp[i]+"="+probs[i]);
    		}
    		
    		return true;
    	}
    }
}
