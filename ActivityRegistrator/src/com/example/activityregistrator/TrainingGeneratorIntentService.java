package com.example.activityregistrator;

import trainergenerator.SVMTraining;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class TrainingGeneratorIntentService extends IntentService {
	private String TAG = "TrainingGeneratorIntentService";
	
	public TrainingGeneratorIntentService() {
        super("TrainingGeneratorIntentService");
    }
	
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "flag");
		if(SVMTraining.generateTrainingFile(
				intent.getStringExtra("sensorDataFolder"), 
				intent.getStringExtra("sensorDataFile"),
				intent.getStringExtra("sensorMarksFolder"),
				intent.getStringExtra("sensorMarksFile"),
				intent.getStringExtra("outFolder"),
				intent.getStringExtra("outFile"),
				this.getApplicationContext())){
			
			Intent i = new Intent("com.example.activityregistrator.USER_MESSAGE");
	    	i.putExtra("userMessage", "DONE");
			sendBroadcast(i);
		}else{
			Intent i = new Intent("com.example.activityregistrator.USER_MESSAGE");
	    	i.putExtra("userMessage", "FAIL");
			sendBroadcast(i);
		}
	}
	
	
}