package com.example.activityregistrator;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import myutil.*;

public class SaveFileIntentService extends IntentService {

	private String TAG = ActivityRegistrator.TAG;
	
	public SaveFileIntentService() {
        super("SaveFileIntentService");
        Log.d(TAG, "SaveFileIntentService()");
    }
	
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "onHandleIntent()");
		String[] s = intent.getStringArrayExtra("lastData");
		MyUtil.writeToSDFile(s, "ActivityRegistrator/record", "sensorData.txt", true);
		Log.d(TAG, "saved file");
	}
	
	
}
