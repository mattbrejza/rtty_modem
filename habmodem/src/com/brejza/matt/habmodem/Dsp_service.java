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

import com.brejza.matt.habmodem.Map_Activity.LoggingTimerTask;

import rtty.StringRxEvent;
import rtty.moving_average;
import rtty.rtty_receiver;
import ukhas.Gps_coordinate;
import ukhas.HabitatRxEvent;
import ukhas.Habitat_interface;
import ukhas.Listener;
import ukhas.Payload;
import ukhas.Telemetry_string;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.TextView;

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
	
	Telemetry_string last_str;
	
	boolean _enableChase = false;
	boolean _enablePosition = false;

	Timer updateTimer;
	Timer serviceInactiveTimer;
	
	
	public double currentLatitude = 0;
	public double currentLongitude = 0;
	public boolean currentLocationValid = false;
	
	private Location_handler loc_han;
	private LocationManager locationManager;
	
	private long chasecarUpdateSecs = 45;
	private long lastChasecarUpdate = 0;
	
	moving_average ascent_rates;
	
	LoggingQueue log = new LoggingQueue(200);
	
	Habitat_interface hab_con;
	
	public List<String> listRxStr = Collections.synchronizedList(new ArrayList<String>()); 
	private ConcurrentHashMap<String, Payload> mapPayloads = new ConcurrentHashMap<String, Payload>();
	
	public Dsp_service() {
		rcv.addStringRecievedListener(this);		
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
	
	public class LocalBinder extends Binder {
		Dsp_service getService() {
            // Return this instance of LocalService so clients can call public methods
            return Dsp_service.this;
        }
    }
	
	public void changeLocationSettings(boolean enablePos, boolean enableChase)
	{
		 //TODO: work out why diabling one disables both
		if (!_enablePosition && !_enableChase && (enablePos  || enableChase))
			EnableLocation();
		
		if ((_enablePosition || _enableChase) && !enablePos && !enableChase)
			DisableLocation();
		
		_enablePosition = enablePos;
		_enableChase = enableChase;
		
	}
	
	//doesnt need to be called when service first starts (but can be)
	public void serviceRestart()
	{
		startAudio();
		updateActivePayloadsHabitat();
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
	
    private void EnableLocation()
    {
    	//my location part
        Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
        
        String bestProvider = this.locationManager.getBestProvider(criteria, true);
        if (bestProvider == null)
        	return;
        System.out.println("STARTING GPS WITH: "+bestProvider);
        this.locationManager.requestLocationUpdates(bestProvider, 1000, 0, this.loc_han);
        
    }
    
    private void DisableLocation()
    {
    	if (locationManager != null)
    	{
    		locationManager.removeUpdates(this.loc_han);
    	}
    }
	
	public void startAudio()
	{
		System.out.println("isRecording: " + isRecording);
		logEvent("Starting Audio. Already listening: " + isRecording,false);
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
	        ct.start();
		}
	}
	
//	public void stopAudio()
	//{
	//	isRecording = false;
	//}
	
	private void startUpdateTimer()
	{
		
		if (updateTimer == null){
			logEvent("Starting Update Timer",false);
			updateTimer = new Timer();
			int interval = 3 * 60 * 1000;
			updateTimer.scheduleAtFixedRate(new UpdateTimerTask(), interval,interval);
		}
	}
	private void startInactiveTimer()
	{
		
		if (serviceInactiveTimer == null)
			serviceInactiveTimer = new Timer();		
		else		
			serviceInactiveTimer.cancel();
		
		logEvent("Starting Inactivity Timer",false);
		
		int interval = 20 * 60 * 1000;
		serviceInactiveTimer.scheduleAtFixedRate(new InactiveTimerTask(), interval,interval);
		
	}
	
	class UpdateTimerTask extends TimerTask {
    	
    	public void run() {
	    	 updateActivePayloadsHabitat();
	    	 logEvent("Starting Habitat Refresh",true);
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
			Payload p = new Payload(call,true, lookBehind);
			mapPayloads.put(callu,p);
		}
		else {
			Payload p = mapPayloads.get(callu);
			p.setIsActivePayload(true);
			p.setMaxLookBehindDays(lookBehind);
		}
	}
	public void removeActivePayload(String call){  //TODO: remove data and other info, but not any IDs
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
			{
				if (hab_con.flight_configs.containsKey(entry.getKey().toUpperCase()))
					mapPayloads.put(call, new Payload(entry.getKey(),hab_con.payload_configs.get(call),hab_con.flight_configs.get(call)));
				else
					mapPayloads.put(call, new Payload(entry.getKey(),hab_con.payload_configs.get(call)));
				
			}
		}
		//	out.add(entry.getKey());		
		return mapPayloads;
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
            AudioManager manager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            boolean lastHead = false;
            
            logEvent("Starting Audio. Buffer Size: " + buffsize,true);
 
          //  setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
            
            buffsize =  mRecorder.read(buffer, 0, buffsize);  
        	mPlayer.write(buffer, 0, buffsize);
        	
            while(isRecording) 
            {
            	buffsize =  mRecorder.read(buffer, 0, buffsize);  
            	if (usingMic){	            	
	            	mPlayer.write(buffer, 0, buffsize);
	            	if (mPlayer.getPlayState() != AudioTrack.PLAYSTATE_PLAYING && lastHead==true)
	            		mPlayer.play();
	            	lastHead = true;
            	}
            	else
            		lastHead = false;
            	
            	if (buffsize >= 512)
            	{
	                s = new double [buffsize];
	                for (int i = 0; i < buffsize; i++)
	            	    s[i] = (double) buffer[i];
	                String rxchar =  rcv.processBlock(s,_baud);
	                Intent i = new Intent(CHAR_RX);
	                i.putExtra(CHARS, rxchar);
	                sendBroadcast(i);
	                
	                if (rcv.get_fft_updated())
	                	sendBroadcast(new Intent(FFT_UPDATED));
	                
	                
	               /*
	               handler.post(new Runnable() {
	                   @Override
	                   public void run() {
	                     t.setText(rcv.current_state.toString());
	                   }
	               	});
	               
	               System.out.print("                     ");
	               if (rcv.get_peaklocs() != null)
	               {
		               for (int i = 0; i < rcv.get_peaklocs().length; i++)
		            	   System.out.print("  " + rcv.get_peaklocs()[i]);
	               }
	               System.out.println();
	               
	               handler.post(new Runnable() {
	                   @Override
	                   public void run() {
	                	   showFFT();
	                   }
	               	});*/
            	}
            //   System.out.println("Got some samples");
             }

            mRecorder.stop();
            System.out.println("DONE RECORDING");
            logEvent("Stopping Audio",true);
            isRecording = false;
       }	
    	
    }

	public void updateActivePayloadsHabitat()
	{		
		int count=0;
		for (Map.Entry<String, Payload> entry : mapPayloads.entrySet())
		{
			if (entry.getValue().isActivePayload()){
				count++;
				long start = entry.getValue().getUpdateStart(false);      //TODO: if flight and enabled query flight DB here
				if ( start + 30 < (System.currentTimeMillis() / 1000L) )
					hab_con.addDataFetchTask(entry.getValue().callsign,start, (System.currentTimeMillis() / 1000L), entry.getValue().getMaxRecords());
			}
		}	
		if (count < 1){
			if (updateTimer != null){
				updateTimer.cancel();
				logEvent("Cancelling habitat update timer - no active payloads", false);
			}
		}
	}

	public void StringRx(Telemetry_string str, boolean checksum)
	{
		String call = str.callsign.toUpperCase();
		if (!checksum && !mapPayloads.containsKey(call))
			return;
		last_str = str;
		listRxStr.add(str.getSentence().trim());
		
		logEvent("Decoded String - " + str.getSentence().trim(),true);
		
		if (checksum){
			hab_con.upload_payload_telem(str);    //upload received string to server
			
			if (serviceInactiveTimer != null){
				startInactiveTimer();
			}
			
			if (mapPayloads.containsKey(call)){
				mapPayloads.get(call).setIsActivePayload(true);
				mapPayloads.get(call).putPacket(str);
				if ((System.currentTimeMillis() / 1000L) -60 < mapPayloads.get(call).getLastUpdated())
					mapPayloads.get(call).setLastUpdatedNow(); //if there are no (big) gaps since last string add current time as last update
			}
			else
			{		//first one, dont need to do anything special
				//TreeMap<Long,Telemetry_string> l = new TreeMap<Long,Telemetry_string>(); 
				//l.put(Long.valueOf(str.time.getTime()),str);
				//listPayloadData.put(str.callsign.toUpperCase(),l);
				mapPayloads.put(call,new Payload(str));
				startUpdateTimer();
				updateActivePayloadsHabitat();
			}
		}
		else if (str.getSentence().length() > 10)
			mapPayloads.put(call,new Payload(call,true));
		
		sendBroadcast(new Intent(TELEM_RX));
	}

	@Override
	public void HabitatRx(TreeMap<Long,Telemetry_string> data, boolean success, String callsign,
			long startTime, long endTime) {
		// TODO Auto-generated method stub
		String call = callsign.toUpperCase();
		
		if (success)
		{
			System.out.println("DEBUG: Got " + data.size() + " sentences for payload " + callsign);
			logEvent("Habitat Query Got " + data.size() + " Sentences For Payload " + callsign,true);
			
			if (mapPayloads.containsKey(call)){
				mapPayloads.get(call).setLastUpdated(endTime);
				mapPayloads.get(call).putPackets(data);
				mapPayloads.get(call).setIsActivePayload(true);
			}
			else
			{
				Payload p = new Payload(callsign,true);
				p.setLastUpdated(endTime);
				p.data = data;
				mapPayloads.put(call, p);
			}
			
			Intent i = new Intent(HABITAT_NEW_DATA);
			if (data.size() > 0)
				i.putExtra(TELEM_STR, data.get(data.lastKey()).getSentence());
			sendBroadcast(i);
		}
		else
		{
			logEvent("Habitat Query Failed",true);
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
    
	
	public class Location_handler implements LocationListener  {

		public Location_handler() {
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			
			
			
			if ((lastChasecarUpdate + chasecarUpdateSecs < System.currentTimeMillis() / 1000L) && _enableChase){
				String call_u = getFromSettingsCallsign();

				hab_con.updateChaseCar(new Listener(call_u, new Gps_coordinate(location.getLatitude(), location.getLongitude(),location.getAltitude()),true));
				lastChasecarUpdate = System.currentTimeMillis() / 1000L;
			}
			
			
			currentLatitude = location.getLatitude();
			currentLongitude = location.getLongitude();
			currentLocationValid = true;
			sendBroadcast(new Intent(GPS_UPDATED));
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
	
	

}
