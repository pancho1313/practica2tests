package activityrecognition;

import android.app.IntentService;
import android.content.Intent;

public class SvmRecognizerIntentService extends IntentService {
	public static final int NUMBER_OF_LABELS = 4;
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
		float[] featuresList = intent.getFloatArrayExtra("featuresList");
		
		
		// predict
		int[] statesPredicted = new int[1];
		double[] predictionsProbabilities = new double[NUMBER_OF_LABELS];
		SvmRecognizer svmRecognizer = new SvmRecognizer(scaleH, scaleL, featuresMin, featuresMax);
		if(svmRecognizer.predict(featuresList, modelFile, statesPredicted, predictionsProbabilities)){
			sendPrediction(sendTo, statesPredicted, predictionsProbabilities);
		}
	}
	
	private void sendPrediction(String sendTo, int[] statesPredicted, double[] classesProbabilities){
		Intent i = new Intent(sendTo);
		i.putExtra("statePredicted", statesPredicted[0]);
		i.putExtra("classesProbabilities", classesProbabilities);
		sendBroadcast(i);
	}
}
