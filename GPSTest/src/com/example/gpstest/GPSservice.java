package com.example.gpstest;


import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class GPSservice extends Service implements LocationListener {

	private BroadcastReceiver reciever;
	private Notification notification;
	private int updates;
	
	@Override
    public void onCreate() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	initStopReciever();
    	
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        handleCommand(intent);
        
        LocationManager mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

		mlocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, this);
        
        //boolean statusOfGPS = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        updates = 0;
        
        updateLog("GPSservice started");
        
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
		Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
		unregisterReceiver(reciever);
		
		updateLog("GPSservice dead");
    }
    
    private void initStopReciever(){
    	reciever = new BroadcastReceiver() {
		    @Override
		    public void onReceive(Context context, Intent intent) {
				stopMe();
		    }
		  };
		  
		 IntentFilter filter = new IntentFilter();
		 filter.addAction("com.example.gpstest.END_GPS_SERVICE");
		 registerReceiver(reciever, filter);
    }
    
    private void stopMe(){
    	this.stopSelf();
    }
    
    private void handleCommand(Intent i){
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle("GPSservice");
		
		PendingIntent pIntent = PendingIntent.getService(this.getApplicationContext(), 0, i,PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(pIntent);
		
		notification = mBuilder.build();
		
		this.startForeground(333, notification);
	}
    
    private void updateLog(String s){
    	Intent i = new Intent("com.example.gpstest.LOG");
    	i.putExtra("log", s);
		sendBroadcast(i);
    }
    ///////////////
    
    @Override
	public void onLocationChanged(Location loc){

		loc.getLatitude();
		loc.getLongitude();
		String text = "Latitud = " + loc.getLatitude() +
		"Longitud = " + loc.getLongitude();
		
		updateLog((updates++)+": "+text);
		//long time = loc.getElapsedRealtimeNanos();
		saveData(text);
	}

	@Override
	public void onProviderDisabled(String provider){

		Toast.makeText( getApplicationContext(),
		"Gps Disabled",
		Toast.LENGTH_SHORT ).show();

		updateLog("GPS off!");
	}

	@Override
	public void onProviderEnabled(String provider){

		Toast.makeText( getApplicationContext(),
		"Gps Enabled",
		Toast.LENGTH_SHORT).show();

		updateLog("GPS on!");
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras){}
    
	
	private void saveData(String data){
    	Intent intent = new Intent(this, SaveFileIntentService.class);
    	intent.putExtra("data", data);
    	startService(intent);
    }
	
}
