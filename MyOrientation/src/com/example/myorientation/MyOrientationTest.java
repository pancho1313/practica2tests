package com.example.myorientation;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class MyOrientationTest extends Activity implements SensorEventListener {
	private String TAG;
	private SensorManager mSensorManager;
    private Sensor mGravity;
    private float[] m_vals;
    private float[] m_rotationMatrix;
    private boolean newData;
    
    private int sensor = Sensor.TYPE_GRAVITY;
    
    //displays
    private TextView display;
    private Button actionButton;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_orientation_test);
		
		TAG = "MyOrientationTest";
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mGravity = mSensorManager.getDefaultSensor(sensor);
        
		//displays
		display = (TextView)findViewById(R.id.vals);
		actionButton = (Button)findViewById(R.id.actionButton);
		newData = false;
		
		//evitar que se apague la pantalla
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		m_vals = new float[3];
		m_rotationMatrix = new float[9];
		
		//test
		float[] m = new float[9];
		float[] gValues = new float[3];
		gValues[0] = 0;
		gValues[1] = 9;
		gValues[2] = 0;
		
		float[] northDir = new float[3];
		northDir[0] = 0;
		northDir[1] = 0;
		northDir[2] = -25;
		
		float[] gValuesClone = gValues.clone();
		float[] northDirClone = northDir.clone();
		
		
		boolean bien = SensorManager.getRotationMatrix(m, null,
				gValuesClone, northDirClone);
		
		/*
		float[] resp = new float[3];
		float[] t = {0,15,0};
		resp[0] = 
				 m[0]*t[0]
				+m[1]*t[0]
				+m[2]*t[0];
		resp[1] = 
				 m[3]*t[1]
				+m[4]*t[1]
				+m[5]*t[1];
		resp[2] = 
				 m[6]*t[2]
				+m[7]*t[2]
				+m[8]*t[2];
		*/
		float[] resp = new float[4];
		float[] t = {0,15,0,0};
		float[] m16 = new float[16];
		m16[0]=m[0];
		m16[1]=m[1];
		m16[2]=m[2];
		m16[4]=m[3];
		m16[5]=m[4];
		m16[6]=m[5];
		m16[8]=m[6];
		m16[9]=m[7];
		m16[10]=m[8];
		
		float[] m16t = new float[16];
		Matrix.transposeM(m16t, 0, m16, 0);
		
		Matrix.multiplyMV(resp , 0, m16t, 0, t , 0);
		
		Log.d(TAG,">> m: ["+m[0]+"|"+m[1]+"|"+m[2]+"]");
		Log.d(TAG,">> m: ["+m[3]+"|"+m[4]+"|"+m[5]+"]");
		Log.d(TAG,">> m: ["+m[6]+"|"+m[7]+"|"+m[8]+"]");
		
		Log.d(TAG,bien+ ">> resp: ["+resp[0]+"|"+resp[1]+"|"+resp[2]+"]");
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.my_orientation_test, menu);
		return true;
	}
	
	protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mGravity, SensorManager.SENSOR_DELAY_GAME);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
    	//Log.d(TAG, "onSensorChanged");
    	if (event.sensor.getType() != sensor)
            return;
    	
    	float[] gValues = event.values.clone();
    	float [] northDir = {0,0,-25};//como sacando una foto al norte
    	
    	
    	if (SensorManager.getRotationMatrix(m_rotationMatrix, null,
    			gValues, northDir)) {
			SensorManager.getOrientation(m_rotationMatrix, m_vals);
			
			m_vals[0] = (float) Math.toDegrees(m_vals[0]);
			m_vals[1] = (float) Math.toDegrees(m_vals[1]);
			m_vals[2] = (float) Math.toDegrees(m_vals[2]);
			newDataAlarm();
		}
    }

    public void action(View v){
    	Log.d(TAG, "action");
    	refreshDisplay();
    }
    
    private void refreshDisplay(){
    	String v =  m_vals[0]+"    "+m_vals[1]+"    "+m_vals[2]+"\n";
    	v += display.getText();
    	display.setText(v);
    	newDataCkecked();
    }
    
    private void newDataAlarm(){
    	newData = true;
    	actionButton.setBackgroundColor(Color.GREEN);
    }
    private void newDataCkecked(){
    	newData = false;
    	actionButton.setBackgroundColor(Color.LTGRAY);
    }
}