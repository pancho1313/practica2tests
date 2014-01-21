package com.example.activityregistrator;

import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.Scanner;

import myutil.AssetsPropertyReader;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class SelectActivityRegistrator extends Activity {

	private AssetsPropertyReader assetsPropertyReader;
    private Context context;
    private Properties properties;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_activity_registrator);
		
		//keep screen on
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		// .properties
		context = this;
        assetsPropertyReader = new AssetsPropertyReader(context);
        properties = assetsPropertyReader.getProperties("ActivityRegistrator.properties");
         
        showProperties();
        
        //TODO delete
        scaleFeatures(null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.select_activity_registrator, menu);
		return true;
	}

	public void startRecorder(View view) {
	    Intent intent = new Intent(this, ActivityRegistrator.class);
	    startActivity(intent);
	}
	
	public void startRegistrator(View view) {
	    Intent intent = new Intent(this, ActivityMarker.class);
	    startActivity(intent);
	}
	
	public void startGenerator(View view) {
	    Intent intent = new Intent(this, TrainingFileGenerator.class);
	    startActivity(intent);
	}
	
	private void showProperties(){
		Enumeration<Object> em = properties.keys();
        String propertiesList = "properties:";
        while(em.hasMoreElements()){
       	 String str = (String)em.nextElement();
       	 propertiesList += "\n" +str + ": " + properties.get(str);
        }
        ((TextView)findViewById(R.id.properties)).setText(propertiesList);
	}
	
	private void scaleFeatures(float[] features){
		String TAG = "scaleFeatures";
		Log.d(TAG, "[ 1.1407704 2.9884565 171.4136 400.23703 ] (input)");
		features = new float[]{1.1407704f, 2.9884565f, 171.4136f, 400.23703f};
		/////////////////////////////////
		
		float scaleH = 1;
		float scaleL = -1;

		float[] featuresMax = new float[features.length];
		float[] featuresMin = new float[features.length];
		
		featuresMin[0] = 0.5763978f;
		featuresMin[1] = 0.6271618f;
		featuresMin[2] = 41.775806f;
		featuresMin[3] = 207.24507f;
		
		featuresMax[0] = 5.0268035f;
		featuresMax[1] = 15.986454f;
		featuresMax[2] = 4166.345f;
		featuresMax[3] = 1696.9004f;
		
		float zero = (featuresMax[i] + featuresMin[i])/2;
		float dif;
		for(int i = 0; i < features.length; i++){
			dif = zero - features[i];
			features[i] = (dif * (scaleH - scaleL))/(featuresMax[i] - featuresMin[i]);
		}
		////////////////
		Log.d(TAG, "[ ");
		for(float f : features){
			Log.d(TAG, f+" ");
		}
		Log.d(TAG, "] (your's)");
		Log.d(TAG, "[ -0.807638 -0.697996 -0.945516 -0.797683 ] (libsvm)");
	}
}
