package activityrecognition;

import features.IFeatures;
import features.MyFeatures1;
import windowdata.IWindowData;
import windowdata.WindowHalfOverlap;
import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

public class SvmCarRecognizerIntentService extends SvmRecognizerIntentService {
	private static final int wSize = 64;
	private static final int floatsPerWindowData = MyFeatures1.FLOATS_PER_WINDOW_DATA;
	
	public SvmCarRecognizerIntentService() {
        super("SvmCarRecognizerIntentService");
    }
	
	@Override
	protected void onHandleIntent(Intent intent) {
		String modelFile = Environment.getExternalStorageDirectory() + "/trainingCar.64.1.txt.model";
		
		//libsvm scale ranges
		float scaleH = 1f;
		float scaleL = -1f;
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
		
		predictState(intent, modelFile, wSize, floatsPerWindowData, scaleH, scaleL, featuresMin, featuresMax);
	}
	
	public static IWindowData getNewWindowData(){
		return new WindowHalfOverlap(wSize, floatsPerWindowData);
	}
	
	public static IFeatures getMyFeatures(){
		return new MyFeatures1();
	}
}
