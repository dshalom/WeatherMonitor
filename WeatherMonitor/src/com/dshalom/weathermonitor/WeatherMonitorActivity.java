package com.dshalom.weathermonitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.dshalom.weathermonitor3.R;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibrary;

import com.crashlytics.android.Crashlytics;

public class WeatherMonitorActivity extends ListActivity implements
		OnSharedPreferenceChangeListener {

	TextView textViewLocation, textViewLastRefresh;
	ArrayList<WeatherData> weatherDataList;
	long lastUpdate;
	SharedPreferences prefs;
	WeatherAdapter adapter;
	Button buttonRefresh;
	



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Crashlytics.start(this);
	
		weatherDataList = new ArrayList<WeatherData>();

		adapter = new WeatherAdapter(this, R.layout.row, weatherDataList);

		View header = (View) getLayoutInflater().inflate(R.layout.headerrow,
				null);

		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.BLUE));

		getListView().addHeaderView(header);
		getListView().setBackgroundResource(R.drawable.seagulls);

		View footerMsg = (View) getLayoutInflater().inflate(
				R.layout.footerlastrefresh, null);

		View footerButton = (View) getLayoutInflater().inflate(
				R.layout.footerrefreshbutton, null);

		getListView().addFooterView(footerMsg);
		getListView().addFooterView(footerButton);
		getListView().setClickable(false);

		setListAdapter(adapter);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);

		textViewLocation = (TextView) findViewById(R.id.txtHeaderLocation);
		textViewLastRefresh = (TextView) findViewById(R.id.textViewLastRefresh);

		buttonRefresh = (Button) findViewById(R.id.buttonRefresh);
		buttonRefresh.setText(R.string.refreshing);
		buttonRefresh.setClickable(false);

		LocationLibrary.initialiseLibrary(getBaseContext(), false,
				"com.dshalom.weathermonitor");

		doWeatherUpdate();

	}

	public void onGoClick(View v) {
		doWeatherUpdate();
	}

	private void doWeatherUpdate() {

		
		if (prefs.getBoolean("prefCurrentLocation", false)) {
			LocationInfo latestInfo = new LocationInfo(getBaseContext());

			Geocoder geocoder;
			List<Address> addresses = null;
			geocoder = new Geocoder(this, Locale.getDefault());
			try {
				addresses = geocoder.getFromLocation(latestInfo.lastLat,
						latestInfo.lastLong, 1);
				String city = addresses.get(0).getAddressLine(1);
				textViewLocation.setText(city);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				textViewLocation.setText(latestInfo.lastLat + " "
						+ latestInfo.lastLong);
			}

			DataDownloader dataDownloader = new DataDownloader(this);
			String location = latestInfo.lastLat + " " + latestInfo.lastLong;
			dataDownloader.execute(new String[] { location });

		} else {
			DataDownloader dataDownloader = new DataDownloader(this);
			String location = prefs.getString("prefLocation", "London");
			textViewLocation.setText(location);
			dataDownloader.execute(new String[] { location });
		}

	}
	
	public void updateLocation(String location){
		
		if (prefs.getBoolean("prefCurrentLocation", false)){
			return;
			
		}
		else{
			textViewLocation.setText(location);
		}
	}

	public void showError(ErrorCodes result) {
		Toast toast = null;
		if (result == ErrorCodes.CONNECTIONERROR
				|| result == ErrorCodes.OTHERERROR) {
			toast = Toast.makeText(getBaseContext(),
					"Error, please check internet connection!",
					Toast.LENGTH_SHORT);
			// update widgets
			textViewLastRefresh.setText(R.string.connectionError);
			buttonRefresh.setText(R.string.refresh);
			buttonRefresh.setClickable(true);

		} else if (result == ErrorCodes.POSTCODEERROR) {
			toast = Toast.makeText(getBaseContext(),
					"Error, please check location!", Toast.LENGTH_SHORT);
			// update widgets
			textViewLastRefresh.setText(R.string.locationError);
			buttonRefresh.setText(R.string.refresh);
			buttonRefresh.setClickable(true);
		}
		toast.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.itemPrefs:
			Intent intent = new Intent(this, PrefsActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void onSharedPreferenceChanged(SharedPreferences arg0, String key) {
		doWeatherUpdate();

	}

}