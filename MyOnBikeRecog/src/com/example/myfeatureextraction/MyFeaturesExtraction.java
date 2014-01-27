package com.example.myfeatureextraction;


import features.IFeatures;
import features.MyFeatures1;
import features.MyFeatures2;
import features.MyFeatures3;
import features.MyFeatures4;
import windowdata.IWindowData;
import windowdata.WindowHalfOverlap;
import activityrecognition.SvmBicycleRecognizerIntentService;
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
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class MyFeaturesExtraction extends Activity implements SensorEventListener {

	private String TAG = "MyFeaturesExtraction";
	private SensorManager mSensorManager;
    private Sensor mSensor, gSensor;
    private float[] gData;
    private float prevState;
    private float prevStateProbability;
    
    private final String sendTo = "com.example.myfeatureextraction.USER_ACTIVITY_PREDICTION";
    
    private final int sensorType = Sensor.TYPE_LINEAR_ACCELERATION;
    private int sensorDelay = SensorManager.SENSOR_DELAY_FASTEST;

    private IWindowData windowData;
    private IFeatures myFeatures;
    
    private BroadcastReceiver userActivityReceiver;
    
    private String textView;
 	
    private void init(){
    	mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(sensorType);
        gSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    	
    	windowData = SvmBicycleRecognizerIntentService.getNewWindowData();
		myFeatures = SvmBicycleRecognizerIntentService.getMyFeatures();
		
		initRecievers();
		
		gData = new float[3];
		prevState = prevStateProbability = 0f;
		
		textView = "";
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
	    		addFeatures(features);
	    	}
    	}
    	if (event.sensor.getType() == Sensor.TYPE_GRAVITY)
    		gData = event.values.clone();
    }
    
    private void updateUserStateDisplay(String statePredicted, double predictionProbability){
    	// TODO
    	textView = statePredicted + " (" + predictionProbability + ")\n" + textView;
    	refreshTextView();
    }
    
    private void refreshTextView(){
    	((TextView)findViewById(R.id.textView)).setText(textView);
    }
    
    //////////////////////////////intent service
    /**
     * performs a states (may be more than one prediction) prediction request
     * @param featuresList
     */
    private void requestStatePrediction(float[][] featuresList){
    	Intent intent = new Intent(this, SvmBicycleRecognizerIntentService.class);
    	intent.putExtra("featuresList.length", featuresList.length);
    	intent.putExtra("featuresList[0].length", featuresList[0].length);
    	intent.putExtra("sendTo", sendTo);
    	
    	for(int i = 0; i < featuresList.length; i++){
    		intent.putExtra("featuresList["+i+"]", featuresList[i]);
    	}
    	
    	startService(intent);
    }
    
    /**
     * this is usefull if we pretend to accumulate
     * a list of svm prediction requests  
     * @param features
     */
    private void addFeatures(float[] features){
    	// TODO admit more than one featureLists per prediction (more efficient)
    	requestStatePrediction(new float[][]{features});
    }
    
    /**
     * here we receive predictions made before
     */
    private void initRecievers(){
    	userActivityReceiver = new BroadcastReceiver() {
		    @Override
		    public void onReceive(Context context, Intent intent) {
		    	int[] statesPredicted = intent.getIntArrayExtra("statesPredicted");
		    	double[] predictionsProbabilities = intent.getDoubleArrayExtra("predictionsProbabilities");
		    	
		    	processPrediction(statesPredicted, predictionsProbabilities);
		    }
		  };
		  
		 IntentFilter filter = new IntentFilter();
		 filter.addAction(sendTo);
		 registerReceiver(userActivityReceiver, filter);
    }
    private void unregisterReceivers(){
    	unregisterReceiver(userActivityReceiver);
    }
    
    /**
     * here we can decide what to do with the prediction result
     * 
     * @param statesPredicted
     * @param predictionsProbabilities
     */
    private void processPrediction(int[] statesPredicted, double[] predictionsProbabilities){
    	//TODO
    	updateUserStateDisplay(stateToString(statesPredicted[0]), predictionsProbabilities[0]);
    	
    	// TODO IMPORTANT!!! update prevState and prevStateProbability wisely
    	
    	if(predictionsProbabilities[0] < 0)
    		Log.e(TAG, "predictionsProbabilities[0] < 0");
    	// TODO: decide prevState by prevStateProbability
    	prevState = statesPredicted[0];
    	prevStateProbability = (float) predictionsProbabilities[0];
    }
    
    private String stateToString(int state){
    	String s = "---";
    	switch(state){
    	case SvmBicycleRecognizerIntentService.NOT_MOVING:
    		s = "NOT_MOVING";
    		break;
    	case SvmBicycleRecognizerIntentService.CRUISE:
    		s = "CRUISE";
    		break;
    	case SvmBicycleRecognizerIntentService.ACCELERATING:
    		s = "ACCELERATING";
    		break;
    	case SvmBicycleRecognizerIntentService.BREAKING:
    		s = "BREAKING";
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
