package activityrecognition;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * this service receives features vector and performs a state prediction
 * @author fhafon
 *
 */
public class SvmRecognizerIntentService extends IntentService {
	private final String TAG = "SvmRecognizerIntentService";
	
	// number of states posible to predict with svm
	public static final int NUMBER_OF_LABELS = 4 * 2;
	
	// bicycle states 
	public static final int BIKE_NOT_MOVING = 11; // Must be >0
	public static final int BIKE_CRUISE = 12; // Must be >0
	public static final int BIKE_ACCELERATING = 13; // Must be >0
	public static final int BIKE_BREAKING = 14; // Must be >0
	
	// car states
	public static final int CAR_NOT_MOVING = 21; // Must be >0
	public static final int CAR_CRUISE = 22; // Must be >0
	public static final int CAR_ACCELERATING = 23; // Must be >0
	public static final int CAR_BREAKING = 24; // Must be >0
	
	public SvmRecognizerIntentService() {
        super("SvmRecognizerIntentService");
    }
	
	protected void onHandleIntent(Intent intent) {
		// set parameters and perform prediction
		(new SvmBicycleCarRecognizer(this.getApplicationContext())).predictState(intent, this);
	}
	
	/**
	 * performs svm state prediction
	 * @param intent
	 * @param modelFile
	 * @param wSize
	 * @param floatsPerWindowData
	 * @param scaleH
	 * @param scaleL
	 * @param featuresMin
	 * @param featuresMax
	 */
	public void predictState(
			Intent intent,
			String modelFile,
			int wSize,
			int floatsPerWindowData,
			float scaleH,
			float scaleL,
			float[] featuresMin,
			float[] featuresMax){
		// get vector of features to use in svm prediction
		String sendTo = intent.getStringExtra("sendTo");
		float[] features = intent.getFloatArrayExtra("features");
		
		
		// get svm prediction from SvmRecognizer
		int[] statePredicted = new int[NUMBER_OF_LABELS];
		double[] predictionsProbabilities = new double[NUMBER_OF_LABELS];
		SvmRecognizer svmRecognizer = new SvmRecognizer(scaleH, scaleL, featuresMin, featuresMax, this.getApplicationContext());
		if(svmRecognizer.predict(features, modelFile, statePredicted, predictionsProbabilities)){
			// send prediction to MyFeaturesExtraction
			sendPrediction(sendTo, statePredicted[0], predictionsProbabilities);
		}
	}
	
	/**
	 * send predicted state and probability to the BroadcastReceiver specified by sendTo
	 * @param sendTo
	 * @param statePredicted
	 * @param classesProbabilities
	 */
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
