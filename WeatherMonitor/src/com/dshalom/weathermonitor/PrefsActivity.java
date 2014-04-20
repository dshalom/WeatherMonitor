package com.dshalom.weathermonitor;



import android.os.Bundle;
import android.preference.PreferenceActivity;
import com.dshalom.weathermonitor3.R;

public class PrefsActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
	}
}
