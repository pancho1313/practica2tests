package activityrecognition;

import java.util.Properties;

import myutil.AssetsPropertyReader;

import features.IFeatures;
import features.MyFeatures1;
import features.MyFeatures2;
import features.MyFeatures3;
import features.MyFeatures4;
import windowdata.IWindowData;
import windowdata.WindowHalfOverlap;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

public class SvmBicycleCarRecognizer {
	private String TAG = "SvmBicycleCarRecognizer";
	private static final int wSize = 128; // 64 128
	private static final int myFeatures = 4; // 1 2 3 4
	private static final int floatsPerWindowData = getFloatsPerWindowData();
	private Properties properties;
	
	public SvmBicycleCarRecognizer(Context context){
		// .properties
        AssetsPropertyReader assetsPropertyReader = new AssetsPropertyReader(context);
        properties = assetsPropertyReader.getProperties("range."+propertiesPrefix()+"properties");
	}
	
	private String propertiesPrefix(){
		return "" + wSize + "." + myFeatures + ".";
	}
	
	public void predictState(Intent intent, SvmRecognizerIntentService svmRecognizerIntentService) {
		String modelFile = Environment.getExternalStorageDirectory() + "/trainingBC."+propertiesPrefix()+"txt.model";
		
		// read from *.properties
		float scaleH = Float.parseFloat(properties.getProperty(propertiesPrefix()+"scaleH"));
		float scaleL = Float.parseFloat(properties.getProperty(propertiesPrefix()+"scaleL"));

		int featuresLength = Integer.parseInt(properties.getProperty(propertiesPrefix()+"featuresLength"));
		float[] featuresMax = new float[featuresLength];
		float[] featuresMin = new float[featuresLength];

		for(int i = 0; i < featuresLength; i++){
		featuresMin[i] = Float.parseFloat(properties.getProperty(propertiesPrefix()+(i+1)+"L"));
		featuresMax[i] = Float.parseFloat(properties.getProperty(propertiesPrefix()+(i+1)+"H"));
		}
		
		
		Log.d(TAG, "SvmBicycleCarRecognizer");
		svmRecognizerIntentService.predictState(intent, modelFile, wSize, floatsPerWindowData, scaleH, scaleL, featuresMin, featuresMax);
	}
	
	public static IWindowData getNewWindowData(){
		return new WindowHalfOverlap(wSize, floatsPerWindowData);
	}
	
	public static IFeatures getMyFeatures(){
		switch(myFeatures){
		case 2:
			return new MyFeatures2();
		case 3:
			return new MyFeatures3();
		case 4:
			return new MyFeatures4();
		default:
			return new MyFeatures1();
		}
		
	}
	
	private static int getFloatsPerWindowData(){
		switch(myFeatures){
		case 2:
			return MyFeatures2.FLOATS_PER_WINDOW_DATA;
		case 3:
			return MyFeatures3.FLOATS_PER_WINDOW_DATA;
		case 4:
			return MyFeatures4.FLOATS_PER_WINDOW_DATA;
		default:
			return MyFeatures1.FLOATS_PER_WINDOW_DATA;
		}
	}
}
