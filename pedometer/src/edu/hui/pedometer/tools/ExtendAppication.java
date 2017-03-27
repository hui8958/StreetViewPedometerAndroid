package edu.hui.pedometer.tools;

import java.io.IOException;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Application;
import android.content.Context;
import android.util.Log;

public class ExtendAppication extends Application {
	
	  
	   private static final String METHOD_NAME = "HelloWorld"; 
	   private static final String NAMESPACE = "http://tempuri.org/"; 
	   private static final String SOAP_ACTION = NAMESPACE + METHOD_NAME; 
	   private static final String URL = "http://192.168.1.14/WebService1.asmx"; 
	   private DBManager mgr ;
	   private UDPManager udpMgr = new UDPManager();
	public ExtendAppication() {
	}
	
	public String onstart(){
		return "start";
	}
	public String getResponse(String info) {
		String Response = "";
		try {

			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			request.addProperty("a", info);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			envelope.dotNet = true;
			envelope.setOutputSoapObject(request);
			HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
			androidHttpTransport.call(SOAP_ACTION, envelope);
			
			Response = String.valueOf( envelope.getResponse());
			Log.v("Response", Response);

		} catch (IOException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
		} catch (XmlPullParserException e) {

			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Response:" + Response);
		return Response;
	}
	public DBManager getMgr() {
		return mgr;
	}
	public void setMgr(Context context) {
		mgr = new DBManager(context);
	}

	public UDPManager getUdpMgr() {
		return udpMgr;
	}


	


}
