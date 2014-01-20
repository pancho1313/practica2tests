package com.example.activityregistrator;

import java.util.Date;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;



public class LinearAccelerationService extends Service implements SensorEventListener {
	private String TAG = ActivityRegistrator.TAG;
	private SensorManager mSensorManager;
    private Sensor mSensor, gSensor;
    private final int sensorType = Sensor.TYPE_LINEAR_ACCELERATION;//TODO TYPE_LINEAR_ACCELERATION TYPE_ACCELEROMETER
    private long initTime;
    private int dataLength;
    private String[] dataCache;
    private int dataIndex;
    private BroadcastReceiver reciever;
    private float[] gData;
    
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
    	//Log.d(TAG, "onSensorChanged");
    	if (event.sensor.getType() == sensorType)
    		updateData(event.values.clone());
    	if (event.sensor.getType() == Sensor.TYPE_GRAVITY)
    		gData = event.values.clone();
    }

    @Override
    public void onCreate() {
    	dataIndex = 0;
    	dataLength = 1000;
    	dataCache = new String[dataLength];
    	gData = new float[3];
    	
    	mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(sensorType);
        gSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, gSensor, SensorManager.SENSOR_DELAY_GAME);
        
        initTime = System.currentTimeMillis();
        
        initStopReciever();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        handleCommand(intent);
        saveDate();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
		Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
		unregisterReceiver(reciever);
		mSensorManager.unregisterListener(this);
    }
    
    private long getServiceTime(){
    	return System.currentTimeMillis() - initTime;
    }

    private void updateData(float[] data){
    	//Log.d(TAG, "updateData");
    	if(dataIndex < dataLength){
    		dataCache[dataIndex] = 
    				getServiceTime()
    				+ " " + data[0] + " " + data[1] + " " + data[2]
    				+ " " + gData[0] + " " + gData[1] + " " + gData[2];
    		dataIndex++;
    	}else{
    		saveData();
    		dataIndex = 0;
    	}
    }
    
    private void saveData(){
    	
    	Intent intent = new Intent(this, SaveFileIntentService.class);
    	String[] lastData = new String[dataIndex];
    	for(int i = 0; i < dataIndex; i++){
    		lastData[i] = dataCache[i];
    	}
    	intent.putExtra("lastData", lastData);
    	startService(intent);
    	Log.d(TAG, "pls saveData()");
    }
    
    private void saveDate(){	
    	Intent intent = new Intent(this, SaveFileIntentService.class);
    	String[] startDate = {"T " + new Date().toString()};
    	intent.putExtra("lastData", startDate);
    	startService(intent);
    	Log.d(TAG, "pls saveData()");
    }
    
    private void handleCommand(Intent i){
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle("LAccService")
		        .setContentText("LinearAccelerationService");
		
		PendingIntent pIntent = PendingIntent.getService(this.getApplicationContext(), 0, i,PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(pIntent);
		
		this.startForeground(333, mBuilder.build());
	}
    
    private void initStopReciever(){
    	reciever = new BroadcastReceiver() {
		    @Override
		    public void onReceive(Context context, Intent intent) {
		    	
		    	Log.d(TAG, "save and stop");
		    	saveData();
				stopMe();
		    }
		  };
		  
		 IntentFilter filter = new IntentFilter();
		 filter.addAction("com.example.activityregistrator.END_AND_SAVE");
		 registerReceiver(reciever, filter);
    }
    
    private void stopMe(){
    	this.stopSelf();
    }
    
    private void checkDeadLine(){
    	if(getServiceTime() > 60000)
    		stopMe();
    }
} 