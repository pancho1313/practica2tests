package com.example.activityregistrator;

import trainergenerator.SVMTraining;
import android.app.IntentService;
import android.content.Intent;

public class TrainingGeneratorIntentService extends IntentService {

	
	public TrainingGeneratorIntentService() {
        super("TrainingGeneratorIntentService");
    }
	
	@Override
	protected void onHandleIntent(Intent intent) {
		if(SVMTraining.generateTrainingFile(
				intent.getStringExtra("sensorDataFolder"), 
				intent.getStringExtra("sensorDataFile"),
				intent.getStringExtra("sensorMarksFolder"),
				intent.getStringExtra("sensorMarksFile"),
				intent.getStringExtra("outFolder"),
				intent.getStringExtra("outFile"),
				this.getApplicationContext())){
			
			Intent i = new Intent("com.example.activityregistrator.UPDATE_PROGRESS");
	    	i.putExtra("progress", 100f);
			sendBroadcast(i);
		}else{
			Intent i = new Intent("com.example.activityregistrator.UPDATE_PROGRESS");
	    	i.putExtra("progress", -1f);
			sendBroadcast(i);
		}
	}
	
	
}