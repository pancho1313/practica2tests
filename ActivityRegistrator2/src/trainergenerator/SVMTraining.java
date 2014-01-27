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
	
	private void foo(){
		BufferedReader br = null;
		 
		try {
 
			String sCurrentLine;
 
			br = new BufferedReader(new FileReader("C:\\testing.txt"));
 
			while ((sCurrentLine = br.readLine()) != null) {
				System.out.println(sCurrentLine);
			}
 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		///////////////////////////
		String s = "10140 18.674652 4.2338295 -0.03422594 0.7854203 6.3772726 7.408367";

        Scanner scan =new Scanner(s);
        scan.useLocale(Locale.US);
        if(scan.hasNextLong())
        	Log.d("SvmRecognizer","scan.nextLong(): "+scan.nextLong());
        if(scan.hasNextFloat()){
        	Log.d("SvmRecognizer","scan.nextFloat(): "+scan.nextFloat());
        	Log.d("SvmRecognizer","scan.nextFloat(): "+scan.nextFloat());
        	Log.d("SvmRecognizer","scan.nextFloat(): "+scan.nextFloat());
        }
        scan.close();
	}
	
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
