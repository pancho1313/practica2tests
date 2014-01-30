package activityrecognition;

import features.IFeatures;
import features.MyFeatures1;
import features.MyFeatures3;
import windowdata.IWindowData;
import windowdata.WindowHalfOverlap;
import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

public class SvmCarRecognizer {
	private static final int wSize = 128;
	private static final int floatsPerWindowData = MyFeatures3.FLOATS_PER_WINDOW_DATA;
	
	public SvmCarRecognizer(){}
	
	public void predictState(Intent intent, SvmRecognizerIntentService svmRecognizerIntentService) {
		String modelFile = Environment.getExternalStorageDirectory() + "/trainingCar.128.3.txt.model";
		
		//libsvm scale ranges
		float scaleH = 1f;
		float scaleL = -1f;
		
		float[] featuresMin = {
				0.121566325f,
				1.2980267f,
				4.071516f,
				118.82734f,
				0f,
				0f,
				0f,
				0f
		};
		float[] featuresMax = {
				2.545794f,
				19.487278f,
				2590.1914f,
				2180.7207f,
				1f,
				1f,
				1f,
				1f
		};
		
		svmRecognizerIntentService.predictState(intent, modelFile, wSize, floatsPerWindowData, scaleH, scaleL, featuresMin, featuresMax);
	}
	
	public static IWindowData getNewWindowData(){
		return new WindowHalfOverlap(wSize, floatsPerWindowData);
	}
	
	public static IFeatures getMyFeatures(){
		return new MyFeatures3();
	}
}
