package com.kpbird.myactivityrecognition;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognitionClient;

public class MainActivity extends Activity implements GooglePlayServicesClient.ConnectionCallbacks,GooglePlayServicesClient.OnConnectionFailedListener {

	private ActivityRecognitionClient arclient;
	private PendingIntent pIntent;
	private BroadcastReceiver receiver;
	private TextView tvActivity;
	private LinearLayout backColor;
	private Button startButton;
	private Button cancelButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tvActivity = (TextView) findViewById(R.id.tvActivity);
		backColor = (LinearLayout) findViewById(R.id.backColor);
		startButton = (Button) findViewById(R.id.requestUpdates);
		cancelButton = (Button) findViewById(R.id.cancelUpdates);
		
		//evitar que se apague la pantalla
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		int resp = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if(resp == ConnectionResult.SUCCESS){
			arclient = new ActivityRecognitionClient(this, this, this);
			arclient.connect();
		}
		else{
			Log.d("myDebug", "NO__GooglePlayServicesAvailable");
			Toast.makeText(this, "Please install Google Play Service.", Toast.LENGTH_SHORT).show();
		}
		
		receiver = new BroadcastReceiver() {
		    @Override
		    public void onReceive(Context context, Intent intent) {
		    	String v =  "Activity : " + intent.getStringExtra("Activity") + " " + "Confidence : " + intent.getExtras().getInt("Confidence") + "\n";
		    	v += tvActivity.getText();
		    	tvActivity.setText(v);
		    	if(intent.getBooleanExtra("onBike", false)){
		    		backColor.setBackgroundColor(Color.YELLOW);
		    		Log.d("myDebug", "onBike");
		    	}else{
		    		backColor.setBackgroundColor(Color.WHITE);
		    		Log.d("myDebug", "Not_Bike");
		    	}
		    }
		  };
		  
		 IntentFilter filter = new IntentFilter();
		 filter.addAction("com.kpbird.myactivityrecognition.ACTIVITY_RECOGNITION_DATA");
		 registerReceiver(receiver, filter);
		
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(arclient!=null){
			arclient.removeActivityUpdates(pIntent);
			arclient.disconnect();
		}
		unregisterReceiver(receiver);
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();
	}
	@Override
	public void onConnected(Bundle arg0) {
		Log.d("myDebug", "onConnected");
		Intent intent = new Intent(this, ActivityRecognitionService.class);
		pIntent = PendingIntent.getService(this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);
		arclient.requestActivityUpdates(0, pIntent);   
	}
	@Override
	public void onDisconnected() {
	}

	public void cancelUpdates(View view) {
		arclient.removeActivityUpdates(pIntent);
		cancelButton.setVisibility(View.GONE);
		startButton.setVisibility(View.VISIBLE);
	}
	public void requestUpdates(View view) {
		arclient.requestActivityUpdates(0, pIntent);
		cancelButton.setVisibility(View.VISIBLE);
		startButton.setVisibility(View.GONE);
	}
}
