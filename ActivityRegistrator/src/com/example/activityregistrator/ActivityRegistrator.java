package com.example.activityregistrator;

import java.util.Date;

import android.os.Bundle;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ActivityRegistrator extends Activity {
	public static final String TAG = "ActivityRegistrator";
	private BroadcastReceiver receiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		
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
	
	private void initReciever(){
		receiver = new BroadcastReceiver() {
		    @Override
		    public void onReceive(Context context, Intent intent) {
		    	
		    	
		    	int type = intent.getIntExtra("activityType", 0);
		    	
		    }
		  };
		  
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.example.activityregistrator.LINEAR_ACCELERATION_DATA");
		registerReceiver(receiver, filter);
	}

}
