package com.dshalom.weathermonitor;



import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.dshalom.weathermonitor2.R;

public class PrefsActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
	}
}
