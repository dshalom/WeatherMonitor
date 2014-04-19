package com.dshalom.weathermonitor;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

public class WeatherMonitorActivity extends ListActivity implements OnSharedPreferenceChangeListener {

	TextView textViewLocation,textViewLastRefresh;
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

		View header = (View) getLayoutInflater().inflate(
				R.layout.headerrow, null);
		
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.BLUE)); 
		//actionBar.setBackgroundDrawable(R.drawable.header_look);
		
		//actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); 
	//	actionBar.setDisplayShowCustomEnabled(true);
	//	actionBar.setDisplayShowTitleEnabled(false);
	//	actionBar.setCustomView(R.layout.ab);

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
		doWeatherUpdate();
	}
	
	private void doWeatherUpdate()
	{
		DataDownloader dataDownloader = new DataDownloader(this);		
		String location = prefs.getString("prefLocation", "London");
		dataDownloader.execute(new String[] { location });		
	}

	public void showError(ErrorCodes result) {
		Toast toast = null;
		if (result == ErrorCodes.CONNECTIONERROR
				|| result == ErrorCodes.OTHERERROR) {
			toast = Toast.makeText(getBaseContext(),
					"Error, please check internet connection!",
					Toast.LENGTH_SHORT);
			//update widgets
			textViewLastRefresh.setText(R.string.connectionError);
			buttonRefresh.setText(R.string.refresh);
			buttonRefresh.setClickable(true);
			
		} else if (result == ErrorCodes.POSTCODEERROR) {
			toast = Toast.makeText(getBaseContext(),
					"Error, please check location!", Toast.LENGTH_SHORT);
			//update widgets
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

		
		 switch (item.getItemId()) 
		   {
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