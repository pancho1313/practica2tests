package activityrecognition;

import features.IFeatures;
import features.MyFeatures1;
import features.MyFeatures2;
import features.MyFeatures3;
import features.MyFeatures4;
import windowdata.IWindowData;
import windowdata.WindowHalfOverlap;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

public class SvmBicycleCarRecognizer {
	private String TAG = "SvmBicycleCarRecognizer";
	private static final int wSize = 128;
	private static final int floatsPerWindowData = MyFeatures4.FLOATS_PER_WINDOW_DATA;
	
	public SvmBicycleCarRecognizer(){}
	
	public void predictState(Intent intent, SvmRecognizerIntentService svmRecognizerIntentService) {
		String modelFile = Environment.getExternalStorageDirectory() + "/trainingBC.128.4.txt.model";
		
		//libsvm scale ranges
		float scaleH = 1f;
		float scaleL = -1f;
		
		// merge 128.4
		float[] featuresMin = {
				0.08994215999999999f,
				1.5994453f,
				2.318644f,
				86.19423f,
				0.062371057f,
				1.5402364f,
				1.3249372f,
				54.699028f,
                0f,0f,0f,0f,0f,0f,0f,0f
		};
		float[] featuresMax = {
				2.892728f,
				18.43066f,
				2403.788f,
				2820.9966f,
				3.2103055f,
				22.734346f,
				3213.3376f,
				3033.125f,
                1f,1f,1f,1f,1f,1f,1f,1f
		};
		
		Log.d(TAG, "SvmBicycleCarRecognizer");
		svmRecognizerIntentService.predictState(intent, modelFile, wSize, floatsPerWindowData, scaleH, scaleL, featuresMin, featuresMax);
	}
	
	public static IWindowData getNewWindowData(){
		return new WindowHalfOverlap(wSize, floatsPerWindowData);
	}
	
	public static IFeatures getMyFeatures(){
		return new MyFeatures4();
	}
}
