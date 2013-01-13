package com.brejza.matt.habmodem;

import org.mapsforge.android.maps.overlay.ItemizedOverlay;
import org.mapsforge.android.maps.overlay.OverlayItem;
import org.mapsforge.core.GeoPoint;

import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class Location_handler implements LocationListener  {

	private final Map_Activity mapact;
	Drawable _icon;

	
	public Location_handler(Map_Activity mapactivity, Drawable icon) {
		// TODO Auto-generated constructor stub
		mapact = mapactivity;
		_icon = icon;
		
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		if (mapact.overlayMyLocation == null)
		{
			mapact.overlayMyLocation = new OverlayItem(new GeoPoint(location.getLatitude(),location.getLongitude()),
														"User Location","",ItemizedOverlay.boundCenterBottom(_icon));
			mapact.array_img_balloons.addItem(mapact.overlayMyLocation);
			mapact.array_img_balloons.requestRedraw();
		}
		else
		{
			mapact.overlayMyLocation.setPoint(new GeoPoint(location.getLatitude(),location.getLongitude()));
			mapact.array_img_balloons.requestRedraw();
		}
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
