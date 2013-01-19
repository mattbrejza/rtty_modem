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
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import android.graphics.Color;
import android.graphics.Paint;
import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.overlay.ArrayItemizedOverlay;
import org.mapsforge.android.maps.overlay.ArrayWayOverlay;
import org.mapsforge.android.maps.overlay.ItemizedOverlay;
import org.mapsforge.android.maps.overlay.OverlayItem;
import org.mapsforge.android.maps.overlay.OverlayWay;
import org.mapsforge.core.GeoPoint;

import ukhas.Gps_coordinate;
import ukhas.Telemetry_string;

import com.brejza.matt.habmodem.Dsp_service.LocalBinder;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import group.pals.android.lib.ui.filechooser.FileChooserActivity;
import group.pals.android.lib.ui.filechooser.io.localfile.LocalFile;


public class Map_Activity extends MapActivity implements AddPayloadFragment.NoticeDialogListener,LocationSelectFragment.NoticeDialogListener {

	MapView mapView;
	private StringRxReceiver strrxReceiver;
	private HabitatRxReceiver habirxReceiver;
	private GPSRxReceiver gpsrxReceiver;
	private LogEventReceiver logReceiver;
	boolean isReg = false;
	boolean requestUpdate = false;
	
	private static final int _ReqChooseFile = 0;

	boolean _mapFile_set = false;

	Timer timerLogging;
	private Handler handler;

	Button btnMapPath;
	
	protected int last_colour = 0x0;
	public ConcurrentHashMap<String,Integer> path_colours = new ConcurrentHashMap<String,Integer>();
	
	//Drawable defaultMarker;
	//ArrayItemizedOverlay itemizedOverlay;
	protected ArrayItemizedOverlay array_img_balloons;
	private ArrayWayOverlay array_waypoints; 
	protected OverlayItem overlayMyLocation;
	
	private ConcurrentHashMap<String,OverlayWay> map_path_overlays = new ConcurrentHashMap<String,OverlayWay>();
	protected ConcurrentHashMap<String,OverlayItem> map_balloon_overlays = new ConcurrentHashMap<String,OverlayItem>();
	
	Dsp_service mService;
    boolean mBound = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
      //  getActionBar().setDisplayHomeAsUpEnabled(true);
        
        mapView = (MapView) findViewById(R.id.mapView);
    	 mapView.setClickable(true);
    	 mapView.setBuiltInZoomControls(true);
        
    	// mapView.setMapFile(new File("/mnt/sdcard/Pictures/england.map"));
        
         //////////////////////////////////
         //now for some stuff that isnt test code
        
        Paint dw = new Paint(Paint.ANTI_ALIAS_FLAG);
        dw.setStyle(Paint.Style.STROKE);
        dw.setColor(Color.BLUE);
         
        array_waypoints = new  ArrayWayOverlay(dw,dw);
        mapView.getOverlays().add(array_waypoints);
         

        array_img_balloons = new ArrayItemizedOverlay(getResources().getDrawable(R.drawable.ic_map_balloon));
        
        mapView.getOverlays().add(array_img_balloons);
        
        setMapFile();
        
        handler = new Handler(); //for timer to interact with ui
    }

    
    private void setMapFile(){
    	
    	if (_mapFile_set)
    		return;
          String mapst = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("pref_map_path", "");
       //  SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
       //  String mapst = settings.getString("silentMode", "");
         
        //  mapst = "/mnt/sdcard/Alarms/england.map";
         File file = new File(mapst);
         if(file.exists())
         {
	         mapView.setMapFile(file);
	         _mapFile_set = true;
	
         }
      /*   else if (!_drawFileButton)
         {
        	 _mapFile_set = false;
        	 _drawFileButton = true;
        	 btnMapPath = new Button(this);
        	 btnMapPath.setText("Select Map File");
        	// android:id="@+id/balloon_data_holder"
        			 
        	 RelativeLayout ll = (RelativeLayout)findViewById(R.id.map_rel_lay);
        	 RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
        			 RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        	 lp.addRule(RelativeLayout.CENTER_VERTICAL);
        	 lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        	 
        	 btnMapPath.setOnClickListener(new OnClickListener() {
     			@Override
     			public void onClick(View v) {
     				// Display the file chooser dialog
     				showMapChooser();
     			}
     		});
        	 
        	ll.addView(btnMapPath, lp);
         } */
    }
    
    private void showMapChooser()
    {
    	Intent intent = new Intent(Map_Activity.this, FileChooserActivity.class);
		intent.putExtra(FileChooserActivity._Rootpath, (Parcelable) new LocalFile(Environment.getExternalStorageDirectory().getPath() ));
		intent.putExtra(FileChooserActivity._RegexFilenameFilter, "(?si).*\\.(map)$");
		intent.putExtra(FileChooserActivity._Theme, android.R.style.Theme_Dialog);
		startActivityForResult(intent, _ReqChooseFile);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case _ReqChooseFile:
            if (resultCode == RESULT_OK) {
                /*
                 * you can use two flags included in data
                 */
               // IFileProvider.FilterMode filterMode = (IFileProvider.FilterMode)
               //     data.getSerializableExtra(FileChooserActivity._FilterMode);
               // boolean saveDialog = data.getBooleanExtra(FileChooserActivity._SaveDialog, false);

                /*
                 * a list of files will always return,
                 * if selection mode is single, the list contains one file
                 */
                List<LocalFile> files = (List<LocalFile>)
                    data.getSerializableExtra(FileChooserActivity._Results);
                for (File f : files)
                {
                	PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).edit().putString("pref_map_path", f.getPath()).commit();
                	System.out.println(f.toString());
                }
             
              //  RelativeLayout ll = (RelativeLayout)findViewById(R.id.balloon_data_holder);
              //  ll.removeView(btnMapPath);
              //  _drawFileButton = false;
                
                if (files.size() > 0){
	                if(files.get(0).exists())
	                {
	       	         mapView.setMapFile(files.get(0));
	       	         _mapFile_set = true;
	       	
	                }
                }
                                
            }
            break;
        }
    }
    
    public void btnAddPayload(View view)
    {
        FragmentManager fm = getFragmentManager();

    	AddPayloadFragment di = new AddPayloadFragment();
    	di.setAutoPayload(mService.getPayloadList());
    	di.show(fm, "AddPayload");
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public void onStart()
    {
    	super.onStart();
    	  //   // Bind to LocalService
        Intent intent = new Intent(this, Dsp_service.class);    
        startService(intent);
    	requestUpdate = true;
    	System.out.println("DEBUG : STARTING ACTIVITY");
    }
    
   
    @Override
    public void onStop()
    {
    	super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection 
    	/*
    	Intent intent;
        switch (item.getItemId()) {
        	case android.R.id.home:
        		NavUtils.navigateUpFromSameTask(this);
            case R.id.status_screen:
            	intent = new Intent(this, StatusScreen.class);
            	startActivity(intent);
                return true;
            case R.id.location_dialog:
            	FragmentManager fm = getFragmentManager();
            	LocationSelectFragment di = new LocationSelectFragment();
            	di.enChase = mService.getEnableChase();
            	di.enPos = mService.getEnablePosition();
             	di.show(fm, "Location Settings");
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
        } */
    	Intent intent;

    	/*if (item.getItemId() == android.R.id.home)
    		NavUtils.navigateUpFromSameTask(this);
    	else */ if (item.getItemId() == R.id.status_screen) {
        	intent = new Intent(this, StatusScreen.class);
        	startActivity(intent);
            return true; }
        else if (item.getItemId() ==  R.id.location_dialog) {
        	FragmentManager fm = getFragmentManager();
        	LocationSelectFragment di = new LocationSelectFragment();
        	di.enChase = mService.getEnableChase();
        	di.enPos = mService.getEnablePosition();
         	di.show(fm, "Location Settings");
        	return true; }
        else if (item.getItemId() ==  R.id.refresh_button) {
        	mService.updateActivePayloadsHabitat();
        	return true; }
        else if (item.getItemId() ==  R.id.fft_screen) {
        	intent = new Intent(this, FFTActivity.class);
        	startActivity(intent);
            return true; }
        else if (item.getItemId() ==  R.id.settings_screen) {
        	intent = new Intent(this,Preferences_activity.class);
        	startActivity(intent); 
            return true;}
        
        return super.onOptionsItemSelected(item);
    
    }
    
    
    @Override
   	public void onResume() {
   		super.onResume();
   		
   		if (mapView.getMapFile() == null)
   			showMapChooser();
   		
   		if (!mBound){
	        Intent intent = new Intent(this, Dsp_service.class);
	        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
   		}
        
        //string receiver
   		if (strrxReceiver == null) strrxReceiver = new StringRxReceiver();
   		IntentFilter intentFilter1 = new IntentFilter(Dsp_service.TELEM_RX);
   		if (!isReg) { registerReceiver(strrxReceiver, intentFilter1); }
   	   
   		//habitat receiver
   		if (habirxReceiver == null) habirxReceiver = new HabitatRxReceiver();
   		IntentFilter intentFilter2 = new IntentFilter(Dsp_service.HABITAT_NEW_DATA);
   		if (!isReg) { registerReceiver(habirxReceiver, intentFilter2); }
   		
   		
   		//gps receiver
   		if (gpsrxReceiver == null) gpsrxReceiver = new GPSRxReceiver();
   		IntentFilter intentFilter3 = new IntentFilter(Dsp_service.GPS_UPDATED);
   		if (!isReg) { registerReceiver(gpsrxReceiver, intentFilter3); }
   		
   		//log receiver
   		if (logReceiver == null) logReceiver = new LogEventReceiver();
   		IntentFilter intentFilter4 = new IntentFilter(Dsp_service.LOG_EVENT);
   		if (!isReg) { registerReceiver(logReceiver, intentFilter4); }
   		
   		isReg = true;
   	
   		
   	}
       
    @Override
    public void onPause(){
       	super.onPause();
       	if (isReg)
       	{
	    	if (habirxReceiver != null) unregisterReceiver(habirxReceiver);
	       	if (strrxReceiver != null) unregisterReceiver(strrxReceiver);
	       	if (gpsrxReceiver != null) unregisterReceiver(gpsrxReceiver);
	       	if (logReceiver != null) unregisterReceiver(logReceiver);
       	}
        isReg = false;  
    }
    
    protected int getColour(String callsign)
    {
    	if (path_colours.containsKey(callsign.toUpperCase()))
    		return path_colours.get(callsign.toUpperCase()).intValue();
    	else {
    		int c = newColour();
    		path_colours.put(callsign.toUpperCase(), Integer.valueOf(c));
    		return c;
    	}    		
    }
    
    private int newColour()
    {
    	if (last_colour == 0)
    	{
    		last_colour = 0xFFFF0000;
    		return last_colour;
    	}
    	else
    	{
    		float lasthsv[]= new float[3];
    		Color.colorToHSV(last_colour,lasthsv);
    		lasthsv[0] = (lasthsv[0] + (180 + 33)) % 360;
    		last_colour = Color.HSVToColor(lasthsv);
    		return last_colour;
    	}
    }
    
    private void UpdateBalloonLocation(Gps_coordinate coord, String callsign)
    {
    	if (mapView == null)
    		return ;
    	if (map_balloon_overlays.containsKey(callsign) && coord.latlong_valid)
    	{
    		map_balloon_overlays.get(callsign).setPoint(new GeoPoint(coord.latitude,coord.longitude));
    	}
    	else
    	{
    		OverlayItem i = new OverlayItem(new GeoPoint(coord.latitude,coord.longitude), callsign, callsign + " location");
    		array_img_balloons.addItem(i);
    		array_img_balloons.requestRedraw();
    	}
    }
    
    private void UpdateBalloonTrack(TreeMap<Long,Telemetry_string> telem, String callsign, boolean reAdd, boolean forceAppend)//, long dataStartTime, long dataEndTime)
    {
    	if (mapView == null)
    		return ;	
    	callsign = callsign.toUpperCase();
    	GeoPoint lp=new GeoPoint(0,0);
    	//step1: check to see if data already exists
    	if (map_path_overlays.containsKey(callsign))
    	{
    		//if (last_update_time.containsKey(callsign))
    		//{
    			if (forceAppend)// || last_update_time.get(callsign).longValue() < dataStartTime)
    			{
    				//no conflict, just keep drawing
    				System.out.println("Update track, no conflict - add to end");
    				OverlayWay way = map_path_overlays.get(callsign);
    				int size_org = way.getWayNodes()[0].length;
    				GeoPoint[][] points = new GeoPoint[1][telem.size() + size_org];
    				 
    				//copy old points into new object
    				System.arraycopy(way.getWayNodes()[0], 0, points[0], 0, size_org);
    				 
    				//add new points to array
    			
    				int i=0;
					Iterator it = telem.entrySet().iterator();
				    while (it.hasNext()) {
				    	TreeMap.Entry pairs = (TreeMap.Entry)it.next();
				    	Telemetry_string ts = (Telemetry_string) pairs.getValue();
				    	if (ts != null){
	            		if (ts.coords != null)
	            			lp =  new GeoPoint(ts.coords.latitude,ts.coords.longitude);  
		            	}      
		            	points[0][size_org+i] = lp;
		            	i++;
				      //  it.remove(); // avoids a ConcurrentModificationException
				    }
    				    
		            	
    				way.setWayNodes(points);
    				array_waypoints.requestRedraw();
    				 
    			}
    			else if (reAdd)
    			{
    				 //wipe and start again, so get the data from the service
    				System.out.println("Update track - wipe old array");
    				OverlayWay way = map_path_overlays.get(callsign);
    				
	    			TreeMap<Long, Telemetry_string> tm = mService.getPayloadData(callsign);
	    			if (tm != null){
	    				GeoPoint[][] points = new GeoPoint[1][tm.size()];
	    				
	    				//add new points to array
	    				int i=0;
						Iterator it = telem.entrySet().iterator();
					    while (it.hasNext()) {
					    	TreeMap.Entry pairs = (TreeMap.Entry)it.next();
					    	Telemetry_string ts = (Telemetry_string) pairs.getValue();
					    	if (ts != null){
		            		if (ts.coords != null)
		            			lp =  new GeoPoint(ts.coords.latitude,ts.coords.longitude);  
			            	}      
			            	points[0][i] = lp;
			            	i++;
					       // it.remove(); // avoids a ConcurrentModificationException
					    }
	    				
	    				//add new points to array (old)
					    /*
	    				for (int i = 0 ; i < tm.size(); i++)
			            {
			            	Telemetry_string ts = (tm.get(i));
			            	if (ts != null){
			            		if (ts.coords != null)
			            			lp =  new GeoPoint(ts.coords.latitude,ts.coords.longitude);  
			            	}      
			            	points[0][i] = lp;
			            }
	    				way.setWayNodes(points);
	    				array_waypoints.requestRedraw(); */
    				}
    			}
    			else
    			{
    				 //do nothing
    				System.out.println("Update track - do nothing");
    			}
    		//}
    	}
    	else
    	{
    		//create new objects and start from scratch.
    		//ignore reAdd as there was nothing here to begin with
    		System.out.println("Update track - new track");
    		
    		GeoPoint[][] points = new GeoPoint[1][telem.size()];
    		/*
            for (int i = 0 ; i < telem.size(); i++)
            {
            	Telemetry_string ts =(telem.get(i));
            	if (ts != null)
            	{
            		if (ts.coords != null)
            			lp =  new GeoPoint(ts.coords.latitude,ts.coords.longitude);                    			 

            	}      
            	points[0][i] = lp;
             }
            */
    		//add new points to array
			int i=0;
			Iterator it = telem.entrySet().iterator();
		    while (it.hasNext()) {
		    	TreeMap.Entry pairs = (TreeMap.Entry)it.next();
		    	Telemetry_string ts = (Telemetry_string) pairs.getValue();
		    	if (ts != null){
        		if (ts.coords != null)
        			lp =  new GeoPoint(ts.coords.latitude,ts.coords.longitude);  
            	}      
            	points[0][i] = lp;
            	i++;
		        //it.remove(); // avoids a ConcurrentModificationException
		    }
            
            
            
    		Paint linepaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    		linepaint.setStyle(Paint.Style.STROKE);
    		linepaint.setColor(getColour(callsign));
    		linepaint.setAlpha(128);
    		linepaint.setStrokeWidth(4);
    		linepaint.setStrokeJoin(Paint.Join.ROUND);
            
    		OverlayWay way = new OverlayWay(points,linepaint,linepaint);
    		
    		array_waypoints.addWay(way);
    		map_path_overlays.put(callsign.toUpperCase(), way);
    		
    		array_waypoints.requestRedraw();
    		
    	}  
    	//last_update_time.put(callsign.toUpperCase(), Long.valueOf(dataEndTime));	
    	System.out.println("Update track - done");
    }
    
    private void updateAll()
    {
    	if (mapView == null)
    		return ;
    	
    	Balloon_data_fragment fragment = (Balloon_data_fragment) getFragmentManager().findFragmentById(R.id.balloon_data_holder);
    	    	
    	//try
    	//{
    	List<String> flights = mService.getActivePayloadList();
    	if (mBound && mService != null)
    	{
	    	for (int i = 0; i < flights.size(); i++)
	    	{
	    		String call = flights.get(i).toUpperCase();
	    		fragment.AddPayload(flights.get(i),getColour(call));
	    		TreeMap <Long, Telemetry_string> tm = mService.getPayloadData(call);
	    		if (tm.size() > 0){
	        		UpdateBalloonTrack(tm,call,true, false);
	        		
		    		fragment.updatePayload(tm.lastEntry().getValue(),mService.getAscentRate(call));
		    		UpdateBalloonLocation(tm.lastEntry().getValue().coords,call);
		    	}
	    	}
	    	requestUpdate = false;
    	}
    	
    	
    		
    	//}
    	//catch (Exception e)
    	//{
    		
    	//}
    }
    
    private class StringRxReceiver extends BroadcastReceiver  {

    	@Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Dsp_service.TELEM_RX)) {
            //Do stuff
            	System.out.println("GOT INTENT telem  " + mService.getLastString().coords.latitude + "  " + mService.getLastString().coords.longitude);
            //	list.add
            	//item.setPoint(new GeoPoint(mService.getLastString().coords.latitude,mService.getLastString().coords.longitude));
            	//itemizedOverlay.requestRedraw();
            	UpdateBalloonLocation(mService.getLastString().coords,mService.getLastString().callsign);
   
            	TreeMap<Long,Telemetry_string> l = new TreeMap<Long,Telemetry_string>(); 
				l.put(mService.getLastString().time.getTime(),mService.getLastString());
				UpdateBalloonTrack(l,mService.getLastString().callsign, false, true);//, 0, System.currentTimeMillis() / 1000L );
            	
            	Balloon_data_fragment fragment = (Balloon_data_fragment) getFragmentManager().findFragmentById(R.id.balloon_data_holder);
            	fragment.updatePayload(mService.getLastString(),mService.getAscentRate(mService.getLastString().callsign));
            }
        }
    }
    
    
    private class GPSRxReceiver extends BroadcastReceiver  {

    	@Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Dsp_service.GPS_UPDATED)) {
            //Do stuff
            	if (mService.currentLocationValid && mService.getEnablePosition())
            	{
            		
            		if (overlayMyLocation == null)
            		{
            		//	System.out.println("DEBUG : adding user onto map new  " + mService.currentLatitude + "  " + mService.currentLatitude);
            			overlayMyLocation = new OverlayItem(new GeoPoint(mService.currentLatitude,mService.currentLongitude),
            														"User Location","",ItemizedOverlay.boundCenterBottom(getResources().getDrawable(R.drawable.ic_map_rx)));
            			array_img_balloons.addItem(overlayMyLocation);
            			array_img_balloons.requestRedraw();
            		}
            		else
            		{
            		//	System.out.println("DEBUG : adding user onto map  " + mService.currentLatitude + "  " + mService.currentLongitude);
            			overlayMyLocation.setPoint(new GeoPoint(mService.currentLatitude,mService.currentLongitude));
            			array_img_balloons.requestRedraw();
            		}
            	}
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

                	//item.setPoint(new GeoPoint(str.coords.latitude,str.coords.longitude));
                	//itemizedOverlay.requestRedraw();
            		
            		//TODO: check that our data is actually new
            		if (mService.payloadExists(str.callsign)){
            			if (str.time.getTime()>= mService.getMostRecent(str.callsign).time.getTime())
            				UpdateBalloonLocation(str.coords,str.callsign);
            			
            			UpdateBalloonTrack(mService.getPayloadData(str.callsign),str.callsign,true, false);//, 0, System.currentTimeMillis() / 1000L );
            		}
            		
                	Balloon_data_fragment fragment = (Balloon_data_fragment) getFragmentManager().findFragmentById(R.id.balloon_data_holder);
                	fragment.updatePayload(str,mService.getAscentRate(str.callsign));
            	}
            }
        }
    }
    
    
    private class LogEventReceiver extends BroadcastReceiver  {

    	@Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Dsp_service.LOG_EVENT)) {
            //Do stuff
            	if (intent.hasExtra(Dsp_service.LOG_STR))
            	{
            		String str =  intent.getStringExtra(Dsp_service.LOG_STR);

            		TextView tv = (TextView)findViewById(R.id.txtLogStatus);
            		tv.setText(str);
            		
            		timerLogging = new Timer();
            		timerLogging.schedule(new LoggingTimerTask(), 6 * 1000);
            	}
            }
        }
    }
    
    class LoggingTimerTask extends TimerTask {
    	
    	public void run() {
    		
    		 handler.post(new Runnable() {
                 @Override
                 public void run() {
                	 TextView tv = (TextView)findViewById(R.id.txtLogStatus);
                   tv.setText("");
                 }
             	});
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

       		if (requestUpdate)
       			updateAll();
   
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

	@Override
	public void onDialogPositiveClick(DialogFragment dialog, String callsign, int lookBehind) {
		// TODO Auto-generated method stub
    	Balloon_data_fragment fragment = (Balloon_data_fragment) getFragmentManager().findFragmentById(R.id.balloon_data_holder);
    	    	
    	fragment.AddPayload(callsign,getColour(callsign));
    	mService.addActivePayload(callsign,lookBehind);
    	mService.updateActivePayloadsHabitat();
	}
	
	public void removePayload(String callsign)
	{
		if (mService.payloadExists(callsign.toUpperCase()))
			mService.removeActivePayload(callsign.toUpperCase());
		System.out.println("REMOVED PAYLOAD: " + callsign);
	}
	


	@Override
	public void onDialogPositiveClick(DialogFragment dialog, boolean enPos, boolean enChase) {
		
		mService.changeLocationSettings(enPos,enChase);
		 /*
		if (!mService.enablePosition && enPos)
			mService.EnableLocation();
		
		if (mService.enablePosition && !enPos)
			mService.DisableLocation();
		
		mService.enablePosition = enPos;
		mService.enableChase = enChase;
		*/
	}

}
