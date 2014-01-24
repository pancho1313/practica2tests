package com.example.gpstest;



import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class GPSActivity extends Activity {

	private BroadcastReceiver LogReciever;
	private String log;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gps);
		
		// keep screen on
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		((Button)findViewById(R.id.start)).setEnabled(true);
		((Button)findViewById(R.id.stop)).setEnabled(false);
		
		initRecievers();
		log = "";
	}
	
	@Override
	public void onResume() {
	    super.onResume();

	    ((TextView)findViewById(R.id.textView)).setText(log);
	}
	
	@Override
    public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(LogReciever);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.g, menu);
		return true;
	}
	
	public void startButton(View v){
		((Button)v).setEnabled(false);
		((Button)findViewById(R.id.stop)).setEnabled(true);
		
		Intent intent = new Intent(this, GPSservice.class);
		startService(intent);
	}
	
	public void stopButton(View v){
		((Button)v).setEnabled(false);
		((Button)findViewById(R.id.stop)).setEnabled(false);
		
		Intent i = new Intent("com.example.gpstest.END_GPS_SERVICE");
		sendBroadcast(i);
	}
	
	private void initRecievers(){
    	LogReciever = new BroadcastReceiver() {
		    @Override
		    public void onReceive(Context context, Intent intent) {
		    	log = intent.getStringExtra("log")+"\n"+log;
		    	((TextView)findViewById(R.id.textView)).setText(log);
		    }
		  };
		  
		 IntentFilter filter = new IntentFilter();
		 filter.addAction("com.example.gpstest.LOG");
		 registerReceiver(LogReciever, filter);
	}

}
