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
		Log.d(TAG, "[ 70 40 15 81 ] (input)");
		features = new float[]{70, 40, 15, 81};
		/////////////////////////////////
		
		float scaleH = 1;
		float scaleL = -1;

		float[] featuresMax = new float[features.length];
		float[] featuresMin = new float[features.length];
		/*
		760 16 255
		761 15 245
		762 14 247
		763 18 255
		*/
		featuresMin[0] = 16;
		featuresMin[1] = 15;
		featuresMin[2] = 14;
		featuresMin[3] = 18;
		
		featuresMax[0] = 255;
		featuresMax[1] = 245;
		featuresMax[2] = 247;
		featuresMax[3] = 255;
		
		
		for(int i = 0; i < features.length; i++){
			float zero = (featuresMax[i] + featuresMin[i])/2;
			float dif = features[i] - zero;
			features[i] = (dif * (scaleH - scaleL))/(featuresMax[i] - featuresMin[i]);
		}
		////////////////
		String s = "[ ";
		for(float f : features){
			s+= f+" ";
		}
		s+="] (your's)";
		Log.d(TAG, s);
		Log.d(TAG, "[ -0.548117 -0.782609 -0.991416 -0.468354 ] (libsvm)");
	}
}
