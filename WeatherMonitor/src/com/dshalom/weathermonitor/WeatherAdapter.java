package com.dshalom.weathermonitor;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class WeatherAdapter extends ArrayAdapter<WeatherData> {

	Context context;
	int layoutResourceId;
	public ArrayList<WeatherData>weatherDataList;
	static final String TAG = "WeatherAdapter";

	public WeatherAdapter(Context context, int resourceId,
			List<WeatherData> objects) {
		super(context, resourceId, objects);
		this.context = context; 
		this.layoutResourceId = resourceId; 
		this.weatherDataList = (ArrayList<WeatherData>) objects;
		}

	@Override
	public View getView(int position, View row, ViewGroup parent) {

		WeatherHolder holder = null;
		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new WeatherHolder();
			holder.imageViewIcon = (ImageView) row
					.findViewById(R.id.imageViewIcon);
			holder.textViewDay = (TextView) row.findViewById(R.id.textViewDay);
			holder.textViewLow = (TextView) row.findViewById(R.id.textViewLow);
			holder.textViewHigh = (TextView) row.findViewById(R.id.textViewHigh);

			row.setTag(holder);
		} else {
			holder = (WeatherHolder) row.getTag();
		}
		int screenHeight = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight();
		WeatherData weather = weatherDataList.get(position);
		holder.textViewDay.setText(weather.day);
		holder.textViewLow.setText(weather.low);
		holder.textViewHigh.setText(weather.high);
		holder.imageViewIcon.setImageBitmap(weather.bitmap);
		 
		
		//holder.textViewDay.setMinHeight(screenHeight/9);
		return row;

	}

	static class WeatherHolder {
		TextView textViewDay;
		ImageView imageViewIcon;
		TextView textViewLow;
		TextView textViewHigh;
	}

}
