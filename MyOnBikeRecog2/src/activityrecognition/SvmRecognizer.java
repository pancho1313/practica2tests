package activityrecognition;

import java.io.IOException;

import libsvm.*;
import android.os.Environment;
import android.util.Log;
public class SvmRecognizer implements IActivityRecognizer {

	private static String TAG = "SvmRecognizer";
	// scale
    //private Properties properties;
    float scaleZero;
	float scaleDif;
	float[] featuresZero;
	float[] featuresDif;
	
    public SvmRecognizer(float scaleH, float scaleL, float[] featuresMin, float[] featuresMax){
        // scale
        preCalculateScaleFeatures(scaleH, scaleL, featuresMin, featuresMax);
    }
	//////////scale////////////////////////////////////////////////////////////////////
	private void preCalculateScaleFeatures(float scaleH, float scaleL, float[] featuresMin, float[] featuresMax){
		// precalculate data
		scaleZero = (scaleH + scaleL)/2;
		scaleDif = scaleH - scaleL;
		featuresZero = new float[featuresMax.length];
		featuresDif = new float[featuresMax.length];
		for(int i = 0; i < featuresMax.length; i++){
			featuresZero[i] = (featuresMax[i] + featuresMin[i])/2;
			featuresDif[i] = featuresMax[i] - featuresMin[i];
		}
	}
	private void scaleFeatures(float[] features){
		for(int i = 0; i < features.length; i++){
			features[i] = scaleZero + ((features[i] - featuresZero[i]) * scaleDif)/featuresDif[i];
		}
	}
	/////////////////////////////////////////////////////////////////////////////////
	
	private void libsvmPredict(float[] featuresList, String modelFile, int labels[], double probs[]){
		svm_model model = null;
		try {
			model = svm.svm_load_model(modelFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(svm.svm_check_probability_model(model)==0){
			Log.e(TAG,"Model does not support probabiliy estimates");
		}
		svm_node[] x = new svm_node[featuresList.length];
		for(int j=0;j<featuresList.length;j++)
		{
			x[j] = new svm_node();
			x[j].index = j+1;
			x[j].value = featuresList[j];
		}

		double v= svm.svm_predict_probability(model,x,probs);
	}
    
    ////////////////////////////////////////////////////////////////////////////
    public boolean predict(float[][] featuresList, String modelFile, int labels[], double probs[]){
    	//Log.d(TAG,"predict");
    	int isProb = 1;// 0 or 1 (default 0)
    	int[][] indices = new int[featuresList.length][featuresList[0].length];

    	for(int i = 0; i < indices.length; i++){
    		for(int j = 0; j < indices[0].length; j++){
    			indices[i][j] = j+1;
    		}
    	}
    	
    	for(int i = 0; i < featuresList.length; i++){
    		scaleFeatures(featuresList[i]);
    	}
    	
    	if(doClassificationNative(featuresList, indices, isProb, modelFile, labels, probs) != 0){
    		return false;
    	}else{
    		return true;
    	}
    }
    ////////////////////////////////////////////////////////////////////////////
    
    

    
    /**
     * classify generate labels for features.
     * Return:
     * 	-1: Error
     * 	0: Correct
     */
    public int callSVM(float values[][], int indices[][], int groundTruth[], int isProb, String modelFile,
    		int labels[], double probs[]) {
    	// SVM type
    	final int C_SVC = 0;
    	final int NU_SVC = 1;
    	final int ONE_CLASS_SVM = 2;
    	final int EPSILON_SVR = 3;
    	final int NU_SVR = 4;
    	
    	// For accuracy calculation
    	int correct = 0;
    	int total = 0;
    	float error = 0;
    	float sump = 0, sumt = 0, sumpp = 0, sumtt = 0, sumpt = 0;
    	float MSE, SCC, accuracy;  	

    	int num = values.length;
    	int svm_type = C_SVC;
    	if (num != indices.length)
    		return -1;
    	// If isProb is true, you need to pass in a real double array for probability array
        int r = doClassificationNative(values, indices, isProb, modelFile, labels, probs);
        
        // Calculate accuracy
        if (groundTruth != null) {
        	if (groundTruth.length != indices.length) {
        		return -1;
        	}
        	for (int i = 0; i < num; i++) {
            	int predict_label = labels[i];
            	int target_label = groundTruth[i];
            	if(predict_label == target_label)
            		++correct;
    	        error += (predict_label-target_label)*(predict_label-target_label);
    	        sump += predict_label;
    	        sumt += target_label;
    	        sumpp += predict_label*predict_label;
    	        sumtt += target_label*target_label;
    	        sumpt += predict_label*target_label;
    	        ++total;
            }
            
        	if (svm_type==NU_SVR || svm_type==EPSILON_SVR)
        	{
        		MSE = error/total; // Mean square error
        		SCC = ((total*sumpt-sump*sumt)*(total*sumpt-sump*sumt)) / ((total*sumpp-sump*sump)*(total*sumtt-sumt*sumt)); // Squared correlation coefficient
        	}
        	accuracy = (float)correct/total*100;
            Log.d(TAG, "Classification accuracy is " + accuracy);
        }       
        
        return r;
    }
    
    public void classify() {
        // Svm classification
        float[][] values = {
                        {0.708333f, 1, 1, -0.320755f, -0.105023f, -1, 1, -0.419847f, -1, -0.225806f, 1, -1 },
                        {0.583333f, -1, 0.333333f, -0.603774f, 1, -1, 1, 0.358779f, -1, -0.483871f, -1, 1},
                        {0.166667f, 1, -0.333333f, -0.433962f, -0.383562f, -1, -1, 0.0687023f, -1, -0.903226f, -1, 1},
                        {0.458333f, 1, 1, -0.358491f, -0.374429f, -1, 1, -0.480916f, 1, -0.935484f, -0.333333f, 1 },
        };
        int[][] indices = {
                        {1,2,3,4,5,6,7,8,9,10,12,13},
                        {1,2,3,4,5,6,7,8,9,10,12,13},
                        {1,2,3,4,5,6,7,8,9,10,12,13},
                        {1,2,3,4,5,6,7,8,9,10,12,13}
        };
        int[] groundTruth = null;
        int[] labels = new int[4];
        double[] probs = new double[4];
        int isProb = 0; // Not probability prediction
        String modelFileLoc = Environment.getExternalStorageDirectory()+"/training_set.model"; // "/model" "/training_set.model"

        if (callSVM(values, indices, groundTruth, isProb, modelFileLoc, labels, probs) != 0) {
                Log.d(TAG, "Classification is incorrect");
        }
        else {
        	String m = "";
        	for (int l : labels)
        		m += l + ", ";
        	Log.d(TAG, "DONE Clasification: "+m);
        }
    }
}