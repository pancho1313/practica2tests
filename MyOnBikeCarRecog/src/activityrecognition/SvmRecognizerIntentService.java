package activityrecognition;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class SvmRecognizerIntentService extends IntentService {
	private final String TAG = "SvmRecognizerIntentService";
	
	public static final int NUMBER_OF_LABELS = 4 * 2;
	
	public static final int BIKE_NOT_MOVING = 11; // Must be >0
	public static final int BIKE_CRUISE = 12; // Must be >0
	public static final int BIKE_ACCELERATING = 13; // Must be >0
	public static final int BIKE_BREAKING = 14; // Must be >0
	
	public static final int CAR_NOT_MOVING = 21; // Must be >0
	public static final int CAR_CRUISE = 22; // Must be >0
	public static final int CAR_ACCELERATING = 23; // Must be >0
	public static final int CAR_BREAKING = 24; // Must be >0
	
	public SvmRecognizerIntentService() {
        super("SvmRecognizerIntentService");
    }
	
	protected void onHandleIntent(Intent intent) {
		(new SvmBicycleCarRecognizer()).predictState(intent, this);
	}
	
	public void predictState(
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
		int[] statePredicted = new int[NUMBER_OF_LABELS];
		double[] predictionsProbabilities = new double[NUMBER_OF_LABELS];
		SvmRecognizer svmRecognizer = new SvmRecognizer(scaleH, scaleL, featuresMin, featuresMax);
		if(svmRecognizer.predict(features, modelFile, statePredicted, predictionsProbabilities)){
			String s = "";
			for(int i : statePredicted)
				s += i+"";
			//Log.d(TAG, "labels: "+s);
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
