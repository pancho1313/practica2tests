package com.example.myfeatureextraction;


import features.IFeatures;
import features.MyFeatures1;
import features.MyFeatures2;
import features.MyFeatures3;
import features.MyFeatures4;
import windowdata.IWindowData;
import windowdata.WindowHalfOverlap;
import activityrecognition.SvmBicycleRecognizerIntentService;
import activityrecognition.SvmCarRecognizerIntentService;
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
    
    private final String sendToBicycleSVM = "com.example.myfeatureextraction.BICYCLE_PREDICTION";
    private final String sendToCarSVM = "com.example.myfeatureextraction.CAR_PREDICTION";
    
    private final int sensorType = Sensor.TYPE_ACCELEROMETER;//TYPE_LINEAR_ACCELERATION
    private int sensorDelay = SensorManager.SENSOR_DELAY_FASTEST;

    private IWindowData windowDataBicycle;
    private IFeatures myFeaturesBicycle;
    private IWindowData windowDataCar;
    private IFeatures myFeaturesCar;
    
    private BroadcastReceiver bicycleActivityReceiver;
    private BroadcastReceiver carActivityReceiver;
    
    private String textView;
 	
    private void init(){
    	mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(sensorType);
        gSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    	
    	windowDataBicycle = SvmBicycleRecognizerIntentService.getNewWindowData();
		myFeaturesBicycle = SvmBicycleRecognizerIntentService.getMyFeatures();
		windowDataCar = SvmCarRecognizerIntentService.getNewWindowData();
		myFeaturesCar = SvmCarRecognizerIntentService.getMyFeatures();
		
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
	    	
	    	if(windowDataBicycle.addData(getDataForWindowData(linearAccel, myFeaturesBicycle.getFeaturesType()))){
	    		// we have a complete windowData
	    		
	    		// calculate features
	    		float [] features = myFeaturesBicycle.getFeatures(windowDataBicycle);
	    		
	    		// report new array of features
	    		addFeatures(sendToBicycleSVM, features);
	    		
	    	}
	    	/*
	    	if(windowDataCar.addData(getDataForWindowData(linearAccel, myFeaturesCar.getFeaturesType()))){
	    		// we have a complete windowData
	    		
	    		// calculate features
	    		float [] features = myFeaturesCar.getFeatures(windowDataCar);
	    		
	    		// report new array of features
	    		addFeatures(sendToCarSVM, features);
	    	}
	    	*/
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
    
    /**
     * performs a states (may be more than one prediction) prediction request
     * @param featuresList
     */
    private void requestStatePrediction(String sendTo, float[][] featuresList){
    	Intent intent = null;
    	
    	if(sendTo.equals(sendToBicycleSVM)){
    		intent = new Intent(this, SvmBicycleRecognizerIntentService.class);
    	}else if(sendTo.equals(sendToCarSVM)){
    		intent = new Intent(this, SvmCarRecognizerIntentService.class);
    	}
    	
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
     * a list of svm prediction requests making it more 
     * efficient but with a delay given by the size of this "prediction buffer".
     * @param features
     */
    private void addFeatures(String sendTo, float[] features){
    	// TODO send more than one feature array per prediction (more efficient)
    	requestStatePrediction(sendTo, new float[][]{features});
    }
    
    /**
     * here we receive predictions made before
     */
    private void initRecievers(){
    	bicycleActivityReceiver = new BroadcastReceiver() {
		    @Override
		    public void onReceive(Context context, Intent intent) {
		    	int[] statesPredicted = intent.getIntArrayExtra("statesPredicted");
		    	double[] predictionsProbabilities = intent.getDoubleArrayExtra("predictionsProbabilities");
		    	
		    	processPrediction(statesPredicted, predictionsProbabilities);
		    }
		};
		 
		IntentFilter filter = new IntentFilter();
		filter.addAction(sendToBicycleSVM);
		registerReceiver(bicycleActivityReceiver, filter);
		
		
		carActivityReceiver = new BroadcastReceiver() {
		    @Override
		    public void onReceive(Context context, Intent intent) {
		    	int[] statesPredicted = intent.getIntArrayExtra("statesPredicted");
		    	double[] predictionsProbabilities = intent.getDoubleArrayExtra("predictionsProbabilities");
		    	
		    	processPrediction(statesPredicted, predictionsProbabilities);
		    }
		};
		 
		IntentFilter filter2 = new IntentFilter();
		filter2.addAction(sendToCarSVM);
		registerReceiver(carActivityReceiver, filter2);
		 
    }
    private void unregisterReceivers(){
    	unregisterReceiver(bicycleActivityReceiver);
    	unregisterReceiver(carActivityReceiver);
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
