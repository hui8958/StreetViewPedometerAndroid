package edu.hui.pedometer.tools;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import android.util.Log;

public class GoogleCity {
	private Double Latitude;// Î³¶È
	private Double Longitude;// ¾­¶È
	private String url = "";
	public GoogleCity(double longitude, double latitude) {
		super();
		
		
		Latitude =	latitude;
		Longitude = longitude;
		
		url = "http://maps.google.com/maps/api/geocode/xml?latlng="+Latitude+","+Longitude+"&language=en-us&sensor=true";
		
		Log.v("cityUrl", url);
		
		
		
	}
	public String getName() {
		try{
			Log.v("Maps", url);
		DefaultHttpClient client = new DefaultHttpClient();
		HttpUriRequest Request = new HttpGet(url);
		HttpResponse Response = client.execute(Request);
		HttpEntity Entity = Response.getEntity();
		InputStream stream = Entity.getContent();
		DocumentBuilder Builder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document doc = Builder.parse(new InputSource(stream));
		NodeList n  = doc.getElementsByTagName("formatted_address");

		String cityName =  n.item(4).getFirstChild().getNodeValue();
		

		
		
		
		
		
		
		
		
		
		
		
		
		
		return cityName;
		}catch(Exception e){
			
		}
		return "";
		
		
	}

}
