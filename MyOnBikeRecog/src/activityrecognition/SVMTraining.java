package activityrecognition;


import java.io.*;
import java.util.*;

import windowdata.IWindowData;
import windowdata.WindowHalfOverlap;
import features.IFeatures;
import features.MyFeatures;

public class SVMTraining {
	
	public static double vecLength(float[] v){
    	return Math.sqrt(Math.pow(v[0], 2)+Math.pow(v[1], 2)+Math.pow(v[2], 2));
    }
	
	public static void main(String[] args) {
	    IWindowData windowData = new WindowHalfOverlap();
	    IFeatures myFeatures = new MyFeatures();
		
		String sensorDataPath = args[1];
		String sensorMarksPath = args[2];
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
		    		    	}
		    				
		    				// TODO
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
}
