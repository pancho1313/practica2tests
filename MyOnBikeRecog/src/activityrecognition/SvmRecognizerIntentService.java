package activityrecognition;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class SvmRecognizerIntentService extends IntentService {
	private final String TAG = "SvmRecognizerIntentService";
	
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
		float[] features = intent.getFloatArrayExtra("features");
		
		
		// predict
		int[] statePredicted = new int[1];
		double[] predictionsProbabilities = new double[NUMBER_OF_LABELS];
		SvmRecognizer svmRecognizer = new SvmRecognizer(scaleH, scaleL, featuresMin, featuresMax);
		if(svmRecognizer.predict(features, modelFile, statePredicted, predictionsProbabilities)){
			sendPrediction(sendTo, statePredicted[0], predictionsProbabilities);
		}
	}
	
	private void sendPrediction(String sendTo, int statePredicted, double[] classesProbabilities){
		Intent i = new Intent(sendTo);
		i.putExtra("statePredicted", statePredicted);
		
		int maxprobid = 0;
		for(int k = 1; k < classesProbabilities.length; k++){
			if(classesProbabilities[k] > classesProbabilities[maxprobid])
				maxprobid = k;
		}
		if(statePredicted > 0)
			i.putExtra("stateProbability", classesProbabilities[maxprobid]);
		sendBroadcast(i);
	}
}
