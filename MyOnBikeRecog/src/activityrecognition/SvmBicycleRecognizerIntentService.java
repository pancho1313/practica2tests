package activityrecognition;

import features.MyFeatures1;
import windowdata.WindowHalfOverlap;
import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;

public class SvmBicycleRecognizerIntentService extends IntentService implements SvmRecognizerIntentServiceInterface {
	private String TAG = "SvmBicycleRecognizerIntentService";
	
	public static final int NOT_MOVING = 1; // Must be >0
	public static final int CRUISE = 2; // Must be >0
	public static final int ACCELERATING = 3; // Must be >0
	public static final int BREAKING = 4; // Must be >0
	
	private final String modelFile = Environment.getExternalStorageDirectory() + "/trainingBike.64.1.txt.model";
	private static final int wSize = 64;
	private static final int floatsPerWindowData = MyFeatures1.FLOATS_PER_WINDOW_DATA;
	
	public SvmBicycleRecognizerIntentService() {
        super("SvmBicycleRecognizerIntentService");
    }
	
	protected void onHandleIntent(Intent intent) {
		predictState(intent);
	}
	
	private void predictState(Intent intent){
		// get list of features to predict
		String sendTo = intent.getStringExtra("sendTo");
		int featuresListLength = intent.getIntExtra("featuresList.length", 0);
		int featuresLength = intent.getIntExtra("featuresList[0].length", 0);
		float[][] featuresList = new float[featuresListLength][featuresLength];
		
		for(int i = 0; i < featuresListLength; i++){
			featuresList[i] = intent.getFloatArrayExtra("featuresList["+i+"]");
		}
		
		//libsvm scale ranges
		float scaleH = 1f;
		float scaleL = -1f;
		float[] featuresMin = {
				0.7140552f,
				0.66037196f,
				64.83476f,
				255.21948f
		};
		float[] featuresMax = {
				5.150639f,
				16.077961f,
				3977.108f,
				1688.7849f
		};
		
		// predict
		int[] statePredicted = new int[featuresListLength];
		double[] predictionProbability = new double[featuresListLength];
		SvmRecognizer svmRecognizer = new SvmRecognizer(scaleH, scaleL, featuresMin, featuresMax);
		svmRecognizer.predict(featuresList, modelFile, statePredicted, predictionProbability);
		
		sendPrediction(sendTo, statePredicted, predictionProbability);
	}
	
	private void sendPrediction(String sendTo, int[] statePredicted, double[] predictionProbability){
		Intent i = new Intent(sendTo);
		i.putExtra("statesPredicted", statePredicted);
		i.putExtra("predictionsProbabilities", predictionProbability);
		sendBroadcast(i);
	}
	
	public static WindowHalfOverlap getNewWindowData(){
		return new WindowHalfOverlap(wSize, floatsPerWindowData);
	}
	
	public static MyFeatures1 getMyFeatures(){
		return new MyFeatures1();
	}
}
