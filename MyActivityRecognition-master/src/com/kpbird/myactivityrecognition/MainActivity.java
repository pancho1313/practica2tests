package com.kpbird.myactivityrecognition;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
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
import com.google.android.gms.location.DetectedActivity;

/**
 * Esta actividad prueba la api de GooglePlayServices para el reconocimiento de actividad.
 * Da la opcion de guardar estadisticas con el porcentaje de cada actividad reconocida.
 * Requiere la libreria google-play-services_lib
 * @author job
 *
 */
public class MainActivity extends Activity implements GooglePlayServicesClient.ConnectionCallbacks,GooglePlayServicesClient.OnConnectionFailedListener {

	private ActivityRecognitionClient arclient;
	private PendingIntent pIntent;
	private BroadcastReceiver receiver;
	private TextView tvActivity;
	private LinearLayout backColor;
	private Button startButton;
	private Button cancelButton;
	
	//para guardar estadisticas
	private int lastActivityType = -1;
	private long[] timeInTask;
	private double[] timeInTaskPercent;
	private long lastUpdateTime;
	private long startMillisec, endMillisec;
	private TextView starDateTextView, endDateTextView;
	private Button saveButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tvActivity = (TextView) findViewById(R.id.tvActivity);
		backColor = (LinearLayout) findViewById(R.id.backColor);
		startButton = (Button) findViewById(R.id.requestUpdates);
		cancelButton = (Button) findViewById(R.id.cancelUpdates);
		//estadisticas
		starDateTextView = (TextView) findViewById(R.id.startDate);
		endDateTextView = (TextView) findViewById(R.id.endDate);
		timeInTask = new long[6];
		timeInTaskPercent = new double[6];
		saveButton = (Button)findViewById(R.id.saveButton);
		
		
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
		    	
		    	
		    	int type = intent.getIntExtra("activityType", DetectedActivity.UNKNOWN);
		    	
		    	if(type == DetectedActivity.ON_BICYCLE){
		    		backColor.setBackgroundColor(Color.YELLOW);
		    	}else{
		    		backColor.setBackgroundColor(Color.WHITE);
		    	}
		    	
		    	updateTaskStatistics(type);
		    	
		    	String v =  "Activity : " + intent.getStringExtra("Activity") + " " + "Confidence : " + intent.getExtras().getInt("Confidence") +" ["+type+"]"+timeInTask[type]+ "\n";
		    	v += tvActivity.getText();
		    	tvActivity.setText(v);
		    	
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
		//arclient.requestActivityUpdates(0, pIntent);
		requestUpdates(null);
	}
	@Override
	public void onDisconnected() {
	}

	public void cancelUpdates(View view) {
		arclient.removeActivityUpdates(pIntent);
		cancelButton.setVisibility(View.GONE);
		startButton.setVisibility(View.VISIBLE);
		saveButton.setVisibility(View.VISIBLE);
		updateEndDate();
	}
	public void requestUpdates(View view) {
		arclient.requestActivityUpdates(0, pIntent);
		cancelButton.setVisibility(View.VISIBLE);
		startButton.setVisibility(View.GONE);
		saveButton.setVisibility(View.GONE);
		updateStartDate();
		resetStatistics();
	}
	private void resetStatistics(){
		timeInTask = new long[6];
		lastActivityType = -1;
	}
	public void saveStatistics(View view) {
		Log.d("myDebug", "saveEstatistics");
		/** Method to check whether external media available and writable. This is adapted from
		   http://developer.android.com/guide/topics/data/data-storage.html#filesExternal */
		
		String[] textToFile = new String[6];
		String fileName = "test.txt";
		
		//calcular el porcentaje de tiempo en cada actividad
		long totalTime = endMillisec - startMillisec;
		for(int i = 0; i < timeInTaskPercent.length; i++){
			timeInTaskPercent[i] = ((double)timeInTask[i]/(double)totalTime)*100;
			textToFile[i] = ActivityRecognitionService.getType(i) +" : "+ timeInTaskPercent[i]+"%"+" ["+timeInTask[i]+"/"+totalTime+"]";
		}
		
		checkExternalMedia();
		writeToSDFile(textToFile, fileName);
		Toast.makeText(this, "File: "+fileName, Toast.LENGTH_SHORT).show();
	}
	private void checkExternalMedia(){
	      boolean mExternalStorageAvailable = false;
	    boolean mExternalStorageWriteable = false;
	    String state = Environment.getExternalStorageState();

	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        // Can read and write the media
	        mExternalStorageAvailable = mExternalStorageWriteable = true;
	    } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        // Can only read the media
	        mExternalStorageAvailable = true;
	        mExternalStorageWriteable = false;
	    } else {
	        // Can't read or write
	        mExternalStorageAvailable = mExternalStorageWriteable = false;
	    }   
	    
	}

	/** Method to write ascii text characters to file on SD card. Note that you must add a 
	   WRITE_EXTERNAL_STORAGE permission to the manifest file or this method will throw
	   a FileNotFound Exception because you won't have write permission. */

	private void writeToSDFile(String[] textToFile, String fileName){

	    // Find the root of the external storage.
	    // See http://developer.android.com/guide/topics/data/data-  storage.html#filesExternal

	    File root = android.os.Environment.getExternalStorageDirectory(); 
	    

	    // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder

	    File dir = new File (root.getAbsolutePath() + "/activityRecog");
	    dir.mkdirs();
	    File file = new File(dir, fileName);

	    try {
	        FileOutputStream f = new FileOutputStream(file);
	        PrintWriter pw = new PrintWriter(f);
	        for(int i = 0; i < textToFile.length; i++){
	        	pw.println(textToFile[i]);
	        }
	        pw.flush();
	        pw.close();
	        f.close();
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	        Log.d("myDebug", "******* File not found. Did you" +
	                " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
	        Log.i("WriteTextFile", "******* File not found. Did you" +
	                " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	
	private void updateStartDate(){
		starDateTextView.setText("a) "+new Date().toString());
		endDateTextView.setText(R.string.emptyDate);
	}
	private void updateEndDate(){
		//ojo hay que contabilizar la ultima actividad tambien
		updateTaskStatistics(-1);
		endMillisec = System.currentTimeMillis();
		endDateTextView.setText("b) "+new Date().toString());
	}
	
	/*
	 * actualiza el registro de las estadisticas de actividades realizadas
	 */
	private void updateTaskStatistics(int type){
		
		if(lastActivityType < 0){
			lastActivityType = type;
			lastUpdateTime = startMillisec = System.currentTimeMillis();//ojo el recorrido comienza la primera vez que detecta actividad
			
		}else if(lastActivityType != type){
			//calcular la diferencia de tiempo
			long now = System.currentTimeMillis();
			long dif = now - lastUpdateTime;
			
			//agregar la diferencia al contador de tiempo respectivo
			timeInTask[lastActivityType] += dif;
			
			//actualizar
			lastActivityType = type;
			lastUpdateTime = now;
		}
	}
	
}
