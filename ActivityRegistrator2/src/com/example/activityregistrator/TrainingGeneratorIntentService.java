package com.example.activityregistrator;

import java.util.Properties;

import features.MyFeatures1;
import features.MyFeatures2;
import features.MyFeatures3;
import features.MyFeatures4;

import myutil.AssetsPropertyReader;

import trainergenerator.SVMTraining;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class TrainingGeneratorIntentService extends IntentService {
	private String TAG = "TrainingGeneratorIntentService";
	
	private AssetsPropertyReader assetsPropertyReader;
	private Context context;
    private Properties properties;
	
	public TrainingGeneratorIntentService() {
        super("TrainingGeneratorIntentService");
    }
	
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "flag");
		
		// .properties
		context = this.getApplicationContext();
		assetsPropertyReader = new AssetsPropertyReader(context);
		properties = assetsPropertyReader.getProperties("ActivityRegistrator.properties");
		String
		sensorDataFolder = intent.getStringExtra("sensorDataFolder"),
		sensorDataFile = intent.getStringExtra("sensorDataFile"),
		sensorMarksFolder = intent.getStringExtra("sensorMarksFolder"),
		sensorMarksFile = intent.getStringExtra("sensorMarksFile"),
		outFolder = intent.getStringExtra("outFolder"),
		outFile = intent.getStringExtra("outFile");
		
		handleCommand(intent);
		
		int wSize = Integer.parseInt((String) properties.get("wSize"));
		int featuresType;
		int floatsPerWindowData;
		if(((String) properties.get("features")).equals("MyFeatures2")){
			featuresType = MyFeatures2.FEATURES_TYPE;
			floatsPerWindowData = MyFeatures2.FLOATS_PER_WINDOW_DATA;
		}else if(((String) properties.get("features")).equals("MyFeatures3")){
			featuresType = MyFeatures3.FEATURES_TYPE;
			floatsPerWindowData = MyFeatures3.FLOATS_PER_WINDOW_DATA;
		}else if(((String) properties.get("features")).equals("MyFeatures4")){
			featuresType = MyFeatures4.FEATURES_TYPE;
			floatsPerWindowData = MyFeatures4.FLOATS_PER_WINDOW_DATA;
		}else /* "MyFeatures1" */{
			featuresType = MyFeatures1.FEATURES_TYPE;
			floatsPerWindowData = MyFeatures1.FLOATS_PER_WINDOW_DATA;
		}
		
		if(SVMTraining.generateTrainingFile(
				sensorDataFolder, 
				sensorDataFile,
				sensorMarksFolder,
				sensorMarksFile,
				outFolder,
				outFile,
				this.getApplicationContext(),
				wSize,
				floatsPerWindowData,
				featuresType)){
			
			Intent i = new Intent("com.example.activityregistrator.USER_MESSAGE");
	    	i.putExtra("userMessage", "DONE");
			sendBroadcast(i);
		}else{
			Intent i = new Intent("com.example.activityregistrator.USER_MESSAGE");
	    	i.putExtra("userMessage", "FAIL");
			sendBroadcast(i);
		}
	}
	
	private void handleCommand(Intent i){
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle("TrainingService")
		        .setContentText(TAG);
		
		PendingIntent pIntent = PendingIntent.getService(this.getApplicationContext(), 0, i,PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(pIntent);
		
		this.startForeground(333, mBuilder.build());
	}
	
	
}