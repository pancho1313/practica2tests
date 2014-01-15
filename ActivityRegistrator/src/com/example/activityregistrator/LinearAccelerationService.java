package com.example.activityregistrator;

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
    private Sensor mSensor;
    private final int sensorType = Sensor.TYPE_LINEAR_ACCELERATION;
    private long initTime;
    private int dataLength;
    private String[] dataCache;
    private int dataIndex;
    private BroadcastReceiver receiver;
    
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
    	if (event.sensor.getType() != sensorType)
            return;
    	
    	updateData(event.values.clone());
    }

    @Override
    public void onCreate() {
    	dataIndex = 0;
    	dataLength = 10;
    	dataCache = new String[dataLength];
    	
    	mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(sensorType);
        //mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        
        initTime = System.currentTimeMillis();
        
        initStopReciever();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting time: ", Toast.LENGTH_SHORT).show();
        handleCommand(intent);

        saveData();
        
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
      Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
      unregisterReceiver(receiver);
      mSensorManager.unregisterListener(this);
    }
    
    private long getServiceTime(){
    	return System.currentTimeMillis() - initTime;
    }

    private void updateData(float[] data){
    	if(dataIndex < dataLength){
    		dataCache[dataIndex] = getServiceTime() + " " + data[0] + " " + data[1] + " " + data[2];
    		dataIndex++;
    	}else{
    		saveData();
    		dataIndex = 0;
    	}
    	/*
    	Intent i = new Intent("com.example.activityregistrator.LINEAR_ACCELERATION_DATA");
		i.putExtra("time", getType(result.getMostProbableActivity().getType()) );
		i.putExtra("Confidence", result.getMostProbableActivity().getConfidence());
		i.putExtra("activityType", result.getMostProbableActivity().getType());
		i.getFloatArrayExtra("");
		sendBroadcast(i);
		*/
    }
    
    private void saveData(){
    	Intent i = new Intent(this, SaveFileIntentService.class);
    	i.putExtra("dataCache", dataCache.clone());// TODO .clone()?
    	startService(i);
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
    	receiver = new BroadcastReceiver() {
		    @Override
		    public void onReceive(Context context, Intent intent) {
		    	
		    	Log.d(TAG, "stop and save");
				stopMe();
		    }
		  };
		  
		 IntentFilter filter = new IntentFilter();
		 filter.addAction("com.example.activityregistrator.END_AND_SAVE");
		 registerReceiver(receiver, filter);
    }
    
    private void stopMe(){
    	this.stopSelf();
    }
} 