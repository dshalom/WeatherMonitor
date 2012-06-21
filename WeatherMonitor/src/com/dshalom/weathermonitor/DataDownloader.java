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

enum ErrorCodes {
	NOERROR, POSTCODEERROR, CONNECTIONERROR, OTHERERROR
};

public class DataDownloader extends AsyncTask<String, Void, ErrorCodes> {
	WeatherMonitorActivity parent;
	String xml;
	XMLParser parser;
	final String REQUEST_ITEM = "request";
	final String LOCATION_ITEM = "query";
	final String KEY_ITEM = "weather"; // parent node
	final String KEY_DATE = "date";
	final String KEY_MINTEMPF = "tempMinF";
	final String KEY_MAXTEMPF = "tempMaxF";
	final String KEY_MINTEMPC = "tempMinC";
	final String KEY_MAXTEMPC = "tempMaxC";
	final String KEY_ICON = "weatherIconUrl";
	String tempUnit;
	String location;
	NodeList nl;
	Bitmap[] bitmaps = new Bitmap[3];

	ErrorCodes error;

	public DataDownloader(WeatherMonitorActivity WeatherMonitorActivity) {
		// TODO Auto-generated constructor stub
		parent = WeatherMonitorActivity;
		parser = new XMLParser();
	}

	@Override
	protected ErrorCodes doInBackground(String... params) {
		error = ErrorCodes.NOERROR;
		String loc = params[0];
		tempUnit = params[1];
		String xml = null;
		try {
			xml = makeRequest(loc);
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

		// Configure it to coalesce CDATA nodes
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setCoalescing(true);
		Document doc = parser.getDomElement(xml.toString());

		// get the city details
		nl = doc.getElementsByTagName(REQUEST_ITEM);
		if (nl.getLength() == 0) {
			return ErrorCodes.POSTCODEERROR;
		}
		Element requestElement = (Element) nl.item(0);
		location = parser.getValue(requestElement, LOCATION_ITEM);

		nl = doc.getElementsByTagName(KEY_ITEM);

		if (nl.getLength() == 0) {
			return ErrorCodes.POSTCODEERROR;
		}

		for (int i = 0; i < nl.getLength(); i++) {
			Element e = (Element) nl.item(i);

			String icon = parser.getValue(e, KEY_ICON);
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
		qparams.add(new BasicNameValuePair("key", "6ffa3d38e9115055122204"));
		qparams.add(new BasicNameValuePair("q", loc));
		qparams.add(new BasicNameValuePair("num_of_days", "3"));
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
	protected void onPostExecute(ErrorCodes result) {
		if (result != ErrorCodes.NOERROR) {
			parent.showError(result);
			return;
		}

		super.onPostExecute(result);
	//	parent.textViewLocation.setText(location);
		updateTemps(tempUnit);
	}

	public void updateTemps(String unit) {

		// update unit, depends on unit
		if (unit == "cent") {
			for (int i = 0; i < nl.getLength(); i++) {
				Element e = (Element) nl.item(i);

				// parent.textViewDateArray[i].setText(GetDayOfWeek(parser
				// .getValue(e, KEY_DATE)));
				//
				// parent.textViewDayHighArray[i].setText(parser.getValue(e,
				// KEY_MAXTEMPC) + "°C");
				// parent.textViewDayLowArray[i].setText(parser.getValue(e,
				// KEY_MINTEMPC) + "°C");
				//
				// parent.imageViewArray[i].setImageBitmap(bitmaps[i]);

				WeatherData weatherData = new WeatherData(
						GetDayOfWeek(parser.getValue(e, KEY_DATE)), bitmaps[i],
						parser.getValue(e, KEY_MINTEMPC) + "°C",
						parser.getValue(e, KEY_MAXTEMPC) + "°C");
				
				parent.adapter.add(weatherData);

			}

		} else {
			for (int i = 0; i < nl.getLength(); i++) {
				Element e = (Element) nl.item(i);

				// parent.textViewDateArray[i].setText(GetDayOfWeek(parser
				// .getValue(e, KEY_DATE)));
				//
				// parent.textViewDayHighArray[i].setText(parser.getValue(e,
				// KEY_MAXTEMPF) + "°F");
				// parent.textViewDayLowArray[i].setText(parser.getValue(e,
				// KEY_MINTEMPF) + "°F");
				//
				// parent.imageViewArray[i].setImageBitmap(bitmaps[i]);
			}
		}

	}

	protected String parseXML(String xml) {
		parser.getDomElement(xml);
		return "";
	}

	public XMLParser parser() {
		return parser;
	}

}
