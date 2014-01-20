package com.example.activityregistrator;

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
import android.widget.EditText;
import android.widget.TextView;

public class TrainingFileGenerator extends Activity {

	BroadcastReceiver progressReciever, hzReciever, userMessage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_training_file_generator);
		
		// keep screen on
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		initRecievers();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.training_file_generator, menu);
		return true;
	}
	
	@Override
    public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(progressReciever);
		unregisterReceiver(hzReciever);
		unregisterReceiver(userMessage);
    }
	
	public void generateTD(View v){
		((Button)v).setEnabled(false);
		
		Intent intent = new Intent(this, TrainingGeneratorIntentService.class);
    	intent.putExtra("sensorDataFolder", "ActivityRegistrator/record");
    	intent.putExtra("sensorDataFile", ((EditText)findViewById(R.id.editText1)).getText()+".txt");
    	intent.putExtra("sensorMarksFolder", "ActivityRegistrator/mark");
    	intent.putExtra("sensorMarksFile", ((EditText)findViewById(R.id.editText2)).getText()+".txt");
    	intent.putExtra("outFolder", "ActivityRegistrator/train");
    	intent.putExtra("outFile", ((EditText)findViewById(R.id.editText3)).getText()+".txt");
    	startService(intent);
	}

	private void initRecievers(){
    	progressReciever = new BroadcastReceiver() {
		    @Override
		    public void onReceive(Context context, Intent intent) {
		    	((TextView)findViewById(R.id.progress)).setText(intent.getFloatExtra("progress", -2)+"%");
		    }
		  };
		  
		 IntentFilter filter = new IntentFilter();
		 filter.addAction("com.example.activityregistrator.UPDATE_PROGRESS");
		 registerReceiver(progressReciever, filter);
		 
		 
		 hzReciever = new BroadcastReceiver() {
		    @Override
		    public void onReceive(Context context, Intent intent) {
		    	((TextView)findViewById(R.id.hz)).setText(intent.getFloatExtra("hz", -3)+"hz");
		    }
		  };
		  
		 IntentFilter filter2 = new IntentFilter();
		 filter2.addAction("com.example.activityregistrator.UPDATE_HZ");
		 registerReceiver(hzReciever, filter2);
		 
		 
		 userMessage = new BroadcastReceiver() {
		    @Override
		    public void onReceive(Context context, Intent intent) {
		    	((TextView)findViewById(R.id.userMessage)).setText(intent.getStringExtra("userMessage"));
		    }
		  };
		  
		 IntentFilter filter3 = new IntentFilter();
		 filter3.addAction("com.example.activityregistrator.USER_MESSAGE");
		 registerReceiver(userMessage, filter3);
    }
}
