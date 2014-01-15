package activityrecognition;


import java.io.*;
import java.util.*;

public class SVMTraining {
	public static void main(String[] args) {
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
	    
	    
	    Long prevTimeSM, postTimeSM;
    	int prevLabel, postLabel;
	    if(scanSM.hasNextLine()){
	    	scanSM.nextLine();
	    	prevTimeSM = scanSM.nextLong();
	    	prevLabel = scanSM.nextInt();
	    
	    	// find next time and label
		    while(scanSM.hasNextLine()){
		    	scanSM.nextLine();
		    	postTimeSM = scanSM.nextLong();
		    	postLabel = scanSM.nextInt();
		    	
		    	// search windowData begining
		    	// assumption: register only label>0  -->  IGNORE<0
		    	while(prevLabel > 0){
		    		scanSM.nextLine();
		    		Long timeSD = scanSD.nextLong();
		    		if(timeSD >= prevTimeSM){
		    			if(timeSD <= postTimeSM){
		    				// add x y z to windowData
		    				float x = scanSD.nextFloat();
		    				float y = scanSD.nextFloat();
		    				float z = scanSD.nextFloat();
		    				
		    				// TODO
		    				
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