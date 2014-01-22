package com.example.myfeatureextraction;

import features.IFeatures;
import features.MyFeatures;
import windowdata.IWindowData;
import windowdata.WindowHalfOverlap;
import activityrecognition.IActivityRecognizer;
import activityrecognition.SvmRecognizer;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class MyFeaturesExtraction extends Activity implements SensorEventListener {

	private String TAG = "MyFeaturesExtraction";
	private SensorManager mSensorManager;
    private Sensor mSensor;
    
    private final int sensorType = Sensor.TYPE_LINEAR_ACCELERATION;
    private int sensorDelay = SensorManager.SENSOR_DELAY_FASTEST;

    private IWindowData windowData;
    private IFeatures myFeatures;
    private IActivityRecognizer activityRecognizer;
 	
    private void init(){
    	mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(sensorType);
    	
    	windowData = new WindowHalfOverlap();
		myFeatures = new MyFeatures();
		activityRecognizer = new SvmRecognizer(this);
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
	
	protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, sensorDelay);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
    	//Log.d(TAG, "onSensorChanged");
    	if (event.sensor.getType() != sensorType)
            return;
    	
    	// get gravity and linear acceleration
    	float[] linearAccel = event.values.clone();
    	
    	if(windowData.addData((float)vecLength(linearAccel))){
    		// we have a complete windowData
    		
    		// calculate features
    		float [] features = myFeatures.getFeatures(windowData);
    		
    		// refresh user activity on screen
    		refreshUserDisplay(activityRecognizer.getUserRecognizedActivity(features));
    	}
    }

    // vector utils
    private double vecLength(float[] v){
    	return Math.sqrt(Math.pow(v[0], 2)+Math.pow(v[1], 2)+Math.pow(v[2], 2));
    }
    
    private void refreshUserDisplay(int activity){
    	// TODO
    }
    
    private void mytest(){
    	//Log.d("SvmRecognizer", "test()");
    	SvmRecognizer mysvm = new SvmRecognizer(this);
    	//mysvm.train();
    	mysvm.classify();
    	
    }
    public void action(View v){
    	mytest();
    }
}
