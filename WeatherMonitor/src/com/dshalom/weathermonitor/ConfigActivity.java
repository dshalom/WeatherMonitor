package com.dshalom.weathermonitor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

public class ConfigActivity extends Activity {

	RadioButton rbCentigrade,rbFahrenheit, rbCity,rbPostCode;
	final String centigrade = "CENTIGRADE";
	final String postcode = "POSTCODE";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.config);

		
		rbCentigrade = (RadioButton) findViewById(R.id.radioCentigrade);
		rbFahrenheit = (RadioButton) findViewById(R.id.radioFahrenheit);
		rbPostCode = (RadioButton) findViewById(R.id.radioPostcode);
		rbCity = (RadioButton) findViewById(R.id.radioCity);


		Intent intent = this.getIntent();
		boolean bCentigrade = intent.getBooleanExtra(centigrade, false);
		boolean bPostCode = intent.getBooleanExtra(postcode, false);
		
		rbCentigrade.setChecked(bCentigrade);
		rbFahrenheit.setChecked(!bCentigrade);
		rbPostCode.setChecked(bPostCode);
		rbCity.setChecked(!bPostCode);

	}


	public void doneClicked(View view) {

		Intent intent = this.getIntent();

		intent.putExtra(centigrade, rbCentigrade.isChecked());
		intent.putExtra(postcode, rbPostCode.isChecked());

		this.setResult(RESULT_OK, intent);
		finish();
	}
}
