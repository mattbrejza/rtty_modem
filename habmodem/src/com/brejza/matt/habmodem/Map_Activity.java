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

import ukhas.Telemetry_string;

import com.brejza.matt.habmodem.Dsp_service.LocalBinder;

import android.location.Criteria;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class Map_Activity extends MapActivity implements AddPayloadFragment.NoticeDialogListener {

	MapView mapView;
	private StringRxReceiver strrxReceiver;
	private HabitatRxReceiver habirxReceiver;
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
    
    public void btnAddPayload(View view)
    {
        FragmentManager fm = getFragmentManager();

    	AddPayloadFragment di = new AddPayloadFragment();
    	di.setAutoPayload(mService.getFlightPayloadList());
    	di.show(fm, "AddPayload");
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
            case R.id.refresh_button:
            	mService.updateActivePayloadsHabitat();
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
        
        //string receiver
   		if (strrxReceiver == null) strrxReceiver = new StringRxReceiver();
   		IntentFilter intentFilter1 = new IntentFilter(Dsp_service.TELEM_RX);
   		if (!isReg) { registerReceiver(strrxReceiver, intentFilter1); }
   	   
        //habitat receiver
   		if (habirxReceiver == null) habirxReceiver = new HabitatRxReceiver();
   		IntentFilter intentFilter2 = new IntentFilter(Dsp_service.HABITAT_NEW_DATA);
   		if (!isReg) { registerReceiver(habirxReceiver, intentFilter2); }
   		isReg = true;
   		
   	}
       
    @Override
    public void onPause(){
       	super.onPause();
       	if (isReg)
       	{
	       	 if (habirxReceiver != null) unregisterReceiver(habirxReceiver);
	       	 if (strrxReceiver != null) unregisterReceiver(strrxReceiver);
       	}
           
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
            	
            	Balloon_data_fragment fragment = (Balloon_data_fragment) getFragmentManager().findFragmentById(R.id.balloon_data_holder);
            	fragment.updatePayload(mService.getLastString());
            }
        }
    }
    
    private class HabitatRxReceiver extends BroadcastReceiver  {

    	@Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Dsp_service.HABITAT_NEW_DATA)) {
            //Do stuff
            	if (intent.hasExtra(Dsp_service.TELEM_STR))
            	{
            		Telemetry_string str = new Telemetry_string( intent.getStringExtra(Dsp_service.TELEM_STR));
                	item.setPoint(new GeoPoint(str.coords.latitude,str.coords.longitude));
                	itemizedOverlay.requestRedraw();
                	
                	Balloon_data_fragment fragment = (Balloon_data_fragment) getFragmentManager().findFragmentById(R.id.balloon_data_holder);
                	fragment.updatePayload(str);
            	}
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

	@Override
	public void onDialogPositiveClick(DialogFragment dialog, String callsign) {
		// TODO Auto-generated method stub
    	Balloon_data_fragment fragment = (Balloon_data_fragment) getFragmentManager().findFragmentById(R.id.balloon_data_holder);
    	fragment.AddPayload(callsign);
    	mService.listActivePayloads.add(callsign);
	}

}
