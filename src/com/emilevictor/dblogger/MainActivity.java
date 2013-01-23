package com.emilevictor.dblogger;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlSerializer;


import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private List<Double> latitudes;
	private List<Double> longitudes;
	private List<Integer> signalStrengths;
	private LocationManager locationManager;
	private String provider;
	private int lastKnownSignalStrength;
	private long numberOfLocationsRecorded;
	private TextView lat;
	private TextView longi;
	private TextView numberRecorded;
	private LocationListener listener;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		lastKnownSignalStrength = 0;
		lat = (TextView) findViewById(R.id.lat);
		longi = (TextView) findViewById(R.id.longi);
		numberRecorded = (TextView) findViewById(R.id.numberRecorded);
		signalStrengths = new ArrayList<Integer>();
		longitudes = new ArrayList<Double>();
		latitudes = new ArrayList<Double>();

		// Get the location manager
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setCostAllowed(false);
		provider = locationManager.getBestProvider(criteria, false);

		
		listener = new LocationListener() {

			    @Override
			    public void onLocationChanged(Location location) {
			        // A new location update is received.  Do something useful with it.  In this case,
			        // we're sending the update to a handler which then updates the UI with the new
			        // location.
			    	Toast.makeText(getApplicationContext(), "Location found", Toast.LENGTH_LONG).show();
			        Log.w("Got location",String.valueOf(location.getLatitude()));
			        
			        lat.setText(String.valueOf(location.getLatitude()));
					longi.setText(String.valueOf(location.getLongitude()));
					


					
						signalStrengths.add(lastKnownSignalStrength);
						latitudes.add(location.getLatitude());
						longitudes.add(location.getLongitude());
						numberOfLocationsRecorded += 1;
						numberRecorded.setText(String.valueOf(numberOfLocationsRecorded));

						//Every tenth time
						if ((numberOfLocationsRecorded % 10) == 0) {
							try {
								writeKMLFile();
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
						}
			        }

				@Override
				public void onProviderDisabled(String provider) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onProviderEnabled(String provider) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onStatusChanged(String provider, int status,
						Bundle extras) {
					// TODO Auto-generated method stub
					
				}
			    
			};
			
			if (provider != null)
			{
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				        2000,          // 10-second interval.
				        5,             // 10 meters.
				        listener);
			}
		

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}




	private void writeKMLFile() throws IllegalArgumentException, IllegalStateException, IOException{
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();


		serializer.setOutput(writer);
		serializer.startDocument("UTF-8", true);
		serializer.startTag("", "kml");
		serializer.attribute("","xmlns", "http://www.opengis.net/kml/2.2");

		for (int i = 0; i < signalStrengths.size(); i++)
		{
			serializer.startTag("","Placemark");
			serializer.startTag("", "name");
			serializer.text(String.valueOf(signalStrengths.get(i)));
			serializer.endTag("","name");

			String coordinateString = latitudes.get(i).toString() + "," + longitudes.get(i).toString() + ",0";

			serializer.startTag("", "Point");
			serializer.startTag("", "coordinates");
			serializer.text(coordinateString);
			serializer.endTag("", "coordinates");
			serializer.endTag("", "Point");
		}



		serializer.endTag("", "kml");
		serializer.endDocument();

		// Write 20 Strings
		DataOutputStream out = 
				new DataOutputStream(openFileOutput("dbLoggerLocations.kml", Context.MODE_PRIVATE));
		out.writeUTF(writer.toString());
		out.close();


	}



}
