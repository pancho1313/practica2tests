package activityrecognition;


import java.io.*;
import java.util.*;

import windowdata.IWindowData;
import windowdata.WindowHalfOverlap;
import features.IFeatures;

public class SVMTraining {
	
	public static double vecLength(float[] v){
    	return Math.sqrt(Math.pow(v[0], 2)+Math.pow(v[1], 2)+Math.pow(v[2], 2));
    }
	
	public static void save(String[] textToFile, String fileName, boolean append){
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
	/*
	public static void main(String[] args) {
	    IWindowData windowData = new WindowHalfOverlap(64);
	    IFeatures myFeatures = new MyFeatures();
	    
		String sensorDataPath = args[0];
		String sensorMarksPath = args[1];
		String fileName = args[2];
	    Scanner scanSD = null, scanSM = null;
	    File sensorData = new File(sensorDataPath);
	    File sensorMarks = new File(sensorMarksPath);
	    try {
	    	scanSD = new Scanner(sensorData);
	    } catch (FileNotFoundException e1) {
	            e1.printStackTrace();
	    }
	    
	    try {
	    	scanSM = new Scanner(sensorMarks);
	    } catch (FileNotFoundException e1) {
	            e1.printStackTrace();
	    }
	    
	    
	    Long prevTimeSM, postTimeSM, timeSD = -1l;
    	int prevLabel, postLabel;
	    if(scanSM.hasNextLine()){
	    	scanSM.nextLine();
	    	prevTimeSM = scanSM.nextLong();
	    	prevLabel = scanSM.nextInt();
	    	
	    	scanSD.nextLine();
	    
	    	// find next time and label
		    while(scanSM.hasNextLine()){
		    	scanSM.nextLine();
		    	postTimeSM = scanSM.nextLong();
		    	postLabel = scanSM.nextInt();
		    	
		    	// each window has unique label
		    	windowData.clean();
		    	
		    	// search windowData begining
		    	// assumption: register only label>0  -->  IGNORE<=0
		    	while(prevLabel > 0){
		    		timeSD = timeSD<0 ? scanSD.nextLong() : timeSD;
		    		if(timeSD >= prevTimeSM){
		    			if(timeSD <= postTimeSM){
		    				// add x y z to windowData
		    				float[] linearAccel = {scanSD.nextFloat(), scanSD.nextFloat(), scanSD.nextFloat()};
		    				
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
		    		    		save(textToFile, fileName, true);
		    		    	}
		    				
		    				if(scanSD.hasNextLine()){
		    					scanSD.nextLine();
		    					timeSD = -1l; // get nextLong()
		    				}else{
		    					break;
		    				}
		    			}else{
		    				break;
		    			}
		    		}
		    	}
		    	
		    	prevTimeSM = postTimeSM;
		    	prevLabel = postLabel;
		    }
	    }
	    
	    
	}
	*/
}
