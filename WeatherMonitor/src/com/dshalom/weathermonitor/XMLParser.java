package com.dshalom.weathermonitor;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMLParser {

	public XMLParser() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getXmlFromUrl(String url) {
		String xml = null;

		try {
			// defaultHttpClient
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url);

			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			xml = EntityUtils.toString(httpEntity);

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// return XML
		return xml;
	}

	public Document getDomElement(String xml)
			throws ParserConfigurationException, SAXException, IOException {
		Document doc = null;

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		// Configure it to coalesce CDATA nodes
		dbf.setCoalescing(true);
		DocumentBuilder db = dbf.newDocumentBuilder();

		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(xml));
		doc = db.parse(is);

		// return DOM
		doc.getDocumentElement().normalize();
		return doc;
	}

	public String getValue(Element item, String str) {
		NodeList n = item.getElementsByTagName(str);
		return this.getElementValue(n.item(0));
	}

	public final String getElementValue(Node elem) {
		Node child;
		if (elem != null) {
			if (elem.hasChildNodes()) {
				for (child = elem.getFirstChild(); child != null; child = child
						.getNextSibling()) {
					if (child.getNodeType() == Node.TEXT_NODE) {
						return child.getNodeValue();

					} else if (child.getNodeType() == Node.CDATA_SECTION_NODE) {
						CharacterData cd = (CharacterData) child;
						return cd.getData();
					}
				}
			}
		}
		return "";
	}

}
