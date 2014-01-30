package activityrecognition;

import features.IFeatures;
import features.MyFeatures1;
import features.MyFeatures2;
import features.MyFeatures3;
import windowdata.IWindowData;
import windowdata.WindowHalfOverlap;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

public class SvmBicycleRecognizer {
	private String TAG = "SvmBicycleRecognizer";
	private static final int wSize = 64;
	private static final int floatsPerWindowData = MyFeatures1.FLOATS_PER_WINDOW_DATA;
	
	public SvmBicycleRecognizer(){}
	
	public void predictState(Intent intent, SvmRecognizerIntentService svmRecognizerIntentService) {
		String modelFile = Environment.getExternalStorageDirectory() + "/trainingBike.64.1.txt.model";
		
		//libsvm scale ranges
		float scaleH = 1f;
		float scaleL = -1f;
		
		// 64.1
		float[] featuresMin = {
                0.5763978f,
                0.6271618f,
                41.775806f,
                207.24507f
		};
		float[] featuresMax = {
		                5.150639f,
		                16.077961f,
		                4166.345f,
		                1696.9004f
		};
		/* 128.3
		float[] featuresMin = {
				0.5882687599999999f,
				1.0850142f,
				88.283646f,
				598.6638f,
				0f,
				0f,
				0f,
				0f
		};
		float[] featuresMax = {
				4.472457f,
				21.731827f,
				5934.718f,
				4212.2583f,
				1f,
				1f,
				1f,
				1f
		};
		*/
		Log.d(TAG, "SvmBicycleRecognizer");
		svmRecognizerIntentService.predictState(intent, modelFile, wSize, floatsPerWindowData, scaleH, scaleL, featuresMin, featuresMax);
	}
	
	public static IWindowData getNewWindowData(){
		return new WindowHalfOverlap(wSize, floatsPerWindowData);
	}
	
	public static IFeatures getMyFeatures(){
		return new MyFeatures1();
	}
}
