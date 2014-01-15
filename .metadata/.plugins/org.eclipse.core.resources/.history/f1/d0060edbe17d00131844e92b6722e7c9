package com.example.activityregistrator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

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
		writeToSDFile(s, "ActivityRegistrator", "test.txt");
		Log.d(TAG, "saved file");
	}
	
	/** Method to write ascii text characters to file on SD card. Note that you must add a 
	   WRITE_EXTERNAL_STORAGE permission to the manifest file or this method will throw
	   a FileNotFound Exception because you won't have write permission. */

	private void writeToSDFile(String[] textToFile, String folder /* "myFolder" */, String fileName){

	    // Find the root of the external storage.
	    // See http://developer.android.com/guide/topics/data/data-  storage.html#filesExternal

	    File root = android.os.Environment.getExternalStorageDirectory(); 
	    

	    // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder

	    File dir = new File (root.getAbsolutePath() + "/" + folder);
	    dir.mkdirs();
	    File file = new File(dir, fileName);

	    try {
	        FileOutputStream f = new FileOutputStream(file);
	        PrintWriter pw = new PrintWriter(f);
	        for(int i = 0; i < textToFile.length; i++){
	        	pw.println(textToFile[i]);
	        }
	        pw.flush();
	        pw.close();
	        f.close();
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	        Log.d(TAG, "******* File not found. Did you" +
	                " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
	        Log.i(TAG, "******* File not found. Did you" +
	                " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
}
