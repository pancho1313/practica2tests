package com.example.activityregistrator;

import trainergenerator.SVMTraining;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class TrainingFileGenerator extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_training_file_generator);
		
		// keep screen on
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.training_file_generator, menu);
		return true;
	}
	
	public void generateTD(View v){
		((Button)v).setEnabled(false);
		TextView progress = (TextView)findViewById(R.id.progress);
		if(SVMTraining.generateTrainingFile(
				"ActivityRegistrator/record",
				((EditText)findViewById(R.id.editText1)).getText()+".txt",
				"ActivityRegistrator/mark",
				((EditText)findViewById(R.id.editText2)).getText()+".txt",
				"ActivityRegistrator/train",
				((EditText)findViewById(R.id.editText3)).getText()+".txt",
				progress))
			Toast.makeText(this, "sucksess", Toast.LENGTH_LONG).show();
		else
			Toast.makeText(this, "FAIL", Toast.LENGTH_LONG).show();
	}

}
