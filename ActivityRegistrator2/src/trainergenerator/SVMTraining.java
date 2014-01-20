package trainergenerator;


import java.io.*;
import java.util.*;

import myutil.MyUtil;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import windowdata.IWindowData;
import windowdata.WindowHalfOverlap;
import features.IFeatures;
import features.MyFeatures;

public class SVMTraining {
	
	private static String TAG = "SVMTraining"; 
	
	private static double vecLength(float[] v){
    	return Math.sqrt(Math.pow(v[0], 2)+Math.pow(v[1], 2)+Math.pow(v[2], 2));
    }
	
	private void save(String[] textToFile, String fileName, boolean append){
		File file = new File(fileName);

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
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	private static int getNumberOfLines(File file){
		int totalLines = 1;
		try {
			LineNumberReader lnr;
			lnr = new LineNumberReader(new FileReader(file));
			lnr.skip(Long.MAX_VALUE);
			totalLines = lnr.getLineNumber();
		    lnr.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return totalLines;
	}
	
	private static boolean getNumberOfLines(File file, int total, float mean){
		
		try {
			double difSum = 0;
			long last, actual;
			int nLines = 0;
			
			Scanner scan = new Scanner(file);
	    	scan.useLocale(Locale.US);
	    	
	    	if(scan.hasNextLine()){
	    		scan.nextLine();
	    		if(scan.hasNextLong() && scan.hasNextLine()){
	    			last = scan.nextLong();
	    			nLines++;
	    			scan.nextLine();
	    		}else{
	    			scan.close();
	    			return false;
	    		}
	    	}else{
	    		scan.close();
	    		return false;
	    	}
	    	
	    	while(scan.hasNextLong()){
	    		actual = scan.nextLong();
	    		nLines++;
	    		difSum += actual - last;
	    		last = actual;
	    		
	    		if(scan.hasNextLine()){
	    			scan.nextLine();
	    		}else{
	    			break;
	    		}
	    	}
	    	
	    	scan.close();
	    	
	    	total = nLines;
	    	mean = (float) difSum/(total-1);
	    	
	    	return true;
	    } catch (FileNotFoundException e1) {
	    	return false;
	    }
	}
	
	public static boolean generateTrainingFile(
			String sensorDataFolder,
			String sensorDataFile,
			String sensorMarksFolder,
			String sensorMarksFile,
			String outFolder,
			String outFile,
			Context context){
	    IWindowData windowData = new WindowHalfOverlap(64);
	    IFeatures myFeatures = new MyFeatures();
	    
	    Scanner scanSD = null, scanSM = null;
	    File sensorData = MyUtil.getSDFile(sensorDataFolder, sensorDataFile);
	    File sensorMarks = MyUtil.getSDFile(sensorMarksFolder, sensorMarksFile);
	    
	    
	    
	    try {
	    	scanSD = new Scanner(sensorData);
	    	scanSD.useLocale(Locale.US);
	    } catch (FileNotFoundException e1) {
	    	return false;
	    }
	    
	    try {
	    	scanSM = new Scanner(sensorMarks);
	    	scanSM.useLocale(Locale.US);
	    } catch (FileNotFoundException e1) {
	    	scanSD.close();
	    	return false;
	    }
	    
	    
	    // progress bar
	    int total = getNumberOfLines(sensorData);
	    
	    /*
	    int total = 0;
	    float mean = 0;
	    boolean correct = getNumberOfLines(sensorData, total, mean); 
	    if(!correct){
	    	scanSM.close();
	    	scanSD.close();
	    	return false;
	    }
	    
	    Intent intent = new Intent("com.example.activityregistrator.UPDATE_HZ");
		intent.putExtra("hz", 1/mean);
		context.sendBroadcast(intent);
		*/
	    float done = 0;
	    
	    
	    Long prevTimeSM, postTimeSM, timeSD = -1l, lastTimeSD = 0l, firstTimeSD = -1l;
    	int prevLabel, postLabel;
    	
	    if(scanSM.hasNextLine()){
	    	scanSM.nextLine();
	    	prevTimeSM = scanSM.nextLong();
	    	prevLabel = scanSM.nextInt();

	    	scanSD.nextLine();
	    	Intent intent = new Intent("com.example.activityregistrator.UPDATE_PROGRESS");
	    	intent.putExtra("progress", (float)((++done)/total)*100);
			context.sendBroadcast(intent);
	    
	    	// find next time and label
		    while(scanSM.hasNextLine()){
		    	scanSM.nextLine();
		    	if(!scanSM.hasNextLong())
		    		break;
		    	
		    	Log.d(TAG,"prevTimeSM: "+prevTimeSM+"; prevLabel: "+prevLabel);
		    	
		    	postTimeSM = scanSM.nextLong();
		    	postLabel = scanSM.nextInt();
		    	
		    	Log.d(TAG,"postTimeSM: "+postTimeSM+"; postLabel: "+postLabel);
		    	
		    	// each window has unique label
		    	windowData.clean();
		    	
		    	// search windowData beginning
		    	// assumption: register only label>0  -->  IGNORE<=0
		    	while(prevLabel > 0){
		    		
		    		if(timeSD<0){
		    			if(!scanSD.hasNextLong()){
		    				break;
		    			}
		    			timeSD = scanSD.nextLong();
		    		}
		    		
		    		
		    		// used to calculate hz
		    		lastTimeSD = timeSD;
		    		if(firstTimeSD < 0)
		    			firstTimeSD = timeSD;
		    		
		    		if(timeSD >= prevTimeSM){
		    			if(timeSD <= postTimeSM){
		    				Log.d(TAG,"timeSD: "+timeSD);
		    				
		    				// add x y z to windowData
		    				float[] linearAccel = {scanSD.nextFloat(), scanSD.nextFloat(), scanSD.nextFloat()};
		    				float[] gData = {scanSD.nextFloat(), scanSD.nextFloat(), scanSD.nextFloat()};
		    				
		    				if(windowData.addData((float)vecLength(linearAccel))){
		    		    		// we have a complete windowData
		    		    		
		    		    		// calculate features
		    		    		float [] features = myFeatures.getFeatures(windowData);
		    		    		
		    		    		// save to libsvm file with the correct label
		    		    		String[] textToFile = new String[1];
		    		    		textToFile[0] = prevLabel + " ";
		    		    		for(int i = 0; i < features.length; i++){
		    		    			textToFile[0] += (i+1)+":"+features[i]+" "; 
		    		    		}
		    		    		MyUtil.writeToSDFile(textToFile, outFolder, outFile, true);
		    		    	}
		    				
		    				if(scanSD.hasNextLine()){
		    					scanSD.nextLine();
		    					intent = new Intent("com.example.activityregistrator.UPDATE_PROGRESS");
		    					intent.putExtra("progress", (float)((++done)/total)*100);
		    					context.sendBroadcast(intent);
		    					timeSD = -1l; // get nextLong()
		    				}else{
		    					break;
		    				}
		    			}else{
		    				break;
		    			}
		    		}else{
		    			if(scanSD.hasNextLine()){
	    					scanSD.nextLine();
	    					intent = new Intent("com.example.activityregistrator.UPDATE_PROGRESS");
	    					intent.putExtra("progress", (float)((++done)/total)*100);
	    					context.sendBroadcast(intent);
	    					timeSD = -1l; // get nextLong()
	    				}else{
	    					break;
	    				}
		    		}
		    	}
		    	
		    	prevTimeSM = postTimeSM;
		    	prevLabel = postLabel;
		    }
	    }
	    
	    scanSD.close();
	    scanSM.close();
	    
	    Intent intent = new Intent("com.example.activityregistrator.UPDATE_HZ");
		intent.putExtra("hz", done/(((float)(lastTimeSD - firstTimeSD))/1000f));
		context.sendBroadcast(intent);
		
		Log.d(TAG,"[firstTimeSD,lastTimeSD,done]: "+firstTimeSD+" "+lastTimeSD+" "+done);
	    
	    return true;
	}
}
