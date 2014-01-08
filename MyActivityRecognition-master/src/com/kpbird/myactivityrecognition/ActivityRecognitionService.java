package com.kpbird.myactivityrecognition;

import java.io.IOException;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class ActivityRecognitionService extends IntentService	 {

	private MediaPlayer myAudio;
	private Notification noti;
	
	private String TAG = this.getClass().getSimpleName();
	public ActivityRecognitionService() {
		super("My Activity Recognition Service");
	}

	/*
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    //handleCommand(intent);
	    // We want this service to continue running until it is explicitly
	    // stopped, so return sticky.
		Log.d("myDebug", "onStartCommand");
	    return START_NOT_STICKY;
	}
	*/
	
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d("myDebug", "onHandle");
		//handleCommand(intent);
		
		if(ActivityRecognitionResult.hasResult(intent)){
			ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
			
			boolean onBike = onBike(result.getMostProbableActivity().getType());
			
			Log.i(TAG, getType(result.getMostProbableActivity().getType()) +"\t" + result.getMostProbableActivity().getConfidence());
			Intent i = new Intent("com.kpbird.myactivityrecognition.ACTIVITY_RECOGNITION_DATA");
			i.putExtra("Activity", getType(result.getMostProbableActivity().getType()) );
			i.putExtra("Confidence", result.getMostProbableActivity().getConfidence());
			i.putExtra("onBike", onBike);
			sendBroadcast(i);
			
			if(onBike){
				playAudio(result.getMostProbableActivity().getType());
			}else{
				playAudio(result.getMostProbableActivity().getType());
			}
		}
		
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
	
	private void playAudio(int type){
		String audioPath = "";
		if(type == DetectedActivity.UNKNOWN)
			audioPath = "beep_low.mp3";
		else if(type == DetectedActivity.IN_VEHICLE)
			audioPath = "beep_low.mp3";
		else if(type == DetectedActivity.ON_BICYCLE)
			audioPath = "beep_high.mp3";
		else if(type == DetectedActivity.ON_FOOT)
			audioPath = "beep_low.mp3";
		else if(type == DetectedActivity.STILL)
			audioPath = "beep_low.mp3";
		else if(type == DetectedActivity.TILTING)
			audioPath = "beep_low.mp3";
		else
			audioPath = "beep_low.mp3";
		
		myAudio = new MediaPlayer();
		AssetFileDescriptor afd;
		try {
			afd = getAssets().openFd(audioPath);
			try {
				myAudio.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
			} catch (IllegalArgumentException e) {
				Log.d("myDebug", "ERROR: myAudio.setDataSource()");
			} catch (IllegalStateException e) {
				Log.d("myDebug", "ERROR: myAudio.setDataSource()");
			} catch (IOException e) {
				Log.d("myDebug", "ERROR: myAudio.setDataSource()");
			}
		} catch (IOException e) {
			Log.d("myDebug", "ERROR: afd = getAssets().openFd(audioPath)");
		}
		
		
		try {
			myAudio.prepare();
		} catch (IllegalStateException e) {
			Log.d("myDebug", "ERROR: myAudio.prepare()");
		} catch (IOException e) {
			Log.d("myDebug", "ERROR: myAudio.prepare()");
		}
		myAudio.setLooping(false);
		myAudio.start();
	}

	private void handleCommand(Intent i){
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle("My notification")
		        .setContentText("Hello World!");
		
		PendingIntent pIntent = PendingIntent.getService(this.getApplicationContext(), 0, i,PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(pIntent);
		
		noti = mBuilder.build();
		
		this.startForeground(333, noti);
	}
}
