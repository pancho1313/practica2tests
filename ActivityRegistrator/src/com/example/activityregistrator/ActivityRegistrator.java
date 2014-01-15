package com.example.activityregistrator;

import java.util.Date;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class ActivityRegistrator extends Activity {
	public static final String TAG = "ActivityRegistrator";
	
	public static final int IGNORE = -1; // Must be <0
	public static final int NOT_MOVING = 1; // Must be >0
	public static final int CRUISE = 2; // Must be >0
	public static final int ACCELERATING = 3; // Must be >0
	public static final int BREAKING = 4; // Must be >0

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		
		//keep screen on
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		Button stop = (Button) findViewById(R.id.stop);
		Button start = (Button) findViewById(R.id.start);
		
		stop.setEnabled(false);
		start.setEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.register, menu);
		return true;
	}
	
	public void start(View v){
		Intent intent = new Intent(this, LinearAccelerationService.class);
		startService(intent);
		stopUI();
	}
	
	public void stop(View v){
		Intent i = new Intent("com.example.activityregistrator.END_AND_SAVE");
		sendBroadcast(i);
		
		TextView endDate = (TextView) findViewById(R.id.endDate);
		endDate.setText(new Date().toString());
	}
	
	private void stopUI(){
		
		Button stop = (Button) findViewById(R.id.stop);
		Button start = (Button) findViewById(R.id.start);
		
		stop.setEnabled(true);
		start.setEnabled(false);
		
		TextView startDate = (TextView) findViewById(R.id.startDate);
		startDate.setText(new Date().toString());
	}
}
