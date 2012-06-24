package com.dshalom.weathermonitor;

import java.util.ArrayList;
import com.dshalom.weathermonitor.DataDownloader;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class WeatherMonitorActivity extends ListActivity implements OnSharedPreferenceChangeListener {

	private static final String TAG = "WeatherMonitorActivity";
	TextView textViewLocation,textViewLastRefresh;
	ArrayList<WeatherData> weatherDataList;
	long lastUpdate;
	SharedPreferences prefs;
	WeatherAdapter adapter;
	Button buttonRefresh;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		weatherDataList = new ArrayList<WeatherData>();

		adapter = new WeatherAdapter(this, R.layout.row, weatherDataList);

		View header = (View) getLayoutInflater().inflate(
				R.layout.headerrow, null);

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

		// make the request
		doWeatherUpdate();
	}

	public void onGoClick(View v) {
		Log.d(TAG,"onGoClick");
		doWeatherUpdate();
	}
	
	private void doWeatherUpdate()
	{
		DataDownloader dataDownloader = new DataDownloader(this);
		Log.d(TAG,"doWeatherUpdate");
		
		String location = prefs.getString("prefLocation", "London");
		Log.d(TAG, String.format("%s", location));	
		dataDownloader.execute(new String[] { location });
		
	}

	public void showError(ErrorCode result) {
		Log.d(TAG,"showError " + result);
		Toast toast = null;
		if (result == ErrorCode.CONNECTIONERROR
				|| result == ErrorCode.OTHERERROR) {
			toast = Toast.makeText(getBaseContext(),
					"Error, please check internet connection!",
					Toast.LENGTH_SHORT);
		} else if (result == ErrorCode.POSTCODEERROR) {
			toast = Toast.makeText(getBaseContext(),
					"Error, please check location!", Toast.LENGTH_SHORT);
		}
		toast.show();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG,"onCreateOptionsMenu");
		getMenuInflater().inflate(R.menu.menu, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG,"showError");
		startActivity(new Intent(this, PrefsActivity.class));
		return true;
	}

	public void onSharedPreferenceChanged(SharedPreferences arg0, String key) {
		Log.d(TAG,"onSharedPreferenceChanged " + key);
		doWeatherUpdate();
		
	}

}