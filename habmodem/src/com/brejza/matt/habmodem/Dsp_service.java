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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import rtty.StringRxEvent;
import rtty.rtty_receiver;
import ukhas.AscentRate;
import ukhas.Gps_coordinate;
import ukhas.HabitatRxEvent;
import ukhas.Habitat_interface;
import ukhas.Listener;
import ukhas.Payload;
import ukhas.TelemetryConfig;
import ukhas.Telemetry_string;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder.AudioSource;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class Dsp_service extends Service implements StringRxEvent, HabitatRxEvent {

	public final static String TELEM_RX = "com.brejza.matt.habmodem.TELEM_RX";
	public final static String CHAR_RX = "com.brejza.matt.habmodem.CHAR_RX";
	public final static String CHARS = "com.brejza.matt.habmodem.CHARS";
	public final static String FFT_UPDATED = "com.brejza.matt.habmodem.FFT_UPDATED";
	public final static String HABITAT_NEW_DATA = "com.brejza.matt.habmodem.HABITAT_NEW_DATA";
	public final static String TELEM_STR = "com.brejza.matt.habmodem.TELEM_STR";
	public final static String GPS_UPDATED = "com.brejza.matt.habmodem.GPS_UPDATED";
	public final static String LOG_EVENT = "com.brejza.matt.habmodem.LOG_EVENT";
	public final static String LOG_STR = "com.brejza.matt.habmodem.LOG_STR";
	
	 // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    rtty_receiver rcv = new rtty_receiver();
    private AudioRecord mRecorder;
    private AudioTrack mPlayer;
	int buffsize;
	boolean isRecording = false;
	boolean usingMic = false;
	HeadsetReceiver headsetReceiver;
	
	private int _baud = 300;
	
	private int last_colour = 0;
	
	Telemetry_string last_str;
	
	boolean _enableChase = false;
	boolean _enablePosition = false;
	
	public boolean enableEcho = false;
	public boolean enableBell = false;
	public boolean enableUploader = true;
	private boolean _enableDecoder = false;

	Timer updateTimer;
	Timer serviceInactiveTimer;
	private int lastHabitatFreq=0;
	
	NotificationManager nm;
	
	Handler handler;
	
	Toast toast;
		
	public double currentLatitude = 0;
	public double currentLongitude = 0;
	public boolean currentLocationValid = false;
	
	private Location_handler loc_han;
	private LocationManager locationManager;
	
	//private long chasecarUpdateSecs = 45;
	private long lastChasecarUpdate = 0;
	
	//moving_average ascent_rates;
	
	LoggingQueue log = new LoggingQueue(200);
	
	Habitat_interface hab_con;
	
	public List<String> listRxStr = Collections.synchronizedList(new ArrayList<String>()); 
	private ConcurrentHashMap<String, Payload> mapPayloads = new ConcurrentHashMap<String, Payload>();
	
	public Dsp_service() {
		rcv.addStringRecievedListener(this);		
	}
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		System.out.println("Service started");
	}

	@Override
	public IBinder onBind(Intent arg0) {
	
		
		if (!isRecording)
			serviceRestart();	
		if (serviceInactiveTimer != null){
			serviceInactiveTimer.cancel();
			serviceInactiveTimer = null;
			logEvent("Stopping Inactivity Timer",false);			
		}
		
		System.out.println("DEBUG : something bound");
		
		handler = new Handler();
		
		
		  //string receiver
   		if (headsetReceiver == null) headsetReceiver = new HeadsetReceiver();
   		IntentFilter intentFilter1 = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
   		registerReceiver(headsetReceiver, intentFilter1);
		
		if (hab_con == null){
			String call_u = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("pref_callsign", "USER");
			
			hab_con = new Habitat_interface(
						PreferenceManager.getDefaultSharedPreferences(this).getString("pref_habitat_server", "habitat.habhub.org"),
						PreferenceManager.getDefaultSharedPreferences(this).getString("pref_habitat_db", "habitat"),
						 new Listener(call_u, new Gps_coordinate(50.2,-0.6,0),false));
			//hab_con.upload_payload_telem(new Telemetry_string("$$ASTRA,12:12:12,5044.11111,-001.00000,1212,34*1234"));	
			hab_con.addGetActiveFlightsTask();
			hab_con.addHabitatRecievedListener(this);
		}
		if (loc_han == null){
			loc_han = new Location_handler();
			this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		}
		
		hab_con.device = android.os.Build.BRAND + " " + android.os.Build.MODEL;
		hab_con.device_software = android.os.Build.VERSION.RELEASE;
		hab_con.application = "HAB Modem for Android";
		String vers = "unknown";
		try{
			Context cn = getApplicationContext();
			vers = cn.getPackageManager().getPackageInfo(cn.getPackageName(), 0).versionName;
		}
		catch (NameNotFoundException e){
			System.out.println("Cannot get version number - " + e.toString());
		}
		hab_con.application_version = vers;

		//System.out.println("Starting audio");
		return mBinder;
	}
	
	@Override
	public void onRebind(Intent intent)
	{
	
		if (!isRecording)
			serviceRestart();	
		if (serviceInactiveTimer != null){
			serviceInactiveTimer.cancel();
			serviceInactiveTimer = null;
			logEvent("Stopping Inactivity Timer",false);			
		}
		System.out.println("REBOUND");
	}
	
	@Override
	public boolean onUnbind(Intent intent)
	{
		
		System.out.println("DEBUG : something unbound");
		
		startInactiveTimer();		
		
		return true;
	}
	
	@Override
	public void onDestroy()
	{
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    	nm.cancel(0);
    	System.out.println("Destroying service");
    	super.onDestroy(); 
	}
	
	public class LocalBinder extends Binder {
		Dsp_service getService() {
            // Return this instance of LocalService so clients can call public methods
            return Dsp_service.this;
        }
    }
	
	public void changeLocationSettings(boolean enablePos, boolean enableChase)
	{
		if (!_enablePosition && !_enableChase && (enablePos  || enableChase))
			enableLocation();
		
		if ((_enablePosition || _enableChase) && !enablePos && !enableChase)
			disableLocation();
		
		_enablePosition = enablePos;
		_enableChase = enableChase;
		
	
		//Intent intent = new Intent(this, Map_Activity.class);
		//PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
		
		//icon at the bottom
        if (_enableChase)
        {
        	
        }
        else
        {
        	nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        	nm.cancel(0);
        }
		
	}
	
	//doesnt need to be called when service first starts (but can be)
	public void serviceRestart()
	{
		startAudio();
		if (countActivePayloads() > 0){
			startUpdateTimer();
			updateActivePayloadsHabitat();
		}
		logEvent("Service Restarted",false);
	}
	
	public void servicePause()
	{
		isRecording = false;
		if (updateTimer != null)
			updateTimer.cancel();
		logEvent("Service Paused",false);
	}
	
	public int countActivePayloads()
	{
		int count=0;
		for (Map.Entry<String, Payload> entry : mapPayloads.entrySet())
		{
			if (entry.getValue().isActivePayload())
				count++;
		}	
		return count;
	}
	
    private void enableLocation()
    {
    	//my location part
        Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
        
        String bestProvider = this.locationManager.getBestProvider(criteria, true);
        if (bestProvider == null)
        	return;
        System.out.println("STARTING GPS WITH: "+bestProvider);
        logEvent("Starting Location with: " + bestProvider,true);
        this.locationManager.requestLocationUpdates(bestProvider, 2000, 0, this.loc_han);
        
        
        
    }
    
    private void disableLocation()
    {
    	if (locationManager != null)
    	{
    		locationManager.removeUpdates(this.loc_han);
    		System.out.println("Disabling location");
    	}
    }
	
	public void startAudio()
	{
		if (!_enableDecoder)
			return;
		
		boolean mic = this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
		
		System.out.println("isRecording: " + isRecording);
		logEvent("Starting Audio. Mic avaliable: " + mic,false);
		if (!isRecording)
		{
			isRecording = true;
			
			buffsize = AudioRecord.getMinBufferSize(8000,AudioFormat.CHANNEL_IN_MONO ,AudioFormat.ENCODING_PCM_16BIT);
	    	buffsize = Math.max(buffsize, 3000);
	    	
	    	mRecorder = new AudioRecord(AudioSource.MIC,8000,
	    			AudioFormat.CHANNEL_IN_MONO ,
	    			AudioFormat.ENCODING_PCM_16BIT,buffsize);
	    	
	    	mPlayer = new AudioTrack(AudioManager.STREAM_MUSIC,8000,AudioFormat.CHANNEL_OUT_MONO,
	    			AudioFormat.ENCODING_PCM_16BIT,2*buffsize,AudioTrack.MODE_STREAM);
	    	
	    	AudioManager manager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
	    	manager.setMode(AudioManager.MODE_IN_CALL);
	    	manager.setSpeakerphoneOn(true);
	    	
	    	mRecorder.startRecording();
	    	System.out.println("STARTING THREAD");
	    	Thread ct = new captureThread();
	    	logEvent("Starting Audio Thread.",false);
	    	setDecoderRunningNotification();
	        ct.start();
		}
	}
	
//	public void stopAudio()
	//{
	//	isRecording = false;
	//}
	
	private void updateTimerPeriod()
	{
		int interval = 3 * 60 * 1000;
		
		String inter = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("pref_habitat_update_freq", "3");
		try
		{
			interval = Integer.parseInt(inter) * 60 * 1000;
			if (interval != lastHabitatFreq && updateTimer != null){
				updateTimer.cancel();
				updateTimer.purge();
				updateTimer = null;
				startUpdateTimer();
			}				
		}
		catch (Exception e)
		{

		}
	}
	
	private void startUpdateTimer()
	{
		
		if (updateTimer == null){
			logEvent("Starting Update Timer",false);
			updateTimer = new Timer();
			int interval = 3 * 60 * 1000;
			
			String inter = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("pref_habitat_update_freq", "3");
			try
			{
				interval = Integer.parseInt(inter) * 60 * 1000;
			}
			catch (Exception e)
			{
				interval = 3 * 60 * 1000;
			}
			
			if (interval < 30*1000)
				interval = 30*1000;
			lastHabitatFreq = interval;
			updateTimer.scheduleAtFixedRate(new UpdateTimerTask(), interval,interval);
		}
	}
	private void startInactiveTimer()
	{
		
		if (serviceInactiveTimer != null)					
			serviceInactiveTimer.cancel();
		
		serviceInactiveTimer = new Timer();	
		
		
		int interval = 20 * 60 * 1000;
		
		String sin = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("pref_inactive", "20");
		try
		{
			interval = Integer.parseInt(sin) * 60 * 1000;
		}
		catch (Exception e)
		{
			interval = 20 * 60 * 1000;
		}		
		if (interval < 30*1000)
		{
			interval = 20 * 60 * 1000;
		}

		logEvent("Starting Inactivity Timer for " + interval/(60*1000) + " Minutes",false);
		serviceInactiveTimer.schedule(new InactiveTimerTask(), interval);
		
	}
	
	class UpdateTimerTask extends TimerTask {
    	
    	public void run() {
	    	 updateActivePayloadsHabitat();
	    	 logEvent("Starting Habitat Refresh",true);
	    	 updateTimerPeriod();	    	 
    	}
    }
	
	class InactiveTimerTask extends TimerTask {
    	
    	public void run() {
	    	 servicePause();

    	}
    }
	
	public Telemetry_string getLastString()
	{
		return last_str;
	}
	
	public boolean payloadExists(String callsign) {
		return (mapPayloads.containsKey(callsign.toUpperCase()));
	}
	public boolean activePayloadExists(String callsign) {
		if (mapPayloads.containsKey(callsign.toUpperCase())){
			return mapPayloads.get(callsign.toUpperCase()).isActivePayload();
		}
		else
			return false;
	}
	public TreeMap<Long, Telemetry_string> getPayloadData (String callsign){
		if (payloadExists(callsign))
			return mapPayloads.get(callsign.toUpperCase()).data;
		else
			return null;
	}
	public double getAscentRate(String callsign) {
		if (payloadExists(callsign))
			return mapPayloads.get(callsign.toUpperCase()).getAscentRate();
		else
			return 0;
	}
	public double getMaxAltitude(String callsign){
		if (payloadExists(callsign))
			return mapPayloads.get(callsign.toUpperCase()).getMaxAltitude();
		else
			return 0;
	}
	//public long getLastUpsddate(String callsign){
	//	if (payloadExists(callsign))
	//		return mapPayloads.get(callsign.toUpperCase()).getLastUpdated();
	//	else
	//		return 0;
	//}  //TODO: if already there, update infos
/*	public void addActivePayload(String call){
		String callu = call.toUpperCase();
		if (!payloadExists(callu))
			mapPayloads.put(callu,new Payload(call,true, 3));
		else {
			Payload p = mapPayloads.get(callu);
			p.setIsActivePayload(true);
		}
	} */
	public void addActivePayload(String call, int lookBehind){
		startUpdateTimer();
		String callu = call.toUpperCase();
		if (!payloadExists(callu)) {
			
			Payload p = new Payload(call,newColour(),true, lookBehind);
			mapPayloads.put(callu,p);
		}
		else {
			Payload p = mapPayloads.get(callu);
			p.setIsActivePayload(true);
			if (p.colour == 0)
				p.setNewColour(newColour());
			
			p.setMaxLookBehindSecs(lookBehind);
		}
	}
	public void removeActivePayload(String call){ 
		mapPayloads.get(call.toUpperCase()).clearUserData();
	}
	public double[] getFFT()
	{
		return rcv.get_fft();
	}
	
	public double getFFT(int i)
	{
		return rcv.get_fft(i);
	}
	
	public int get_f1_FFTbin()
	{
		return (int) (rcv.get_f1()*(rcv.FFT_half_len*2));
	}
	
	public int get_f2_FFTbin()
	{
		return (int) (rcv.get_f2()*(rcv.FFT_half_len*2));
	}
	
	public List<String> getActivePayloadList()
	{
		List<String> out = new ArrayList<String>();		
		for (Map.Entry<String, Payload> entry : mapPayloads.entrySet())
		{
			if (entry.getValue().isActivePayload())
				out.add(entry.getValue().callsign);
		}
		return out;
	}
	
	public ConcurrentHashMap<String,Payload> getPayloadList()
	{
		//check for any new payload data in habitat interface
			
		for (Map.Entry<String, String> entry : hab_con.payload_configs.entrySet())
		{
			String call = entry.getKey().toUpperCase();
			if (payloadExists(call))
			{   //update records
				mapPayloads.get(call).setPayloadID(entry.getValue());
				if (hab_con.flight_configs.containsKey(call))
					mapPayloads.get(call).setFlightID(hab_con.flight_configs.get(call));
			}
			else
			{	//these payloads are not active
				if (hab_con.flight_configs.containsKey(entry.getKey().toUpperCase()))
					mapPayloads.put(call, new Payload(entry.getKey(),hab_con.payload_configs.get(call),hab_con.flight_configs.get(call)));
				else
					mapPayloads.put(call, new Payload(entry.getKey(),hab_con.payload_configs.get(call)));
				
			}
		}
		//	out.add(entry.getKey());		
		return mapPayloads;
	}
	
	public TelemetryConfig getTelemetryConfig(String call)
	{
		if (payloadExists(call))
		{
			return mapPayloads.get(call).telemetryConfig;
		}
		else
			return null;
	}

	
	class captureThread extends Thread
    {
    	
    	public void run() 
    	{  

    		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

            short[] buffer = new short[buffsize];
            double[] s = new double[buffsize];
            mRecorder.startRecording();
            isRecording = true;
            //AudioManager manager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            boolean lastHead = false;
            int clippingCount = 0;
            int samplesSinceToast = 0;
            
            logEvent("Starting Audio. Buffer Size: " + buffsize,true);
 
          //  setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
            
            buffsize =  mRecorder.read(buffer, 0, buffsize);  
        	mPlayer.write(buffer, 0, buffsize);
        	
        	
        	
            while(isRecording && _enableDecoder) 
            {
            	buffsize =  mRecorder.read(buffer, 0, buffsize);  
            	if (usingMic && enableEcho){	            	
	            	mPlayer.write(buffer, 0, buffsize);
	            	if (mPlayer.getPlayState() != AudioTrack.PLAYSTATE_PLAYING && lastHead==true)
	            		mPlayer.play();
	            	lastHead = true;
            	}
            	else
            		lastHead = false;
            	
            	if (buffsize >= 512)
            	{
            		int i;
	                s = new double [buffsize];
	                for (i = 0; i < buffsize; i++)
	            	    s[i] = (double) buffer[i];
	                
	                i=0;
	                clippingCount = 0;
	                while (i < buffsize)
	                {
	                	if (buffer[i] > 30000 || buffer[i] < -30000)
	                		clippingCount++;
	                	
	                	i += 10;
	                }
	                
	                if (clippingCount > 10){
	                	if (samplesSinceToast <= 0 || samplesSinceToast > 8000*3)
	                	{
	                		samplesSinceToast = buffsize;
		                	System.out.println("Clipping detected");
		                	handler.post(new Runnable(){
		                		@Override
		                		public void run() {
		                			if (toast != null){
		                				toast.cancel();
		                				toast = null;
		                			}
		                			toast = Toast.makeText(getApplicationContext(), "Clipping Detected", Toast.LENGTH_SHORT);
		                			toast.show();
		                		}
		                	});
	                	}
	                }
	                samplesSinceToast += buffsize;
	                
	                String rxchar =  rcv.processBlock(s,_baud);
	                Intent it = new Intent(CHAR_RX);
	                it.putExtra(CHARS, rxchar);
	                sendBroadcast(it);
	                
	                if (rcv.get_fft_updated())
	                	sendBroadcast(new Intent(FFT_UPDATED));
	                
            	}

             }

            mRecorder.stop();
            System.out.println("DONE RECORDING");
            logEvent("Stopping Audio",true);
            isRecording = false;
            mRecorder = null;
            nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.cancel(1);
       }	
    	
    }
	
	public void enableDecoder()
	{
		_enableDecoder = true;
		startAudio();
	}

	public void disableDecoder()
	{
		_enableDecoder = false;
	}
	public boolean getDecoderRunning()
	{
		return _enableDecoder;
	}
	
	public void updateActivePayloadsHabitat()
	{		
		int count=0;
		int maxRec = 3000;
		String smr = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("pref_habitat_max_lines", "3000");
		try
		{
			maxRec = Integer.parseInt(smr);
		}
		catch (Exception e)
		{
			maxRec = 3000;
		}		
		if (maxRec < 10 || maxRec > 99999999)
		{
			maxRec = 3000;
		}
		
		for (Map.Entry<String, Payload> entry : mapPayloads.entrySet())
		{
			if (entry.getValue().isActivePayload()){
				count++;
				long start = entry.getValue().getUpdateStart(false); 
				if ( start + 15 < (System.currentTimeMillis() / 1000L) )
				{
					if (entry.getValue().getQueryOngoing() == 0 ||
							entry.getValue().getQueryOngoing() < System.currentTimeMillis()-(60*1000)){
						hab_con.addDataFetchTask(entry.getValue().callsign,start, (System.currentTimeMillis() / 1000L), maxRec);//entry.getValue().getMaxRecords());
						entry.getValue().setQueryOngoing(System.currentTimeMillis());
					}
				}
			}
		}	
		if (count < 1){
			if (updateTimer != null){
				updateTimer.cancel();
				logEvent("Cancelling habitat update timer - no active payloads", false);
			}
		}
	}

	public void StringRx(String str_, boolean checksum)
	{
		Telemetry_string str = new Telemetry_string(str_,null);
		TelemetryConfig tc = getTelemetryConfig(str.callsign);
		if (tc != null)
			str = new Telemetry_string(str_,tc);
		
		String call = str.callsign.toUpperCase();
		if (!checksum && !mapPayloads.containsKey(call))
			return;
		last_str = str;
		listRxStr.add(str.getSentence().trim());
		
		logEvent("Decoded String - " + str.getSentence().trim(),true);
		
		if (checksum){
			if (enableUploader)
				hab_con.upload_payload_telem(str);    //upload received string to server
			
			if (serviceInactiveTimer != null){
				startInactiveTimer();
			}
			
			if (mapPayloads.containsKey(call)){
				mapPayloads.get(call).setIsActivePayload(true);
				if (mapPayloads.get(call).colour == 0)
					mapPayloads.get(call).setNewColour(newColour());
				
				mapPayloads.get(call).putPacket(str);
				if ((System.currentTimeMillis() / 1000L) -60 < mapPayloads.get(call).getLastUpdated())
					mapPayloads.get(call).setLastUpdatedNow(); //if there are no (big) gaps since last string add current time as last update
			}
			else
			{		//first one, dont need to do anything special
				//TreeMap<Long,Telemetry_string> l = new TreeMap<Long,Telemetry_string>(); 
				//l.put(Long.valueOf(str.time.getTime()),str);
				//listPayloadData.put(str.callsign.toUpperCase(),l);
				mapPayloads.put(call,new Payload(str,newColour()));
				startUpdateTimer();
				updateActivePayloadsHabitat();
			}
			if (str.coords.alt_valid)
				mapPayloads.get(call).putMaxAltitude(str.coords.altitude);
		}
		else if (str.getSentence().length() > 10 && !payloadExists(str.callsign)){
			mapPayloads.put(call,new Payload(call,newColour(),true));
		}
		
		sendBroadcast(new Intent(TELEM_RX));
	}

	@Override
	public void HabitatRx(TreeMap<Long,Telemetry_string> data, boolean success, String callsign,
			long startTime, long endTime, AscentRate as, double maxAltitude) {
		
		mapPayloads.get(callsign.toUpperCase()).setQueryOngoing(0);
		if (success)
		{
			
			
			
			
			String call = callsign.toUpperCase();
			System.out.println("DEBUG: Got " + data.size() + " sentences for payload " + callsign);
			logEvent("Habitat Query Got " + data.size() + " Sentences For Payload " + callsign,true);
			
			if (mapPayloads.containsKey(call)){
				Payload p = mapPayloads.get(call);
				
				
				//if havnt already got a telem_config, see if one exists in hab_con
				if (p.telemetryConfig == null){
					if (hab_con.getTelemConfigs().containsKey(call)){
						p.telemetryConfig = hab_con.getTelemConfigs().get(call);
					}
				}
					
				
				long lt = p.getLastTime();
				
				p.setLastUpdated(endTime);
				p.putPackets(data);
				p.setIsActivePayload(true);
				if (p.colour == 0)
					p.setNewColour(newColour());
				
				if (data.size() > 0){
					if (lt < Long.valueOf(data.lastKey())){
						if (as != null){
							if (as.valid())
								p.ascentRate = as;
						}
					}
				}
			}
			else
			{
				Payload p = new Payload(callsign,newColour(),true);

				if (hab_con.getTelemConfigs().containsKey(call)){
					p.telemetryConfig = hab_con.getTelemConfigs().get(call);
				}

				p.setLastUpdated(endTime);
				p.data = data;
				mapPayloads.put(call, p);
				if (as != null){
					if (as.valid())
						mapPayloads.get(call).ascentRate = as;
				}
			}
			
			mapPayloads.get(call).putMaxAltitude(maxAltitude);
			
			Intent i = new Intent(HABITAT_NEW_DATA);
			if (data.size() > 0)
				i.putExtra(TELEM_STR, data.get(data.lastKey()).getSentence());
			sendBroadcast(i);
		}
		else
		{
			logEvent("Habitat Query Failed - " + callsign,true);
		}		
	}
	
	
	public void logEvent(String event,boolean broadcast)
	{		
		SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm:ss");//dd/MM/yyyy

	    String s = sdfDate.format(new Date()) + " - " + event;

		if (broadcast){
			Intent i = new Intent(LOG_EVENT);		
			i.putExtra(LOG_STR, log.offerAndReturn(s));
			sendBroadcast(i);	
		}
		else
			 log.offer(s);
	}
	
	public String getFromSettingsCallsign()
	{
		return PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("pref_callsign", "USER");
		
	}
	
    private class HeadsetReceiver extends BroadcastReceiver  {

    	@Override
        public void onReceive(Context context, Intent intent) {
    		usingMic = false;
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
            //Do stuff
            	if (intent.hasExtra("microphone"))
            	{            		
            		if (intent.getIntExtra("microphone", 0) == 1){
            			logEvent("Using Line In",true);
            			usingMic = true;
            			return;
            		}
            		else{
            			logEvent("Not Using Line In",true);
            			usingMic = false;
            		}
            	}
            	else
            	{
            		logEvent("Headset: Not Using Line In",true);
            		usingMic = false;
            	}
            }
        }
    }
    
    public void setChaseCarNotification()    
    {
    	String body = "Uploading Chase Car Positions";
    	String title = "Chase Car";
    	NotificationCompat.Builder mBuilder =
    	        new NotificationCompat.Builder(this)
    	        .setSmallIcon(R.drawable.ic_stat_car)
    	        .setContentTitle(title)
    	        .setContentText(body);
    	// Creates an explicit intent for an Activity in your app
   /* 	Intent resultIntent = new Intent(this, Map_Activity.class);

    	
    	TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
    	// Adds the back stack for the Intent (but not the Intent itself)
    	stackBuilder.addParentStack(Map_Activity.class);
    	// Adds the Intent that starts the Activity to the top of the stack
    	stackBuilder.addNextIntent(resultIntent);
    	PendingIntent resultPendingIntent =
    	        stackBuilder.getPendingIntent(
    	            0,
    	            PendingIntent.FLAG_UPDATE_CURRENT
    	        );
    	mBuilder.setContentIntent(resultPendingIntent); */
    	NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    	// mId allows you to update the notification later on.
    	mNotificationManager.notify(0, mBuilder.build());
	
    }
    
    public void setDecoderRunningNotification()    
    {
    	String body = "RTTY decoder is processing audio";
    	String title = "Decoder Running";
    	NotificationCompat.Builder mBuilder =
    	        new NotificationCompat.Builder(this)
    	        .setSmallIcon(R.drawable.ic_stat_decoderrunning)
    	        .setContentTitle(title)
    	        .setContentText(body);
    	
    	NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    	// mId allows you to update the notification later on.
    	mNotificationManager.notify(1, mBuilder.build());
	
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
	
    private int getChaseCarUpdatePeriod()
    {
    	int t = 45;
		String st = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("pref_chase_update_freq", "45");
		
		try
		{
			t=Integer.parseInt(st);			
		}
		catch (Exception e)
		{
			t = 45;
		}
		if (t < 10 || t > 60*60*24)
			t = 45;
		
		return t;
    }
    
	public class Location_handler implements LocationListener  {

		public Location_handler() {
		}

		@Override
		public void onLocationChanged(Location location) {
			
			int chasecarUpdateSecs = getChaseCarUpdatePeriod();
			
			if ((lastChasecarUpdate + chasecarUpdateSecs < System.currentTimeMillis() / 1000L) && _enableChase){
				String call_u = getFromSettingsCallsign();
				float speed = location.getSpeed();
				hab_con.updateChaseCar(new Listener(call_u, new Gps_coordinate(location.getLatitude(), location.getLongitude(),location.getAltitude()),speed,true));
				lastChasecarUpdate = System.currentTimeMillis() / 1000L;
				
				setChaseCarNotification();
			}
			
			
			currentLatitude = location.getLatitude();
			currentLongitude = location.getLongitude();
			currentLocationValid = true;
			sendBroadcast(new Intent(GPS_UPDATED));
		}
		
		@Override
		public void onProviderDisabled(String provider) {
			nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	    	nm.cancel(0);
		}

		@Override
		public void onProviderEnabled(String provider) {

			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

	}
	
	public boolean getEnableChase(){
		return _enableChase;
	}
	
	public boolean getEnablePosition(){
		return _enablePosition;
	}
	
	public Telemetry_string getMostRecent(String callsign)
	{
		if (mapPayloads.containsKey(callsign.toUpperCase()))
		{
			return mapPayloads.get(callsign.toUpperCase()).getLastString();
		}
		else
			return null;
	}
	public LoggingQueue getLog(){
		return log;
	}
	public TreeMap<Long,Telemetry_string> getAllData(String callsign)
	{
		if (mapPayloads.containsKey(callsign.toUpperCase()))
		{
			return mapPayloads.get(callsign.toUpperCase()).data;
		}
		else
			return null;
	}
	public void setBaud(int baud)
	{
		_baud = baud;
	}
	public int getBaud(){
		return _baud;
	}
	public int getPayloadColour(String call)
	{
		call = call.toUpperCase();
		if (payloadExists(call))
		{
			int i = mapPayloads.get(call).colour;
			if (i == 0)
				i = 0xFF000000;
			return i;
		}
		return 0;
	}
	
	

}
