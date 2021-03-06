package myutil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

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
	
}
