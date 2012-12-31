// Copyright 2012 (C) Matthew Brejza
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.


package com.brejza.matt.habmodem;

import java.io.File;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.overlay.ArrayItemizedOverlay;
import org.mapsforge.android.maps.overlay.ArrayWayOverlay;
import org.mapsforge.android.maps.overlay.OverlayItem;
import org.mapsforge.android.maps.overlay.OverlayWay;
import org.mapsforge.core.GeoPoint;

import com.brejza.matt.habmodem.Dsp_service.LocalBinder;

import android.location.Criteria;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v4.app.NavUtils;

public class Map_Activity extends MapActivity {

	MapView mapView;
	private StringRxReceiver strrxReceiver;
	boolean isReg = false;
	
	Drawable defaultMarker;
	ArrayItemizedOverlay itemizedOverlay;
	OverlayItem item;
	
	GeoPoint[][] points;
	
	public OverlayItem overlayMyLocation;
	private Location_handler loc_han;
	private LocationManager locationManager;
	
	Dsp_service mService;
    boolean mBound = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
     
        this.mapView = (MapView) findViewById(R.id.mapView);
        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(true);
        mapView.setMapFile(new File("/sdcard/england.map"));
        
        
        

        // create a default marker for the overlay
        // R.drawable.marker is just a placeholder for your own drawable
        defaultMarker = getResources().getDrawable(R.drawable.ic_launcher);

        // create an ItemizedOverlay with the default marker
        itemizedOverlay = new ArrayItemizedOverlay(defaultMarker);

        // create a GeoPoint with the latitude and longitude coordinates
        GeoPoint geoPoint = new GeoPoint(52.516272, -0.0);

        // create an OverlayItem with title and description
        item = new OverlayItem(geoPoint, "Brandenburg Gate",
                "One of the main symbols of Berlin and Germany.");
        
        overlayMyLocation = new OverlayItem();
        itemizedOverlay.addItem(overlayMyLocation);

        // add the OverlayItem to the ArrayItemizedOverlay
        itemizedOverlay.addItem(item);

       
        //test path
        
        Paint wayDefaultPaintFill = new Paint(Paint.ANTI_ALIAS_FLAG);
		wayDefaultPaintFill.setStyle(Paint.Style.STROKE);
		wayDefaultPaintFill.setColor(Color.BLUE);
		wayDefaultPaintFill.setAlpha(160);
		wayDefaultPaintFill.setStrokeWidth(2);
		wayDefaultPaintFill.setStrokeJoin(Paint.Join.ROUND);
		wayDefaultPaintFill.setPathEffect(new DashPathEffect(new float[] { 20, 20 }, 0));
		Paint wayDefaultPaintOutline = new Paint(Paint.ANTI_ALIAS_FLAG);
		wayDefaultPaintOutline.setStyle(Paint.Style.STROKE);
		wayDefaultPaintOutline.setColor(Color.BLUE);
		wayDefaultPaintOutline.setAlpha(128);
		wayDefaultPaintOutline.setStrokeWidth(7);
		wayDefaultPaintOutline.setStrokeJoin(Paint.Join.ROUND);
		// create an individual paint object for an overlay way
		Paint wayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		wayPaint.setStyle(Paint.Style.FILL);
		wayPaint.setColor(Color.YELLOW);
		wayPaint.setAlpha(192);
		
		GeoPoint geoPoint1 = new GeoPoint(51.45,-0.3);
		GeoPoint geoPoint2 = new GeoPoint(51.75,-0.4);
		GeoPoint geoPoint3 = new GeoPoint(50.75,-0.2);

        ArrayWayOverlay wayover = new ArrayWayOverlay(wayDefaultPaintFill,wayDefaultPaintOutline);
        points = new GeoPoint[][] { { geoPoint1, geoPoint2 } };
        OverlayWay way1 = new OverlayWay(points);
		
		wayover.addWay(way1);

        
        
        
        // add the ArrayItemizedOverlay to the MapView
		 mapView.getOverlays().add(itemizedOverlay);
		 mapView.getOverlays().add(wayover);
        
        
      
		 points = new GeoPoint[][]{ { geoPoint1, geoPoint2, geoPoint3 } };
		 way1 = new OverlayWay(points);
        
        //string receiver
   		if (strrxReceiver == null) strrxReceiver = new StringRxReceiver();
   		IntentFilter intentFilter1 = new IntentFilter(Dsp_service.TELEM_RX);
   		if (!isReg) { registerReceiver(strrxReceiver, intentFilter1); }
   		
   		
   		loc_han = new Location_handler(this);
   		this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    public void btnEnableLocation(View view)
    {
    	//my location part
        Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
        
        String bestProvider = this.locationManager.getBestProvider(criteria, true);
        if (bestProvider == null)
        	return;
        
        this.locationManager.requestLocationUpdates(bestProvider, 1000, 0, this.loc_han);
        
        
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
    	Intent intent;
        switch (item.getItemId()) {
        	case android.R.id.home:
        		NavUtils.navigateUpFromSameTask(this);
            case R.id.status_screen:
            	intent = new Intent(this, StatusScreen.class);
            	startActivity(intent);
                return true;
            case R.id.fft_screen:
            	intent = new Intent(this, FFTActivity.class);
            	startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
   	public void onResume() {
   		super.onResume();

   		
   	 // Bind to LocalService
        Intent intent = new Intent(this, Dsp_service.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
   	}
       
    @Override
    public void onPause(){
       	super.onPause();
       	//if (isReg)
        //	   if (strrxReceiver != null) unregisterReceiver(strrxReceiver);
           
           isReg = false;
           
       if (mBound) {
           unbindService(mConnection);
           mBound = false;
       }
    }
    
    private class StringRxReceiver extends BroadcastReceiver  {

    	@Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Dsp_service.TELEM_RX)) {
            //Do stuff
            	System.out.println("GOT INTENT telem  " + mService.getLastString().coords.latitude + "  " + mService.getLastString().coords.longitude);
            //	list.add
            	item.setPoint(new GeoPoint(mService.getLastString().coords.latitude,mService.getLastString().coords.longitude));
            	itemizedOverlay.requestRedraw();
            }
        }
    }
    
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

}
