package com.kpbird.myactivityrecognition;

import java.io.IOException;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class CopyOfActivityRecognitionService extends IntentService	implements GooglePlayServicesClient.ConnectionCallbacks,GooglePlayServicesClient.OnConnectionFailedListener {

	private MediaPlayer myAudio;
	private Notification noti;
	private ActivityRecognitionClient arclient;
	
	private String TAG = this.getClass().getSimpleName();
	public CopyOfActivityRecognitionService() {
		super("My Copy Of Activity Recognition Service");
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
	}
	@Override
	public void onConnected(Bundle arg0) {
		Log.d("myDebug", "onConnected COPY");
		Intent intent = new Intent(this, ActivityRecognitionService.class);
		PendingIntent pi = PendingIntent.getService(this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);
		arclient.requestActivityUpdates(0, pi);   
	}
	@Override
	public void onDisconnected() {
	}
	
/*
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    handleCommand(intent);
	    // We want this service to continue running until it is explicitly
	    // stopped, so return sticky.
		Log.d("myDebug", "onStartCommand");
	    return START_NOT_STICKY;
	}
*/
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d("myDebug", "onHandle COPY");
		handleCommand(intent);
		int count = 20;
		while(testGS() && count > 0){
			
			SystemClock.sleep(2000);
			Log.d("myDebug", "testGS "+count--);
			Intent i = new Intent(this, ActivityRecognitionService.class);
			startService(i);
			PendingIntent pi = PendingIntent.getService(this, 0, i,PendingIntent.FLAG_CANCEL_CURRENT);
			try {
				pi.send();
			} catch (CanceledException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/*
		if(ActivityRecognitionResult.hasResult(intent)){
			ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
			Log.i(TAG, getType(result.getMostProbableActivity().getType()) +"\t" + result.getMostProbableActivity().getConfidence());
			Intent i = new Intent("com.kpbird.myactivityrecognition.ACTIVITY_RECOGNITION_DATA");
			i.putExtra("Activity", getType(result.getMostProbableActivity().getType()) );
			i.putExtra("Confidence", result.getMostProbableActivity().getConfidence());
			i.putExtra("onBike", onBike(result.getMostProbableActivity().getType()));
			sendBroadcast(i);
			Log.d("myDebug", "preplay");
			playAudio();
			Log.d("myDebug", "postplay");
		}
		*/
	}
	
	private String getType(int type){
		if(type == DetectedActivity.UNKNOWN)
			return "Unknown";
		else if(type == DetectedActivity.IN_VEHICLE)
			return "In Vehicle";
		else if(type == DetectedActivity.ON_BICYCLE)
			return "On Bicycle";
		else if(type == DetectedActivity.ON_FOOT)
			return "On Foot";
		else if(type == DetectedActivity.STILL)
			return "Still";
		else if(type == DetectedActivity.TILTING)
			return "Tilting";
		else
			return "";
	}
	
	private boolean onBike(int type){
		if(type == DetectedActivity.ON_BICYCLE){
			return true;
		}
		return false;
	}
	
	private void playAudio(){

		myAudio = new MediaPlayer();
		AssetFileDescriptor afd;
		try {
			afd = getAssets().openFd("beep_low.mp3");
			try {
				myAudio.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		try {
			myAudio.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		myAudio.setLooping(false);
		myAudio.start();
	}

	private void handleCommand(Intent i){
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle("My notification")
		        .setContentText("Hello Copy Of World!");
		
		PendingIntent pIntent = PendingIntent.getService(this.getApplicationContext(), 0, i,PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(pIntent);
		
		noti = mBuilder.build();
		
		this.startForeground(3333, noti);
	}
	
	private boolean testGS(){
		int resp =GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if(resp == ConnectionResult.SUCCESS){
			return true;
		}
		else{
			return false;
		}
	}
}
