package com.brejza.matt.habmodem;

import org.mapsforge.core.GeoPoint;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class Location_handler implements LocationListener  {

	private final Map_Activity mapact;
	

	
	public Location_handler(Map_Activity mapactivity) {
		// TODO Auto-generated constructor stub
		mapact = mapactivity;
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		mapact.overlayMyLocation.setPoint(new GeoPoint(location.getLatitude(),location.getLongitude()));
		mapact.itemizedOverlay.requestRedraw();
	}
	
	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

}
