package activityrecognition;

import android.app.IntentService;
import android.content.Intent;

public class SvmRecognizerIntentService extends IntentService {
	
	public static final int NOT_MOVING = 1; // Must be >0
	public static final int CRUISE = 2; // Must be >0
	public static final int ACCELERATING = 3; // Must be >0
	public static final int BREAKING = 4; // Must be >0
	
	public SvmRecognizerIntentService(String id) {
        super(id);
    }
	
	protected void onHandleIntent(Intent intent) {
	}
	
	protected void predictState(
			Intent intent,
			String modelFile,
			int wSize,
			int floatsPerWindowData,
			float scaleH,
			float scaleL,
			float[] featuresMin,
			float[] featuresMax){
		// get list of features to predict
		String sendTo = intent.getStringExtra("sendTo");
		int featuresListLength = intent.getIntExtra("featuresList.length", 0);
		int featuresLength = intent.getIntExtra("featuresList[0].length", 0);
		float[][] featuresList = new float[featuresListLength][featuresLength];
		
		for(int i = 0; i < featuresListLength; i++){
			featuresList[i] = intent.getFloatArrayExtra("featuresList["+i+"]");
		}
		
		// predict
		int[] statesPredicted = new int[featuresListLength];
		double[] predictionsProbabilities = new double[featuresListLength];
		SvmRecognizer svmRecognizer = new SvmRecognizer(scaleH, scaleL, featuresMin, featuresMax);
		if(svmRecognizer.predict(featuresList, modelFile, statesPredicted, predictionsProbabilities)){
			sendPrediction(sendTo, statesPredicted, predictionsProbabilities);
		}
	}
	
	private void sendPrediction(String sendTo, int[] statesPredicted, double[] predictionsProbabilities){
		Intent i = new Intent(sendTo);
		i.putExtra("statesPredicted", statesPredicted);
		i.putExtra("predictionsProbabilities", predictionsProbabilities);
		sendBroadcast(i);
	}
}
