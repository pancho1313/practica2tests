package com.example.gpstest;

import android.app.IntentService;
import android.content.Intent;
import myutil.*;

public class SaveFileIntentService extends IntentService {

	public SaveFileIntentService() {
        super("SaveFileIntentService");
    }
	
	@Override
	protected void onHandleIntent(Intent intent) {
		String[] s = intent.getStringArrayExtra("data");
		MyUtil.writeToSDFile(s, "GPSActivity/", "gpsData_.txt", true);
	}
}
