package com.example.activityregistrator;

import java.util.Date;

import myutil.MyUtil;
import android.R.color;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Color;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class ActivityMarker extends Activity {

	private final int STOPPED = 0;
	private final int STARTED = 1;
	private int markerState;
	private long initTime;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_activity_marker);
		// keep screen on
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		markerState = STOPPED;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_marker, menu);
		return true;
	}
	
	public void startStop(View v){
		resetColors();
		if(markerState == STOPPED){
			startMarker();
		}else{
			
		}
	}
	public void accelerate(View v){
		if(markerState == STOPPED)
			return;
		resetColors();
		((Button)v).setBackgroundColor(Color.GREEN);
		
		save(ActivityRegistrator.ACCELERATING);
	}
	public void notMoving(View v){
		if(markerState == STOPPED)
			return;
		resetColors();
		((Button)v).setBackgroundColor(Color.BLUE);
		
		save(ActivityRegistrator.NOT_MOVING);
	}
	public void cruise(View v){
		if(markerState == STOPPED)
			return;
		resetColors();
		((Button)v).setBackgroundColor(Color.YELLOW);
		
		save(ActivityRegistrator.CRUISE);
	}
	public void ignore(View v){
		if(markerState == STOPPED)
			return;
		resetColors();
		((Button)v).setBackgroundColor(Color.GRAY);
		
		save(ActivityRegistrator.IGNORE);
	}
	public void breaking(View v){
		if(markerState == STOPPED)
			return;
		resetColors();
		((Button)v).setBackgroundColor(Color.RED);
		
		save(ActivityRegistrator.BREAKING);
	}
	
	private void resetColors(){
		((Button)findViewById(R.id.button1)).setBackgroundResource(android.R.drawable.btn_default);
		((Button)findViewById(R.id.button2)).setBackgroundResource(android.R.drawable.btn_default);
		((Button)findViewById(R.id.button3)).setBackgroundResource(android.R.drawable.btn_default);
		((Button)findViewById(R.id.button4)).setBackgroundResource(android.R.drawable.btn_default);
		((Button)findViewById(R.id.button5)).setBackgroundResource(android.R.drawable.btn_default);
		((Button)findViewById(R.id.button6)).setBackgroundResource(android.R.drawable.btn_default);
	}
	
	private void startMarker(){
		markerState = STARTED;
		initTime = System.currentTimeMillis();
		
		((Button)findViewById(R.id.button1)).setEnabled(false);
		
		((TextView) findViewById(R.id.startDate)).setText(new Date().toString());
		
		String[] s = {"T " + new Date().toString()};
		MyUtil.writeToSDFile(s, "ActivityRegistrator/mark", "sensorMarks_.txt", true);
	}
	
	private long getServiceTime(){
    	return System.currentTimeMillis() - initTime;
    }
	
	private void save(int userActivity){
		String[] s = {getServiceTime()+" "+userActivity};
		MyUtil.writeToSDFile(s, "ActivityRegistrator/mark", "sensorMarks_.txt", true);
		((TextView) findViewById(R.id.endDate)).setText(new Date().toString());
	}
}
