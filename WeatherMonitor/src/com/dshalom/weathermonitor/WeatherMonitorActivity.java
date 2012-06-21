package com.dshalom.weathermonitor;

import java.util.ArrayList;
import java.util.List;
import com.dshalom.weathermonitor.DataDownloader;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class WeatherMonitorActivity extends ListActivity {

    DataDownloader dataDownloader;
	EditText editTextLocation;
	TextView textViewLocation, textViewLocationType;
	final String centigrade = "CENTIGRADE";
	final String postcode = "POSTCODE";
	boolean bCentigrade, bPostCode;
	ArrayList<WeatherData>weatherDataList;
	WeatherAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		weatherDataList = new ArrayList<WeatherData>();

		adapter = new WeatherAdapter(this, R.layout.row,weatherDataList);


		View header = (View) getLayoutInflater().inflate(
				R.layout.listview_header_row, null);

		getListView().addHeaderView(header);
		getListView().setBackgroundResource(R.drawable.background1);
		
		setListAdapter(adapter);

//		editTextLocation = (EditText) findViewById(R.id.editTextLocation);
//		textViewLocation = (TextView) findViewById(R.id.textViewLocation);
//
//		textViewLocationType = (TextView) findViewById(R.id.textViewLocationType);
//
//		// ////get the location type and set
//		SharedPreferences preferences = PreferenceManager
//				.getDefaultSharedPreferences(getBaseContext());
//		bCentigrade = preferences.getBoolean(centigrade, false);
//		bPostCode = preferences.getBoolean(postcode, false);
//
//		if (bPostCode) {
//			textViewLocationType.setText("Enter Postcode");
//			editTextLocation.setText(preferences.getString("postcode", "SW11"));
//
//		} else {
//			textViewLocationType.setText("Enter City");
//			editTextLocation.setText(preferences.getString("city", "London"));
//		}

		// make the request
		dataDownloader = new DataDownloader(this);
		if (bCentigrade) {
			dataDownloader.execute(new String[] {
					"London", "cent" });
		} else {
			dataDownloader.execute(new String[] {
				//	editTextLocation.getText().toString(), "fari" });
					"London", "cent" });
		}

	}

	public void onGoClick(View v) {

		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("city", editTextLocation.getText().toString());
		editor.commit();

		dataDownloader = new DataDownloader(this);
		if (bCentigrade) {
			dataDownloader.execute(new String[] {
					editTextLocation.getText().toString(), "cent" });
		} else {
			dataDownloader.execute(new String[] {
					editTextLocation.getText().toString(), "fari" });
		}

	}

	public void showError(ErrorCodes result) {

		Toast toast = null;
		if (result == ErrorCodes.CONNECTIONERROR
				|| result == ErrorCodes.OTHERERROR) {
			toast = Toast.makeText(getBaseContext(),
					"Error, please check internet connection!",
					Toast.LENGTH_SHORT);
		} else if (result == ErrorCodes.POSTCODEERROR) {
			toast = Toast.makeText(getBaseContext(),
					"Error, please check location!", Toast.LENGTH_SHORT);
		}
		toast.show();
	}

	public void configureClicked(View view) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		// if any field is blank retrieve data from preferences

		Intent intent = new Intent();
		intent.putExtra(centigrade, preferences.getBoolean(centigrade, false));
		intent.putExtra(postcode, preferences.getBoolean(postcode, false));
		intent.setClass(this, ConfigActivity.class);
		startActivityForResult(intent, 0);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		boolean bPC = data.getBooleanExtra(postcode, false);
		boolean bCent = data.getBooleanExtra(centigrade, false);
		// if any field is blank retrieve data from preferences
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(centigrade, bCent);
		editor.putBoolean(postcode, bPC);
		editor.commit();
		// update the textview
		if (bPC != bPostCode) {
			bPostCode = bPC;
			if (bPostCode) {
				textViewLocationType.setText("Enter Postcode");
				editTextLocation.setText("");

			} else {
				textViewLocationType.setText("Enter City");
				editTextLocation.setText("");
			}
		}
		// update unit
		if (bCent != bCentigrade) {
			bCentigrade = bCent;
			if (bCent) {
				dataDownloader.updateTemps("cent");
			} else {
				dataDownloader.updateTemps("fari");
			}
		}

	}

	
}