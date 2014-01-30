package com.example.myfeatureextraction;


import myutil.MyUtil;
import features.IFeatures;
import features.MyFeatures1;
import features.MyFeatures2;
import features.MyFeatures3;
import features.MyFeatures4;
import windowdata.IWindowData;
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

public class MyFeaturesExtraction extends Activity implements SensorEventListener {

	private String TAG = "MyFeaturesExtraction";
	private SensorManager mSensorManager;
    private Sensor mSensor, gSensor;
    private float[] gData;
    private float prevState;
    private float prevStateProbability;
    
    public static final String sendToBicycleCarSVM = "com.example.myfeatureextraction.BICYCLE_CAR_PREDICTION";
    
    private final int sensorType = Sensor.TYPE_LINEAR_ACCELERATION;
    private int sensorDelay = SensorManager.SENSOR_DELAY_FASTEST;

    private IWindowData windowData;
    private IFeatures myFeatures;
    
    private BroadcastReceiver receiver;
    
    private String textView;
    private boolean playSound;
    private boolean dualNM;
 	
    private void init(){
    	mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(sensorType);
        gSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    	
    	windowData = SvmBicycleCarRecognizer.getNewWindowData();
		myFeatures = SvmBicycleCarRecognizer.getMyFeatures();
		
		initRecievers();
		
		gData = new float[3];
		prevState = prevStateProbability = 0f;
		
		textView = "";
		playSound = false;
		dualNM = false;
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_feature_extraction);
		
		//evitar que se apague la pantalla
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

    public void onSensorChanged(SensorEvent event) {
    	
    	if (event.sensor.getType() == sensorType){
	    	// get gravity and linear acceleration
	    	float[] linearAccel = event.values.clone();
	    	
	    	if(windowData.addData(getDataForWindowData(linearAccel, myFeatures.getFeaturesType()))){
	    		// we have a complete windowData
	    		
	    		// calculate features
	    		float [] features = myFeatures.getFeatures(windowData);
	    		
	    		// report new array of features
	    		requestStatePrediction(sendToBicycleCarSVM, features);
	    		
	    	}
    	}
    	
    	if (event.sensor.getType() == Sensor.TYPE_GRAVITY)
    		gData = event.values.clone();
    }
    
    private void updateUserStateDisplay(String statePredicted, float predictionProbability){
    	// TODO
    	String s = statePredicted + " " + predictionProbability + "\n";
    	textView = s + textView;
    	refreshTextView();
    	
    	//TODO
    	if(playSound)
    		MyUtil.playAudio("beep_high.mp3", this, 0.5f, 0.2f);
    }
    
    private void refreshTextView(){
    	((TextView)findViewById(R.id.textView)).setText(textView);
    }
    
    /**
     * performs a states (may be more than one prediction) prediction request
     * @param features
     */
    private void requestStatePrediction(String sendTo, float[] features){
    	Intent intent = new Intent(this, SvmRecognizerIntentService.class);
    	intent.putExtra("features", features);
    	intent.putExtra("sendTo", sendTo);
    	startService(intent);
    }
    
    
    /**
     * here we receive predictions made before
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
     * here we can decide what to do with the prediction result
     * 
     * @param statePredicted
     * @param classesProbabilities
     */
    private void processPrediction(String from, Intent intent){
    	
    	int statePredicted = intent.getIntExtra("statePredicted",-1);
    	double stateProbability = intent.getDoubleExtra("stateProbability", -1);
    	
    	if(statePredicted == -1){
    		Log.d(TAG, "statePredicted == -1");
    	}

    	
    	// TODO IMPORTANT!!! update prevState and prevStateProbability wisely
    	

    	// TODO: decide prevState by prevStateProbability
    	if(dualNM && ((statePredicted==SvmRecognizerIntentService.BIKE_NOT_MOVING) || (statePredicted==SvmRecognizerIntentService.CAR_NOT_MOVING)))
    		prevState = -1f;
    	else
    		prevState = statePredicted;
    	prevStateProbability = (statePredicted>0) ? (float) stateProbability : -1f;
    	
    	updateUserStateDisplay(stateToString(statePredicted), prevStateProbability);//TODO get max probability
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
