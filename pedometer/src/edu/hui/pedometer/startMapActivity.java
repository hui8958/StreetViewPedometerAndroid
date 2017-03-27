package edu.hui.pedometer;

import java.util.Date;
import java.util.HashMap;

import android.support.v4.app.FragmentActivity;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.SupportMapFragment;

import edu.hui.pedometer.R;

import edu.hui.pedometer.tools.EventInfo;
import edu.hui.pedometer.tools.GoogleCity;
import edu.hui.pedometer.tools.ExtendAppication;
import edu.hui.pedometer.tools.commandInfo;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class startMapActivity extends FragmentActivity implements
		OnMapClickListener, OnMapLongClickListener, OnMarkerClickListener,
		InfoWindowAdapter, OnInfoWindowClickListener {

	final int RQS_GooglePlayServices = 1;
	Location myLocation;
	TextView tvLocInfo;
	private GoogleMap myMap;
	private SupportMapFragment mMapFragment;
	private ExtendAppication ws = null;
	private PedometerSettings mPedometerSettings;
	private HashMap<Marker, EventInfo> eventMarkerMap;
	private View window = null;
	Marker currentMkr = null;
	private SharedPreferences mSettings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.maps);
		ws = (ExtendAppication) this.getApplicationContext();
		mMapFragment = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.my_map));

		mMapFragment.getView().setVisibility(View.VISIBLE);
		myMap = mMapFragment.getMap();
		myMap.setMyLocationEnabled(true);
		myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		eventMarkerMap = new HashMap<Marker, EventInfo>();
		window = getLayoutInflater().inflate(R.layout.content, null);
		mSettings = PreferenceManager.getDefaultSharedPreferences(this);

		mPedometerSettings = new PedometerSettings(mSettings);

		myMap.setOnMapClickListener(this);
		myMap.setOnMapLongClickListener(this);
		myMap.setOnMarkerClickListener(this);
		myMap.setInfoWindowAdapter(this);
		myMap.setOnInfoWindowClickListener(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		int resultCode =GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getApplicationContext());

		if (resultCode == ConnectionResult.SUCCESS) {
			Toast.makeText(getApplicationContext(),
					"isGooglePlayServicesAvailable SUCCESS", Toast.LENGTH_LONG)
					.show();
		} else {
			GooglePlayServicesUtil.getErrorDialog(resultCode, this,
					RQS_GooglePlayServices);
		}

	}

	/*
	 * @Override public boolean onOptionsItemSelected(MenuItem item) { switch
	 * (item.getItemId()) { case R.id.menu_legalnotices: String LicenseInfo =
	 * GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(
	 * getApplicationContext()); AlertDialog.Builder LicenseDialog = new
	 * AlertDialog.Builder(MainActivity.this);
	 * LicenseDialog.setTitle("Legal Notices");
	 * LicenseDialog.setMessage(LicenseInfo); LicenseDialog.show(); return true;
	 * } return super.onOptionsItemSelected(item); }
	 */

	@Override
	public void onMapLongClick(LatLng point) {
		// TODO Auto-generated method stub

		Marker mk = myMap.addMarker(new MarkerOptions().position(point).title(
				point.toString()));
		EventInfo EventInfo = new EventInfo(point, "Right now - event",
				new Date(), "Exercise");
		eventMarkerMap.put(mk, EventInfo);

	}

	@Override
	public void onMapClick(LatLng point) {
		// TODO Auto-generated method stub
		myMap.animateCamera(CameraUpdateFactory.newLatLng(point));
	}

	@Override
	public boolean onMarkerClick(Marker arg0) {
		// TODO Auto-generated method stub

		arg0.showInfoWindow();

		// ws.getResponse(commandInfo.geoInfo+String.valueOf(m.getPosition().longitude)+"|"+m.getPosition().latitude);
		return true;
	}

	@Override
	public View getInfoContents(Marker marker) {
		// TODO Auto-generated method stub
		currentMkr = marker;
		// EventInfo eventInfo = eventMarkerMap.get(marker);
		GoogleCity gc = new GoogleCity(marker.getPosition().longitude,
				marker.getPosition().latitude);
		String title = gc.getName();

		TextView txtTitle = ((TextView) window
				.findViewById(R.id.txtInfoWindowTitle));

		if (title != null) {

			// Spannable string allows us to edit the formatting of the text.

			SpannableString titleText = new SpannableString(title);

			titleText.setSpan(new ForegroundColorSpan(Color.RED), 0,
					titleText.length(), 0);

			txtTitle.setText(titleText);

		} else {

			txtTitle.setText("");

		}
		// TextView txtSubTitle = ((TextView) window
		// .findViewById(R.id.txtInfoWindowEventType));
		// txtSubTitle.setText("");

		Log.v("maps", "InfoWindow!");
		return window;

	}

	@Override
	public View getInfoWindow(Marker marker) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		// TODO Auto-generated method stub
		currentMkr = marker;
		// EventInfo eventInfo = eventMarkerMap.get(marker);
		/*
		 * Toast.makeText(getBaseContext(), "The date of " + eventInfo.getName()
		 * + " is " + eventInfo.getSomeDate().toLocaleString(),
		 * Toast.LENGTH_LONG).show();
		 */

		new AlertDialog.Builder(this)
				.setTitle("Notice")
				.setMessage("what do you want to do?")
				.setIcon(R.drawable.icon)
				.setPositiveButton("View",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int whichButton) {
								setResult(RESULT_OK);// ������������
								// ws.getResponse(commandInfo.geoInfo+String.valueOf(currentMkr.getPosition().longitude)+"|"+currentMkr.getPosition().latitude);
								ws.getUdpMgr()
										.send(commandInfo.geoInfo
												+ String.valueOf(currentMkr
														.getPosition().longitude)
												+ "|"
												+ currentMkr.getPosition().latitude,
												mPedometerSettings
														.getIpAddress(),8001);

								// finish();

							}
						})
				.setNeutralButton("Start",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// currentMkr.remove();
								ws.getUdpMgr()
										.send(commandInfo.startInfo
												+ String.valueOf(currentMkr
														.getPosition().longitude)
												+ "|"
												+ currentMkr.getPosition().latitude,
												mPedometerSettings
														.getIpAddress(),8001);
								finish();
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// ������������

							}
						}).show();

	}

}
