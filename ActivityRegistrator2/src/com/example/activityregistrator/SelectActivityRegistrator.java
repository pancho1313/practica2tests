package com.example.activityregistrator;

import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.Scanner;

import windowdata.WindowHalfOverlap;

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

    private Properties properties;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_activity_registrator);
		
		// keep screen on
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		// .properties
		AssetsPropertyReader assetsPropertyReader = new AssetsPropertyReader(this);
        properties = assetsPropertyReader.getProperties("ActivityRegistrator.properties");
         
        showProperties();
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
		Log.d(TAG, "[ 1.2627743 6.3329363 247.03587 407.84216 ] (input)");
		features = new float[]{1.2627743f, 6.3329363f, 247.03587f, 407.84216f};
		
		///////////////////////////////read from .properties
		float scaleH = 1;
		float scaleL = -1;

		float[] featuresMax = new float[features.length];
		float[] featuresMin = new float[features.length];
		/*
		1 0.7140552 5.150639
		2 0.66037196 16.077961
		3 64.83476 3977.108
		4 255.21948 1688.7849
		*/
		featuresMin[0] = 0.7140552f;
		featuresMin[1] = 0.66037196f;
		featuresMin[2] = 64.83476f;
		featuresMin[3] = 255.21948f;
		
		featuresMax[0] = 5.150639f;
		featuresMax[1] = 16.077961f;
		featuresMax[2] = 3977.108f;
		featuresMax[3] = 1688.7849f;
		
		///////////////precalculate data
		float scaleZero = (scaleH + scaleL)/2;
		float scaleDif = scaleH - scaleL;
		float[] featuresZero = new float[features.length];
		float[] featuresDif = new float[features.length];
		for(int i = 0; i < features.length; i++){
			featuresZero[i] = (featuresMax[i] + featuresMin[i])/2;
			featuresDif[i] = featuresMax[i] - featuresMin[i];
		}
		
		////////////////runtime calculation
		for(int i = 0; i < features.length; i++){
			features[i] = scaleZero + ((features[i] - featuresZero[i]) * scaleDif)/featuresDif[i];
		}
		
		////////////////
		String s = "[ ";
		for(float f : features){
			s+= f+" ";
		}
		s+="] (your scale)";
		Log.d(TAG, s);
		Log.d(TAG, "[ -0.752639 -0.264144 -0.906857 -0.787073 ] (libsvm.scale)");
	}
}
