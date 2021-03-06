package com.example.myorientation;

import com.kircherelectronics.lowpasslinearacceleration.filter.LPFAndroidDeveloper;
import com.kircherelectronics.lowpasslinearacceleration.filter.LPFWikipedia;
import com.kircherelectronics.lowpasslinearacceleration.filter.LowPassFilter;

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

/**
 * This activity tests Low-pass filters to separate gravity from simple 3-axial accelerometer data.
 * Then it calculates vertical and horizontal linear acceleration.
 * Shows on screen the max vertical and horizontal linear acceleration (with a reset button).
 * @author job
 *
 */
public class MyOrientationTest extends Activity implements SensorEventListener {
	private String TAG;
	private SensorManager mSensorManager;
    private Sensor mSensor;
    private float[] m_vals;
    private float[] m_rotationMatrix;
    private boolean newData;
    
    /**
     * this application only uses the standard accelerometer sensor
     */
    private final int sensorType = Sensor.TYPE_ACCELEROMETER;
    
    //displays
    private TextView display;
    private Button actionButton;
    private TextView verticalGs;
    private TextView horizontalGs;
    
    
    // Low-Pass Filters ////////////////////////////////////////////////////////////////
    /*
     * uses:
     * 
     * 		package com.kircherelectronics.lowpasslinearacceleration.filter
     * 
     * implementation from:
     * 
     * 		https://github.com/BokiSoft/LowPassLinearAcceleration
     * 		http://www.kircherelectronics.com/blog/index.php/blog-articles/articles/85-blog/android-articles/android-sensor-articles/76-low-pass-filter-linear-acceleration
     */
  
    private int sensorDelay = SensorManager.SENSOR_DELAY_FASTEST;
    
 	private LowPassFilter lpfWiki;
 	private LowPassFilter lpfAndDev;
 	
 	// Indicate if a static alpha should be used for the LPF Wikipedia
 	private boolean staticWikiAlpha = false;
 	// Indicate if a static alpha should be used for the LPF Android Developer
 	private boolean staticAndDevAlpha = false;
 	// The static alpha for the LPF Wikipedia
 	private static float WIKI_STATIC_ALPHA = 0.1f;
 	// The static alpha for the LPF Android Developer
 	private static float AND_DEV_STATIC_ALPHA = 0.9f;
 	
 	// Filter to use
 	private final int USE_LPF_WIKI = 0;
 	private final int USE_LPF_ANDDEV = 0;
 	private int lpfUsed = USE_LPF_WIKI;
 	
 	//max min Gs
 	private float minHGs = 0;
 	private float minVGs = 0;
 	private float maxHGs = 0;
 	private float maxVGs = 0;

 	/**
	 * Initialize the filters.
	 */
	private void initFilters()
	{
		// Create the low-pass filters
		lpfWiki = new LPFWikipedia();
		lpfAndDev = new LPFAndroidDeveloper();

		// Initialize the low-pass filters with the saved prefs
		lpfWiki.setAlphaStatic(staticWikiAlpha);
		lpfWiki.setAlpha(WIKI_STATIC_ALPHA);

		lpfAndDev.setAlphaStatic(staticAndDevAlpha);
		lpfAndDev.setAlpha(AND_DEV_STATIC_ALPHA);
	}
	////////////////////////////////////////////////////////////////////////////////////
 	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_orientation_test);
		
		TAG = "MyOrientationTest";
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(sensorType);
        
		//displays
		display = (TextView)findViewById(R.id.vals);
		actionButton = (Button)findViewById(R.id.actionButton);
		verticalGs = (TextView)findViewById(R.id.verticalGs);
		horizontalGs = (TextView)findViewById(R.id.horizontalGs);
		newData = false;
		
		//evitar que se apague la pantalla
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		m_vals = new float[3];
		m_rotationMatrix = new float[9];
		
		initFilters();
		
		//testLocalToGlobal();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.my_orientation_test, menu);
		return true;
	}
	
	protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, sensorDelay);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
    	//Log.d(TAG, "onSensorChanged");
    	if (event.sensor.getType() != sensorType)
            return;
    	
    	// acceleration data from m/s2 to g's
    	float[] gAccel = event.values.clone();
    	gAccel[0] = gAccel[0] / SensorManager.GRAVITY_EARTH;
    	gAccel[1] = gAccel[1] / SensorManager.GRAVITY_EARTH;
    	gAccel[2] = gAccel[2] / SensorManager.GRAVITY_EARTH;

    	// get gravity and linear acceleration from selected filter
    	float[][] local_grav_linnearAcc;
    	if(lpfUsed == USE_LPF_WIKI)
    		local_grav_linnearAcc = lpfWiki.addSamples(gAccel);
    	else
    		local_grav_linnearAcc = lpfAndDev.addSamples(gAccel);
    	
    	// next: get global linear acceleration from local coordinates (android position)
    	float[] localGravity = local_grav_linnearAcc[0];
    	float[] localLinnearAcc = local_grav_linnearAcc[1];
    	float[] globalLinnearAcc = new float[3];
    	
    	/*
    	 *  calculate magDir (direction of magnetic north)
    	 *  in this case we just need an horizontal vector so
    	 *  the cross product of gravity and an local edge is enough
    	 */
    	float[] magDir;
    	float[] x = {1f,0f,0f};
    	float[] y = {0f,1f,0f};
    	float[] z = {0f,0f,1f};
    	float[] cx = cross(localGravity, x);
    	float[] cy = cross(localGravity, y);
    	float[] cz = cross(localGravity, z);
    	
    	double lencx = vecLength(cx);
    	double lency = vecLength(cy);
    	double lencz = vecLength(cz);
    	
    	// select the largest cross product for better precision
    	magDir = cx;
    	double lenmagDir = lencx;
    	if(lency > lenmagDir){
    		magDir = cy;
    		lenmagDir = lency;
    	}
    	if(lencz > lenmagDir){
    		magDir = cz;
    	}
    	
    	// get global coordinates using SensorManager.getRotationMatrix
    	if (SensorManager.getRotationMatrix(m_rotationMatrix, null,
    			localGravity, magDir)) {
			/*
    		SensorManager.getOrientation(m_rotationMatrix, m_vals);
			m_vals[0] = (float) Math.toDegrees(m_vals[0]);
			m_vals[1] = (float) Math.toDegrees(m_vals[1]);
			m_vals[2] = (float) Math.toDegrees(m_vals[2]);
			*/
    		newDataAlarm();
			
    		
    		// get global linear acceleration
    		float[] resp = new float[4];
    		float[] t = {localLinnearAcc[0],localLinnearAcc[1],localLinnearAcc[2],0};
    		float[] m16 = new float[16];
    		m16[0]=m_rotationMatrix[0];
    		m16[1]=m_rotationMatrix[1];
    		m16[2]=m_rotationMatrix[2];
    		m16[4]=m_rotationMatrix[3];
    		m16[5]=m_rotationMatrix[4];
    		m16[6]=m_rotationMatrix[5];
    		m16[8]=m_rotationMatrix[6];
    		m16[9]=m_rotationMatrix[7];
    		m16[10]=m_rotationMatrix[8];
    		float[] m16t = new float[16];
    		Matrix.transposeM(m16t, 0, m16, 0);
    		Matrix.multiplyMV(resp , 0, m16t, 0, t , 0);
    		
    		globalLinnearAcc[0] = resp[0];
    		globalLinnearAcc[1] = resp[1];
    		globalLinnearAcc[2] = resp[2];
    		
    		// set vertical (globalLinnearAcc[2]) and horizontal (hGs) Gs
    		float hGs = (float)Math.sqrt(Math.pow(globalLinnearAcc[0], 2) + Math.pow(globalLinnearAcc[1], 2));
    		
    		// update max vertical and horizontal global linear acceleration
    		if(globalLinnearAcc[2] < minVGs)
    			minVGs = globalLinnearAcc[2];
    		if(globalLinnearAcc[2] > maxVGs)
    			maxVGs = globalLinnearAcc[2];
    		if(hGs < minHGs)
    			minHGs = hGs;
    		if(hGs > maxHGs)
    			maxHGs = hGs;
    		
    		// refresh user's display with max vertical and horizontal global linear acceleration
    		verticalGs.setText("maxVGs: "+maxVGs +"  minVGs: "+minVGs);
    		horizontalGs.setText("maxHGs: "+maxHGs +"  minHGs: "+minHGs);
		}
    }

    // vector utils
    
    /**
     * cross product between vectors a b
     * @param a
     * @param b
     * @return
     */
    private float[] cross(float[] a, float[] b){
    	float[] c = new float[3];
    	
    	c[0] = a[1]*b[2]-a[2]*b[1];
    	c[1] = a[2]*b[0]-a[0]*b[2];
    	c[2] = a[0]*b[1]-a[1]*b[0];
    	
    	return c;
    }
    private double vecLength(float[] v){
    	return Math.sqrt(Math.pow(v[0], 2)+Math.pow(v[1], 2)+Math.pow(v[2], 2));
    }
    
    /**
     * reset max vertical and horizontal global linear acceleration to zero
     * @param v
     */
    public void action(View v){
    	Log.d(TAG, "action");
    	minHGs = 0;
     	minVGs = 0;
     	maxHGs = 0;
     	maxVGs = 0;
    	//refreshDisplay();
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
    
    private void testLocalToGlobal(){
    	float[] m = new float[9];
		float[] gValues = new float[3];
		gValues[0] = 0;
		gValues[1] = 4;
		gValues[2] = 4;
		
		float[] northDir = new float[3];
		northDir[0] = 0;
		northDir[1] = 10;
		northDir[2] = -10;
		
		float[] gValuesClone = gValues.clone();
		float[] northDirClone = northDir.clone();
		
		
		boolean bien = SensorManager.getRotationMatrix(m, null,
				gValuesClone, northDirClone);
		
		float[] resp = new float[4];
		float[] t = {0,0,15,0};
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
}
