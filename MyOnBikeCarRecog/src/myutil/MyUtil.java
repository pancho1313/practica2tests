package myutil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;


import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.Log;

public class MyUtil {

	private static String TAG = "MyUtil";
	
	/** Method to write ascii text characters to file on SD card. Note that you must add a 
	   WRITE_EXTERNAL_STORAGE permission to the manifest file or this method will throw
	   a FileNotFound Exception because you won't have write permission. */

	public static void writeToSDFile(String[] textToFile, String folder /* "myFolder" */, String fileName, boolean append){

	    // Find the root of the external storage.
	    // See http://developer.android.com/guide/topics/data/data-  storage.html#filesExternal

	    File root = android.os.Environment.getExternalStorageDirectory(); 
	    

	    // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder

	    File dir = new File (root.getAbsolutePath() + "/" + folder);
	    dir.mkdirs();
	    File file = new File(dir, fileName);

	    try {
	        FileOutputStream f = new FileOutputStream(file, append);
	        PrintWriter pw = new PrintWriter(f);
	        for(int i = 0; i < textToFile.length; i++){
	        	pw.println(textToFile[i]);
	        }
	        pw.flush();
	        pw.close();
	        f.close();
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	        Log.e(TAG, "******* File not found. Did you" +
	                " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	public static File getSDFile(String folder /* "myFolder" */, String fileName){

	    File root = android.os.Environment.getExternalStorageDirectory(); 
	    File dir = new File (root.getAbsolutePath() + "/" + folder);
	    File file = new File(dir, fileName);

	    return file;
	}
	
	public static double vecLength(float[] v){
    	return Math.sqrt(Math.pow(v[0], 2)+Math.pow(v[1], 2)+Math.pow(v[2], 2));
    }
	
	public static void playAudio(String audioPath, Context context, float leftVolume, float rightVolume){
		
		final MediaPlayer myAudio = new MediaPlayer();
			myAudio.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				@Override
		        public void onCompletion(MediaPlayer mp) {
					Log.d("myDebug", "myAudio.release()");
		        	if (myAudio != null) {
		        		myAudio.release();
		            }
		        }
		    });
		
		AssetFileDescriptor afd;
		try {
			afd = context.getAssets().openFd(audioPath);
			try {
				myAudio.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
			} catch (IllegalArgumentException e) {
				Log.d("myDebug", "ERROR: myAudio.setDataSource()");
			} catch (IllegalStateException e) {
				Log.d("myDebug", "ERROR: myAudio.setDataSource()");
			} catch (IOException e) {
				Log.d("myDebug", "ERROR: myAudio.setDataSource()");
			}
		} catch (IOException e) {
			Log.d("myDebug", "ERROR: afd = getAssets().openFd(audioPath)");
		}
		
		
		try {
			myAudio.prepare();
		} catch (IllegalStateException e) {
			Log.d("myDebug", "ERROR: myAudio.prepare()");
		} catch (IOException e) {
			Log.d("myDebug", "ERROR: myAudio.prepare()");
		}
		myAudio.setLooping(false);
		myAudio.start();
		myAudio.setVolume(leftVolume, rightVolume);
	}
	
}
