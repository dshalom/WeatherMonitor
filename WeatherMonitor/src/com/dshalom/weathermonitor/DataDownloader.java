package com.dshalom.weathermonitor;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.NodeList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import com.dshalom.weathermonitor2.R;


enum ErrorCodes {
	NOERROR, POSTCODEERROR, CONNECTIONERROR, OTHERERROR
};

public class DataDownloader extends AsyncTask<String, Void, ErrorCodes> {
	WeatherMonitorActivity parent;
	String response;

	String tempUnit;
	String location;
	NodeList nl;
	Bitmap[] bitmaps = new Bitmap[5];
	JSONArray weatherDataArray;
	JSONObject whetherDataObject;
	final String REQUEST_ITEM = "request";
	final String LOCATION_ITEM = "query";
	final String KEY_ITEM = "weather"; // parent node
	final String KEY_DATE = "date";
	final String KEY_MINTEMPF = "tempMinF";
	final String KEY_MAXTEMPF = "tempMaxF";
	final String KEY_MINTEMPC = "tempMinC";
	final String KEY_MAXTEMPC = "tempMaxC";
	final String KEY_ICON = "weatherIconUrl";
	final String DEGREE = "\u00b0";

	ErrorCodes error;

	public DataDownloader(WeatherMonitorActivity WeatherMonitorActivity) {
		// TODO Auto-generated constructor stub
		parent = WeatherMonitorActivity;
	}

	@Override
	protected ErrorCodes doInBackground(String... params) {
		error = ErrorCodes.NOERROR;
		String loc = params[0];
		try {
			response = makeRequest(loc);
		} catch (URISyntaxException e) {
			// possibly pc is wrong
			e.printStackTrace();
			return ErrorCodes.POSTCODEERROR;
		} catch (ClientProtocolException e) {
			// error making connection
			e.printStackTrace();
			return ErrorCodes.CONNECTIONERROR;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ErrorCodes.CONNECTIONERROR;
		}

		try {
			whetherDataObject = new JSONObject(response);
			weatherDataArray = whetherDataObject.getJSONObject("data")
					.getJSONArray("weather");
			;

			location = whetherDataObject.getJSONObject("data")
					.getJSONArray("request").getJSONObject(0)
					.getString("query");
		} catch (JSONException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		for (int i = 0; i < weatherDataArray.length(); i++) {

			String icon = null;
			try {
				icon = weatherDataArray.getJSONObject(0)
						.getJSONArray("weatherIconUrl").getJSONObject(0)
						.getString("value");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			URL url = null;
			try {
				url = new URL(icon);
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
				return ErrorCodes.OTHERERROR;
			}
			URLConnection connection = null;
			InputStream is = null;
			try {
				connection = url.openConnection();
				connection.connect();
				is = connection.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(is);
				bitmaps[i] = BitmapFactory.decodeStream(bis);
				bis.close();
				is.close();
			} catch (IOException e1) {
				e1.printStackTrace();
				return ErrorCodes.CONNECTIONERROR;
			}
		}
		return error;
	}

	public String makeRequest(String loc) throws URISyntaxException,
			ClientProtocolException, IOException {

		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair("key", "kp4wbe8jcnuzfc4bfj36p8fs"));
		qparams.add(new BasicNameValuePair("q", loc));
		qparams.add(new BasicNameValuePair("num_of_days", "5"));
		qparams.add(new BasicNameValuePair("format", "json"));
		URI uri = null;

		uri = URIUtils.createURI("http", "api.worldweatheronline.com", -1,
				"/free/v1/weather.ashx",
				URLEncodedUtils.format(qparams, "UTF-8"), null);

		// return XML
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(uri);
		HttpParams params = httppost.getParams();
		HttpConnectionParams.setConnectionTimeout(params, 3000);
		HttpConnectionParams.setSoTimeout(params, 3000);
		httppost.setParams(params);

		HttpResponse httpResponse = null;
		httpResponse = httpClient.execute(httppost);

		HttpEntity httpEntity = httpResponse.getEntity();
		return EntityUtils.toString(httpEntity);
	}

	private String GetDayOfWeek(String date) {

		String[] separated = date.split("-");

		Calendar cal = new GregorianCalendar(Integer.parseInt(separated[0]),
				Integer.parseInt(separated[1]) - 1,
				Integer.parseInt(separated[2]));

		String day;
		switch (cal.get(Calendar.DAY_OF_WEEK)) {

		case 1:
			day = "Sun";
			break;
		case 2:
			day = "Mon";
			break;
		case 3:
			day = "Tue";
			break;
		case 4:
			day = "Wed";
			break;
		case 5:
			day = "Thu";
			break;
		case 6:
			day = "Fri";
			break;
		case 7:
			day = "Sat";
			break;
		default:
			day = "Invalid day";
			break;

		}
		return day;
	}

	@Override
	protected void onPostExecute(ErrorCodes result) {
		if (result != ErrorCodes.NOERROR) {
			// parent.showError(result);
			return;
		}

		super.onPostExecute(result);
		parent.updateLocation(location);
		try {
			updateData();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateData() throws JSONException {

		// find out what temperature unit to extract
		String unit = parent.prefs.getString("prefTemperatureUnit",
				"Centigrade");

		String degrees = null;
		
		parent.adapter.clear();

		if (unit.equals("Centigrade")) {

			degrees = DEGREE + "C";
			for (int i = 0; i < weatherDataArray.length(); i++) {
				JSONObject e = weatherDataArray.getJSONObject(i);

				WeatherData weatherData = new WeatherData(
						GetDayOfWeek(e.getString(KEY_DATE)), bitmaps[i],
						e.getString(KEY_MINTEMPC) + degrees,
						e.getString(KEY_MAXTEMPC) + degrees);
				parent.adapter.add(weatherData);
			}
		} else {

			degrees = DEGREE + "F";
			for (int i = 0; i < weatherDataArray.length(); i++) {
				JSONObject e = weatherDataArray.getJSONObject(i);

				WeatherData weatherData = new WeatherData(
						GetDayOfWeek(e.getString(KEY_DATE)), bitmaps[i],
						e.getString(KEY_MINTEMPF) + degrees,
						e.getString(KEY_MAXTEMPF) + degrees);
				parent.adapter.add(weatherData);
			}

		}

		// update last refresh and button
		String lastRefresh = "Updated: ";
		lastRefresh += DateFormat.getDateInstance().format(new Date());
		DateFormat dateFormat = new SimpleDateFormat("HH:mm a");
		lastRefresh += " " + dateFormat.format(new Date());
		parent.textViewLastRefresh.setText(lastRefresh);

		parent.buttonRefresh.setText(R.string.refresh);
		parent.buttonRefresh.setClickable(true);

	}

}
