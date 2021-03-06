package com.example.myfeatureextraction;



import myutil.MyUtil;
import features.IFeatures;
import features.MyFeatures1;
import features.MyFeatures2;
import features.MyFeatures3;
import features.MyFeatures4;
import windowdata.IWindowData;
import windowdata.WindowHalfOverlap;
import activityrecognition.SvmBicycleCarRecognizer;
import activityrecognition.SvmRecognizerIntentService;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
/**
 * This class obtains sensor data and sends it to SvmRecognizer.
 * Receives the state prediction from SvmRecognizerIntentService and process it to display the detected activity to user. 
 * @author fhafon
 *
 */
public class MyFeaturesExtraction extends Activity implements SensorEventListener {

	private String TAG = "MyFeaturesExtraction";
	private SensorManager mSensorManager;
    private Sensor mSensor, gSensor;
    private float[] gData;
    private float prevState;
    private float prevStateProbability;
    private WindowHalfOverlap statesProbsWindow;
    
    // global message identifier used to communicate svm predictions
    public static final String sendToBicycleCarSVM = "com.example.myfeatureextraction.BICYCLE_CAR_PREDICTION";
    
    // set sensor type and delay
    private final int sensorType = Sensor.TYPE_LINEAR_ACCELERATION;
    private int sensorDelay = SensorManager.SENSOR_DELAY_FASTEST;
    
    private IWindowData windowData;
    private IFeatures myFeatures;
    
    // receives the state/activity prediction from SvmRecognizerIntentService
    private BroadcastReceiver receiver;
    
    private String textView;
    private boolean playSound, dualNM, prevStateProbabilityMode;
 	
    private void init(){
    	
    	mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(sensorType);
        gSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    	
        // to store the sensor data
    	windowData = SvmBicycleCarRecognizer.getNewWindowData();
		myFeatures = SvmBicycleCarRecognizer.getMyFeatures();
		
		initRecievers();
		
		gData = new float[3]; // vector with gravity sensor data
		prevState = prevStateProbability = 0f;
		
		// this window accumulates the last state predictions for better activity approximation
		int statesProbsWindowSize = 4; // use the last 4 state predictions
		int floatsPerWindowData = 2; // state predicted and its probability
		statesProbsWindow = new WindowHalfOverlap(statesProbsWindowSize, floatsPerWindowData);
		
		// user display
		textView = "";
		
		// menu settings
		playSound = false; // alert the predicted activity
		dualNM = false; // explained on README
		prevStateProbabilityMode = false; // explained on README 
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_feature_extraction);
		
		//keep screen on
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		init();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.my_feature_extraction, menu);
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item){
		if(item.getItemId() == R.id.volToggle){
			playSound = !playSound;
			Toast.makeText(this, "sound: "+playSound, Toast.LENGTH_SHORT).show();
		}else if(item.getItemId() == R.id.dualNM){
			dualNM = !dualNM;
			Toast.makeText(this, "dualNM: "+dualNM, Toast.LENGTH_SHORT).show();
		} else if(item.getItemId() == R.id.prevStateProbabilityMode){
			prevStateProbabilityMode = !prevStateProbabilityMode;
			Toast.makeText(this, "prevStateProbabilityMode: "+prevStateProbabilityMode, Toast.LENGTH_SHORT).show();
		}
		return true;
	}
	
	@Override
    public void onDestroy() {
		super.onDestroy();
		unregisterReceivers();
    }
	
	protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, sensorDelay);
        mSensorManager.registerListener(this, gSensor, sensorDelay);
        refreshTextView();
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    /**
     * here we receive an pre-process the sensors (linear acc. and gravity) data
     */
    public void onSensorChanged(SensorEvent event) {
    	
    	if (event.sensor.getType() == sensorType){
	    	// get gravity and linear acceleration
	    	float[] linearAccel = event.values.clone();
	    	
	    	// add linear acc. data to the window data
	    	if(windowData.addData(getDataForWindowData(linearAccel, myFeatures.getFeaturesType()))){
	    		// complete windowData, we have to calculate the features 
	    		
	    		// calculate features
	    		float [] features = myFeatures.getFeatures(windowData);
	    		
	    		// report new array of features for svm prediction
	    		requestStatePrediction(sendToBicycleCarSVM, features);
	    	}
    	}
    	
    	if (event.sensor.getType() == Sensor.TYPE_GRAVITY)
    		gData = event.values.clone();
    }
    
    /**
     * show predicted state and its probability on user's screen
     * @param statePredicted
     * @param predictionProbability
     */
    private void updateUserStateDisplay(String statePredicted, float predictionProbability){
    	String s = statePredicted + " " + predictionProbability + "\n";
    	textView = s + textView;
    	refreshTextView();
    }
    
    private void refreshTextView(){
    	((TextView)findViewById(R.id.textView)).setText(textView);
    }
    
    /**
     * performs a state prediction request sending features vector to SvmRecognizerIntentService
     * @param features
     */
    private void requestStatePrediction(String sendTo, float[] features){
    	Intent intent = new Intent(this, SvmRecognizerIntentService.class);
    	intent.putExtra("features", features);
    	intent.putExtra("sendTo", sendTo);
    	startService(intent);
    }
    
    
    /**
     * here we receive and process state predictions
     */
    private void initRecievers(){
    	receiver = new BroadcastReceiver() {
		    @Override
		    public void onReceive(Context context, Intent intent) {
		    	processPrediction(sendToBicycleCarSVM, intent);
		    }
		};
		 
		IntentFilter filter = new IntentFilter();
		filter.addAction(sendToBicycleCarSVM);
		registerReceiver(receiver, filter);
		 
    }
    private void unregisterReceivers(){
    	unregisterReceiver(receiver);
    }
    
    /**
     * here we can decide what to do with the prediction result,
     * 
     * 
     * @param statePredicted
     * @param classesProbabilities
     */
    private void processPrediction(String from, Intent intent){
    	// get state predicted and its probability
    	int statePredicted = intent.getIntExtra("statePredicted",-1);
    	double stateProbability = intent.getDoubleExtra("stateProbability", -1);
    	
    	// Necessary condition to support prevStateProbabilityMode (on README)
    	if(stateProbability >= 1.0)
    		stateProbability = 0.99;

    	// set BIKE_NOT_MOVING and CAR_NOT_MOVING as the same state
    	if(dualNM && ((statePredicted==SvmRecognizerIntentService.BIKE_NOT_MOVING) || (statePredicted==SvmRecognizerIntentService.CAR_NOT_MOVING)))
    		prevState = -1f;
    	else
    		prevState = statePredicted;
    	prevStateProbability = (statePredicted>0) ? (float) stateProbability : -0.5f;
    	
    	// include the probability of the predicted state to the vector of features
    	if(prevStateProbabilityMode && prevStateProbability < 1){
    		prevState += prevStateProbability;
    		Log.d(TAG, "prevStateProbabilityMode="+prevState+" ("+((int)prevState)+" "+(prevState-(int)prevState)+")");
    	}
    	
    	// show an user activity calculated with previous state predictions
    	if(statesProbsWindow.addData(new float[]{statePredicted, prevStateProbability})){
    		int mostProbableState = getMostProbableState(statesProbsWindow.getData(0), statesProbsWindow.getData(1));
    		informMostProbableState(mostProbableState);
    	}
    	
    	// show state predicted and its probability
    	updateUserStateDisplay(stateToString(statePredicted), prevStateProbability);
    }
    /**
     * calculate Most Probable State from a windowdata with the last few predictions
     * @param states
     * @param probs
     * @return
     */
    private int getMostProbableState(float[] states, float[] probs){
    	String TAG = "getMostProbableState";
    	int[] STATES = {
    			SvmRecognizerIntentService.BIKE_NOT_MOVING,
    			SvmRecognizerIntentService.BIKE_CRUISE,
    			SvmRecognizerIntentService.BIKE_ACCELERATING,
    			SvmRecognizerIntentService.BIKE_BREAKING,
    			
    			SvmRecognizerIntentService.CAR_NOT_MOVING,
    			SvmRecognizerIntentService.CAR_CRUISE,
    			SvmRecognizerIntentService.CAR_ACCELERATING,
    			SvmRecognizerIntentService.CAR_BREAKING,
    	};
    	
    	float[] STATESprob = new float[STATES.length];
    	Log.d(TAG, "-------------------");
    	for(int i = 0; i < states.length; i++){
    		Log.d(TAG,"(int)(states[i])="+(int)(states[i]));
    		int index = 0;
    		for(int j = 0; j < STATES.length; j++){
    			if((int)(states[i]) == STATES[j]){
    				index = j;
    				break;
    			}
    		}
    		
    		Log.d(TAG,"index="+index);
    		if(index>=0 && index<STATESprob.length){
    			STATESprob[index] += probs[i];
    			Log.d(TAG, "getMostProbableState"+stateToString(index));
    		}
    	}
    	
    	int maxIndex = 0;
    	for(int i = 1; i < STATESprob.length; i++){
    		if(STATESprob[i] > STATESprob[maxIndex])
    			maxIndex = i;
    	}
    	
    	return STATES[maxIndex];
    }
    
    /**
     * play audio of the most probable state predicted
     * @param mostProbableState
     */
    private void informMostProbableState(int mostProbableState){
    	Toast.makeText(this, "mostProbableState: "+stateToString(mostProbableState), Toast.LENGTH_SHORT).show();
    	if(playSound){
    		String audioPath ="";
    		switch(mostProbableState){
    		case SvmRecognizerIntentService.BIKE_NOT_MOVING:
    			audioPath = "not_moving.mp3";
    			break;
    		case SvmRecognizerIntentService.BIKE_CRUISE:
    			audioPath = "cruise.mp3";
    			break;
    		case SvmRecognizerIntentService.BIKE_ACCELERATING:
    			audioPath = "accelerating.mp3";
    			break;
    		case SvmRecognizerIntentService.BIKE_BREAKING:
    			audioPath = "breaking.mp3";
    			break;
    		case SvmRecognizerIntentService.CAR_NOT_MOVING:
    			audioPath = "not_moving.mp3";
    			break;
    		case SvmRecognizerIntentService.CAR_CRUISE:
    			audioPath = "cruise.mp3";
    			break;
    		case SvmRecognizerIntentService.CAR_ACCELERATING:
    			audioPath = "accelerating.mp3";
    			break;
    		case SvmRecognizerIntentService.CAR_BREAKING:
    			audioPath = "breaking.mp3";
    			break;
    		default:
    			audioPath = "beep_high.mp3";
    		}
    		
    		MyUtil.playAudio(audioPath, this, 1.0f, 0.5f);
    	}
    }
    
    private String stateToString(int state){
    	String s = "---";
    	switch(state){
    	case SvmRecognizerIntentService.BIKE_NOT_MOVING:
    		s = "BIKE_NOT_MOVING";
    		break;
    	case SvmRecognizerIntentService.BIKE_CRUISE:
    		s = "BIKE_CRUISE";
    		break;
    	case SvmRecognizerIntentService.BIKE_ACCELERATING:
    		s = "BIKE_ACCELERATING";
    		break;
    	case SvmRecognizerIntentService.BIKE_BREAKING:
    		s = "BIKE_BREAKING";
    		break;
    	case SvmRecognizerIntentService.CAR_NOT_MOVING:
    		s = "CAR_NOT_MOVING";
    		break;
    	case SvmRecognizerIntentService.CAR_CRUISE:
    		s = "CAR_CRUISE";
    		break;
    	case SvmRecognizerIntentService.CAR_ACCELERATING:
    		s = "CAR_ACCELERATING";
    		break;
    	case SvmRecognizerIntentService.CAR_BREAKING:
    		s = "CAR_BREAKING";
    		break;
    	}
    	return s;
    }
    
    /**
     * create the data vector to be stored in the windowData for future state prediction.
     * each type of feature use different information from sensor data.
     * @param linearAccel
     * @param featuresType
     * @return
     */
    private float[] getDataForWindowData(float[] linearAccel, int featuresType){
		switch(featuresType){
		case MyFeatures2.FEATURES_TYPE:
			return MyFeatures2.getDataForWindowData(linearAccel, gData);
		case MyFeatures3.FEATURES_TYPE:
			return MyFeatures3.getDataForWindowData(linearAccel, prevState);
		case MyFeatures4.FEATURES_TYPE:
			return MyFeatures4.getDataForWindowData(linearAccel, gData, prevState);
		default: // MyFeatures1
			return MyFeatures1.getDataForWindowData(linearAccel);
		}
    }
}
