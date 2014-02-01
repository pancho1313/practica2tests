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
import features.MyFeatures1;
import features.MyFeatures2;
import features.MyFeatures3;
import features.MyFeatures4;

/**
 * generate a libsvm training file from sensorDataFile and sensorMarksFile
 * @author job
 *
 */
public class SVMTraining {
	
	private static String TAG = "SVMTraining"; 
	
	/**
	 * getNumberOfLines of a file to check the progress
	 * @param file
	 * @return
	 */
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
	

	
	private static Scanner readLine(BufferedReader br){
		
		try {
			 
			String sCurrentLine = br.readLine();
			if(sCurrentLine == null){
				Log.d(TAG,"readLine(BufferedReader br): sCurrentLine == null");
				return null;
			}
			
			Scanner scan = new Scanner(sCurrentLine);
	        scan.useLocale(Locale.US);
	        return scan;
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.d(TAG,"readLine(BufferedReader br): return null");
		return null;
	}
	
	private static void closeBR(BufferedReader br){
		try {
			if (br != null)br.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	private static BufferedReader getBR(File file){
		BufferedReader br = null;
		 
		try {
 
			br = new BufferedReader(new FileReader(file));
 
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return br;
	}
	

	/**
	 * 
	 * @param sensorDataFolder
	 * @param sensorDataFile
	 * @param sensorMarksFolder
	 * @param sensorMarksFile
	 * @param outFolder
	 * @param outFile
	 * @param context
	 * @param wSize
	 * @param floatsPerWindowData
	 * @param featuresType
	 * @return
	 */
	public static boolean generateTrainingFile(
			String sensorDataFolder,
			String sensorDataFile,
			String sensorMarksFolder,
			String sensorMarksFile,
			String outFolder,
			String outFile,
			Context context,
			int wSize,
			int floatsPerWindowData,
			int featuresType){
		
		/**
		 * windowData and type of features to use for the training file
		 */
	    IWindowData windowData = new WindowHalfOverlap(wSize, floatsPerWindowData);
	    IFeatures myFeatures = new MyFeatures1();
	    if(featuresType == MyFeatures2.FEATURES_TYPE){
	    	myFeatures = new MyFeatures2();
	    }else if(featuresType == MyFeatures3.FEATURES_TYPE){
	    	myFeatures = new MyFeatures3();
	    }else if(featuresType == MyFeatures4.FEATURES_TYPE){
	    	myFeatures = new MyFeatures4();
	    }
	    
	    Scanner scanSM = null, scanSD = null;
	    File sensorData = MyUtil.getSDFile(sensorDataFolder, sensorDataFile);
	    File sensorMarks = MyUtil.getSDFile(sensorMarksFolder, sensorMarksFile);
	    BufferedReader brSD = getBR(sensorData);
	    BufferedReader brSM = getBR(sensorMarks);
	    
	    // progress bar
	    int total = getNumberOfLines(sensorData);
	    float done = 0;
	    
	    
	    Long prevTimeSM, postTimeSM, timeSD = -1l, lastTimeSD = 0l, firstTimeSD = -1l;
    	int prevLabel, actualLabel, postLabel;
    	
    	readLine(brSM);// skip first line: date
    	scanSD = readLine(brSD);// skip first line: date
    	
	    if((scanSM=readLine(brSM)) != null){
	    	prevTimeSM = scanSM.nextLong();
	    	actualLabel = scanSM.nextInt();
	    	prevLabel = actualLabel;

	    	scanSD.close();
	    	scanSD = readLine(brSD);
	    	Intent intent = new Intent("com.example.activityregistrator.UPDATE_PROGRESS");
	    	intent.putExtra("progress", (float)((++done)/total)*100);
			context.sendBroadcast(intent);
	    
	    	// find next time and label
			scanSM.close();
		    while((scanSM=readLine(brSM)) != null){
		    	if(!scanSM.hasNextLong())
		    		break;
		    	
		    	Log.d(TAG,"prevTimeSM: "+prevTimeSM+"; actualLabel: "+actualLabel);
		    	
		    	postTimeSM = scanSM.nextLong();
		    	postLabel = scanSM.nextInt();
		    	
		    	Log.d(TAG,"postTimeSM: "+postTimeSM+"; postLabel: "+postLabel);
		    	
		    	// each window has unique label
		    	windowData.clean();
		    	
		    	// search windowData beginning
		    	// assumption: register only label>0  -->  IGNORE<=0
		    	while(actualLabel > 0){
		    		
		    		if(timeSD<0){
		    			
		    			if(!scanSD.hasNextLong()){
		    				Log.d(TAG,"break: !scanSD.hasNext(): ");
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
		    				
		    				float[] data = new float[1];
		    				
		    				// add x y z to windowData
		    				float[] linearAccel = {scanSD.nextFloat(), scanSD.nextFloat(), scanSD.nextFloat()};
		    				
		    				
		    				if(featuresType == MyFeatures2.FEATURES_TYPE){
		    					
			    				float[] gData = {scanSD.nextFloat(), scanSD.nextFloat(), scanSD.nextFloat()};
			    				data = MyFeatures2.getDataForWindowData(linearAccel, gData);
		    				}else if(featuresType == MyFeatures1.FEATURES_TYPE){
		    					data = MyFeatures1.getDataForWindowData(linearAccel);
		    				}else if(featuresType == MyFeatures3.FEATURES_TYPE){
		    					float prevState = 0;
		    					
		    					if(prevLabel <= 0)
		    						prevLabel = actualLabel;
		    					
		    					prevState = prevLabel;
		    					data = MyFeatures3.getDataForWindowData(linearAccel, prevState);
		    				}else if(featuresType == MyFeatures4.FEATURES_TYPE){
		    					// prev state
		    					float prevState = 0;
		    					
		    					if(prevLabel <= 0)
		    						prevState = actualLabel;
		    					else
		    						prevState = prevLabel;
		    					
		    					float[] gData = {scanSD.nextFloat(), scanSD.nextFloat(), scanSD.nextFloat()};
		    					
		    					data = MyFeatures4.getDataForWindowData(linearAccel, gData, prevState);
		    				}
		    				
		    				// add data to windowData
		    				if(windowData.addData(data)){// we have a complete windowData
		    					
		    		    		// calculate features
		    		    		float [] features = myFeatures.getFeatures(windowData);
		    		    		
		    		    		// save to libsvm file with the correct label
		    		    		String[] textToFile = new String[1];
		    		    		textToFile[0] = actualLabel + " ";
		    		    		for(int i = 0; i < features.length; i++){
		    		    			textToFile[0] += (i+1)+":"+features[i]+" "; 
		    		    		}
		    		    		MyUtil.writeToSDFile(textToFile, outFolder, outFile, true);
		    		    	}
		    				
		    				scanSD.close();
		    				if((scanSD=readLine(brSD)) != null){
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
		    			scanSD.close();
		    			if((scanSD=readLine(brSD)) != null){
	    					intent = new Intent("com.example.activityregistrator.UPDATE_PROGRESS");
	    					intent.putExtra("progress", (float)((++done)/total)*100);
	    					context.sendBroadcast(intent);
	    					timeSD = -1l; // get nextLong()
	    				}else{
	    					break;
	    				}
		    		}
		    	}
		    	
		    	prevLabel = actualLabel;
		    	prevTimeSM = postTimeSM;
		    	actualLabel = postLabel;
		    	
		    	scanSM.close();
		    }
	    }
	    
	    if(scanSD != null)
	    	scanSD.close();
	    if(scanSM != null)
	    	scanSM.close();
	    closeBR(brSD);
	    closeBR(brSM);
	    
	    Intent intent = new Intent("com.example.activityregistrator.UPDATE_HZ");
		intent.putExtra("hz", done/(((float)(lastTimeSD - firstTimeSD))/1000f));
		context.sendBroadcast(intent);
		
		Log.d(TAG,"[firstTimeSD,lastTimeSD,done]: "+firstTimeSD+" "+lastTimeSD+" "+done);
	    
	    return true;
	}
}
