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

/**
 * activity to select between ActivityMarker, ActivityRegistrator and TrainingFileGenerator,
 * it also show windowData size and type of features used in TrainingFileGenerator
 * @author fhafon
 *
 */
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
}
