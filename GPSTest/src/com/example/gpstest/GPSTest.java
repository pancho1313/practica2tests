package com.example.gpstest;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

public class GPSTest extends Activity implements LocationListener  {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gpstest);
		
		LocationManager mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

		mlocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.gpstest, menu);
		return true;
	}
	
	@Override
	public void onLocationChanged(Location loc){

		loc.getLatitude();
		loc.getLongitude();
		String Text = "My current location is: " +
		"Latitud = " + loc.getLatitude() +
		"Longitud = " + loc.getLongitude();
		
		Toast.makeText( getApplicationContext(),
		Text,
		Toast.LENGTH_SHORT).show();

		TextView text = (TextView)findViewById(R.id.text);
		text.setText(text.getText()+"\n"+Text);
	}

	@Override
	public void onProviderDisabled(String provider){

		Toast.makeText( getApplicationContext(),
		"Gps Disabled",
		Toast.LENGTH_SHORT ).show();

	}

	@Override
	public void onProviderEnabled(String provider){

		Toast.makeText( getApplicationContext(),
		"Gps Enabled",
		Toast.LENGTH_SHORT).show();

	}

	@Override

	public void onStatusChanged(String provider, int status, Bundle extras){}

}
