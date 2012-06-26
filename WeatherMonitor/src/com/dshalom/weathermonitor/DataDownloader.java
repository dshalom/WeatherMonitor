package com.dshalom.weathermonitor;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

enum ErrorCode {
	NOERROR, POSTCODEERROR, CONNECTIONERROR, OTHERERROR
};

public class DataDownloader extends AsyncTask<String, Void, ErrorCode> {
	WeatherMonitorActivity parent;

	private static final String TAG = "DataDownloader";
	// private static final int NUMBEROFDAYS = 5;
	private static final String REQUEST_ITEM = "request";
	private static final String LOCATION_ITEM = "query";
	private static final String KEY_ITEM = "weather"; // parent node
	private static final String KEY_DATE = "date";
	private static final String KEY_MINTEMPF = "tempMinF";
	private static final String KEY_MAXTEMPF = "tempMaxF";
	private static final String KEY_MINTEMPC = "tempMinC";
	private static final String KEY_MAXTEMPC = "tempMaxC";
	private static final String KEY_ICON = "weatherIconUrl";
	private XMLParser parser;
	private String location;
	private Bitmap[] bitmaps;
	private NodeList nodeList;
	private static int numberOfDays;

	public DataDownloader(WeatherMonitorActivity parent) {
		this.parent = parent;
		parser = new XMLParser();

		DisplayMetrics metrics = new DisplayMetrics();
		parent.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		//on a low density screen only display 4 days of data
		numberOfDays = (metrics.densityDpi == DisplayMetrics.DENSITY_LOW) ? 4 : 5;
		bitmaps = new Bitmap[numberOfDays];
		
	}

	@Override
	protected ErrorCode doInBackground(String... params) {

		Log.d(TAG, String.format("doInBackground location: %S", params[0]));
		ErrorCode error = ErrorCode.NOERROR;
		String loc = params[0];
		String xml = null;
		// make request
		try {
			xml = makeRequest(loc);
		} catch (URISyntaxException e) {
			// possibly pc is wrong
			e.printStackTrace();
			return ErrorCode.POSTCODEERROR;
		} catch (ClientProtocolException e) {
			// error making connection
			e.printStackTrace();
			return ErrorCode.CONNECTIONERROR;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ErrorCode.CONNECTIONERROR;
		}

		// Configure it to coalesce CDATA nodes
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setCoalescing(true);
		Document doc = parser.getDomElement(xml.toString());

		// get the city or postcode details details
		nodeList = doc.getElementsByTagName(REQUEST_ITEM);
		if (nodeList.getLength() == 0) {
			return ErrorCode.POSTCODEERROR;
		}
		Element requestElement = (Element) nodeList.item(0);
		location = parser.getValue(requestElement, LOCATION_ITEM);

		// start of daily report
		nodeList = doc.getElementsByTagName(KEY_ITEM);

		if (nodeList.getLength() == 0) {
			return ErrorCode.POSTCODEERROR;
		}
		// should be 5 days reported
		for (int i = 0; i < nodeList.getLength(); i++) {
			Element e = (Element) nodeList.item(i);

			// pare bitmap info and fetch
			// the rest of the information is collected in onPostExecute
			String icon = parser.getValue(e, KEY_ICON);
			URL url = null;
			try {
				url = new URL(icon);
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
				return ErrorCode.OTHERERROR;
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
				return ErrorCode.CONNECTIONERROR;
			}
		}
		return error;
	}

	public String makeRequest(String loc) throws URISyntaxException,
			ClientProtocolException, IOException {

		// prepare request
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();
		qparams.add(new BasicNameValuePair("key", "6ffa3d38e9115055122204"));
		qparams.add(new BasicNameValuePair("q", loc));
		qparams.add(new BasicNameValuePair("num_of_days", String
				.valueOf(numberOfDays)));
		qparams.add(new BasicNameValuePair("format", "xml"));
		URI uri = null;

		uri = URIUtils.createURI("http", "free.worldweatheronline.com", -1,
				"/feed/weather.ashx", URLEncodedUtils.format(qparams, "UTF-8"),
				null);

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
	protected void onPostExecute(ErrorCode result) {
		if (result != ErrorCode.NOERROR) {
			parent.showError(result);
			return;
		}

		super.onPostExecute(result);
		parent.textViewLocation.setText(location);
		parent.adapter.clear();
		updateData();
	}

	public void updateData() {

		// find out what temperature unit to extract
		String unit = parent.prefs.getString("prefTemperatureUnit",
				"Centigrade");
		String toMinTempToExtract = null;
		String toMaxTempToExtract = null;
		String degrees = null;

		if (unit.equals("Centigrade")) {
			toMinTempToExtract = KEY_MINTEMPC;
			toMaxTempToExtract = KEY_MAXTEMPC;
			degrees = "°C";
		} else {
			toMinTempToExtract = KEY_MINTEMPF;
			toMaxTempToExtract = KEY_MAXTEMPF;
			degrees = "°F";

		}
		Log.d(TAG, String.format("updateData %s %s", toMinTempToExtract,
				toMaxTempToExtract));
		// update the data
		for (int i = 0; i < nodeList.getLength(); i++) {
			Element e = (Element) nodeList.item(i);

			WeatherData weatherData = new WeatherData(
					GetDayOfWeek(parser.getValue(e, KEY_DATE)), bitmaps[i],
					parser.getValue(e, toMinTempToExtract) + degrees,
					parser.getValue(e, toMaxTempToExtract) + degrees);

			parent.adapter.add(weatherData);

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
