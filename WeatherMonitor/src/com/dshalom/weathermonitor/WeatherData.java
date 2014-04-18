package com.dshalom.weathermonitor;

import android.graphics.Bitmap;

public class WeatherData {
	String day, low, high;
	Bitmap bitmap;

	public WeatherData(String day, Bitmap bitmap, String low, String high) {
		this.day = day;
		this.low = low;
		this.high = high;
		this.bitmap = bitmap;

	}
}