package activityrecognition;

import features.IFeatures;
import features.MyFeatures1;
import windowdata.IWindowData;
import windowdata.WindowHalfOverlap;
import android.content.Intent;
import android.os.Environment;

public class SvmBicycleRecognizer {
	private static final int wSize = 64;
	private static final int floatsPerWindowData = MyFeatures1.FLOATS_PER_WINDOW_DATA;
	
	public SvmBicycleRecognizer(){}
	
	public void predictState(Intent intent, SvmRecognizerIntentService svmRecognizerIntentService) {
		String modelFile = Environment.getExternalStorageDirectory() + "/trainingBike.128.3.txt.model";
		
		//libsvm scale ranges
		float scaleH = 1f;
		float scaleL = -1f;
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
		
		svmRecognizerIntentService.predictState(intent, modelFile, wSize, floatsPerWindowData, scaleH, scaleL, featuresMin, featuresMax);
	}
	
	public static IWindowData getNewWindowData(){
		return new WindowHalfOverlap(wSize, floatsPerWindowData);
	}
	
	public static IFeatures getMyFeatures(){
		return new MyFeatures1();
	}
}
