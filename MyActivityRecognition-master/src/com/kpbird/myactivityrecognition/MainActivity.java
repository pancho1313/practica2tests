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
import android.view.WindowManager;
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
	private RelativeLayout backColor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tvActivity = (TextView) findViewById(R.id.tvActivity);
		backColor = (RelativeLayout) findViewById(R.id.backColor);
		
		//evitar que se apague la pantalla
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		int resp =GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if(resp == ConnectionResult.SUCCESS){
			arclient = new ActivityRecognitionClient(this, this, this);
			arclient.connect();
		}
		else{
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

	private void sendToForeground(PendingIntent pi){
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle("My notification")
		        .setContentText("Hello World!");
/*
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, ResultActivity.class);

		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(ResultActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		
		*/
		mBuilder.setContentIntent(pi);
		NotificationManager mNotificationManager =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		//mNotificationManager.startForeground(3333, mBuilder.build());
		/*
		Notification note=new Notification(R.drawable.ic_launcher,
                "Can you hear the music?",
                System.currentTimeMillis());

note.setLatestEventInfo(this, "Fake Player",
    "Now Playing: \"Ummmm, Nothing\"",
    pi);
note.flags|=Notification.FLAG_NO_CLEAR;

pi.startForeground(1337, note);
*/
	}
}