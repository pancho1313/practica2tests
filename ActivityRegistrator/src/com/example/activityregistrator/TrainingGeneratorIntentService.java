package com.example.activityregistrator;

import trainergenerator.SVMTraining;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class TrainingGeneratorIntentService extends IntentService {
	private String TAG = "TrainingGeneratorIntentService";
	
	public TrainingGeneratorIntentService() {
        super("TrainingGeneratorIntentService");
    }
	
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "flag");
		String
		sensorDataFolder = intent.getStringExtra("sensorDataFolder"),
		sensorDataFile = intent.getStringExtra("sensorDataFile"),
		sensorMarksFolder = intent.getStringExtra("sensorMarksFolder"),
		sensorMarksFile = intent.getStringExtra("sensorMarksFile"),
		outFolder = intent.getStringExtra("outFolder"),
		outFile = intent.getStringExtra("outFile");
		
		handleCommand(intent);
		
		if(SVMTraining.generateTrainingFile(
				sensorDataFolder, 
				sensorDataFile,
				sensorMarksFolder,
				sensorMarksFile,
				outFolder,
				outFile,
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