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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import rtty.StringRxEvent;
import rtty.moving_average;
import rtty.rtty_receiver;
import ukhas.Gps_coordinate;
import ukhas.HabitatRxEvent;
import ukhas.Habitat_interface;
import ukhas.Listener;
import ukhas.Telemetry_string;
import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;

public class Dsp_service extends Service implements StringRxEvent, HabitatRxEvent {

	public final static String TELEM_RX = "com.brejza.matt.habmodem.TELEM_RX";
	public final static String CHAR_RX = "com.brejza.matt.habmodem.CHAR_RX";
	public final static String CHARS = "com.brejza.matt.habmodem.CHARS";
	public final static String FFT_UPDATED = "com.brejza.matt.habmodem.FFT_UPDATED";
	public final static String HABITAT_NEW_DATA = "com.brejza.matt.habmodem.HABITAT_NEW_DATA";
	public final static String TELEM_STR = "com.brejza.matt.habmodem.TELEM_STR";
	
	 // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    rtty_receiver rcv = new rtty_receiver();
    private AudioRecord mRecorder;
	int buffsize;
	boolean isRecording = false;
	
	Telemetry_string last_str;

	moving_average ascent_rates;
	
	Habitat_interface hab_con;
	
	public List<String> listRxStr = Collections.synchronizedList(new ArrayList<String>()); 
	public List<String> listActivePayloads = Collections.synchronizedList(new ArrayList<String>());
	public ConcurrentHashMap<String,Long> payloadLastUpdate = new ConcurrentHashMap<String,Long>();
	
	public Dsp_service() {
		rcv.addStringRecievedListener(this);
		
	}

	@Override
	public IBinder onBind(Intent arg0) {

		startAudio();
		hab_con = new Habitat_interface(
					PreferenceManager.getDefaultSharedPreferences(this).getString("pref_habitat_server", "habitat.habhub.org"),
					PreferenceManager.getDefaultSharedPreferences(this).getString("pref_habitat_db", "habitat"),
					 new Listener("MATT_XOOM", new Gps_coordinate(50.2,-0.6,0)));
		//hab_con.upload_payload_telem(new Telemetry_string("$$ASTRA,12:12:12,5044.11111,-001.00000,1212,34*1234"));	
		hab_con.addGetActiveFlightsTask();
		hab_con.addHabitatRecievedListener(this);
		System.out.println("Starting audio");
		return mBinder;
	}
	
	public class LocalBinder extends Binder {
		Dsp_service getService() {
            // Return this instance of LocalService so clients can call public methods
            return Dsp_service.this;
        }
    }
	
	

	
	public void startAudio()
	{
		System.out.println("isRecording: " + isRecording);
		if (!isRecording)
		{
			isRecording = true;
			
			buffsize = AudioRecord.getMinBufferSize(8000,AudioFormat.CHANNEL_IN_MONO ,AudioFormat.ENCODING_PCM_16BIT);
	    	buffsize = Math.max(buffsize, 10000);
	    	
	    	mRecorder = new AudioRecord(AudioSource.MIC,8000,
	    			AudioFormat.CHANNEL_IN_MONO ,
	    			AudioFormat.ENCODING_PCM_16BIT,buffsize);
	    	
	    	mRecorder.startRecording();
	    	System.out.println("STARTING THREAD");
	    	Thread ct = new captureThread();
	        ct.start();
		}
	}
	
//	public void stopAudio()
	//{
	//	isRecording = false;
	//}
	
	public void StringRx(Telemetry_string str, boolean checksum)
	{
		if (!checksum) return;
		last_str = str;
		listRxStr.add(str.getSentence());
		
		if (checksum)
			hab_con.upload_payload_telem(str);
		
		if (!listActivePayloads.contains(str.callsign))
		{
			listActivePayloads.add(str.callsign);
		}
		sendBroadcast(new Intent(TELEM_RX));
	}
	
	public Telemetry_string getLastString()
	{
		return last_str;
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
	
	public List<String> getFlightPayloadList()
	{
		List<String> out = new ArrayList<String>();
		
		Iterator<Entry<String, String>> it = hab_con.payload_configs.entrySet().iterator();
		while (it.hasNext()) {				
	        out.add(it.next().getKey());
	        it.remove();
	    }

		return out;
	}

	
	class captureThread extends Thread
    {
    	
    	public void run() 
    	{  

    		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

            short[] buffer = new short[buffsize];
            mRecorder.startRecording();
            isRecording = true;
 
          //  setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

            while(isRecording) 
            {
            	buffsize =  mRecorder.read(buffer, 0, buffsize);  
            	if (buffsize >= 512)
            	{
	                double[] s = new double [buffsize];
	                for (int i = 0; i < buffsize; i++)
	            	    s[i] = (double) buffer[i];
	                String rxchar =  rcv.processBlock(s,300);
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
            isRecording = false;
       }	
    	
    }

//TODO: avoid hardcoding stuff here
	public void updateActivePayloadsHabitat()
	{
		for (int i = 0; i < listActivePayloads.size(); i++)
		{
			String call = listActivePayloads.get(i);
			long start = (System.currentTimeMillis() / 1000L) - (7*24*60*60) ;//0;
			if (payloadLastUpdate.contains(call))
				start = payloadLastUpdate.get(call).longValue();
			
			if ( start + 60 < (System.currentTimeMillis() / 1000L) )
				hab_con.addDataFetchTask(listActivePayloads.get(i), start, (System.currentTimeMillis() / 1000L), 3000);
			
			
		}
	}


	@Override
	public void HabitatRx(List<String> data, boolean success, String callsign,
			long startTime, long endTime) {
		// TODO Auto-generated method stub
		
		
		if (success)
		{
			System.out.println("DEBUG: Got " + data.size() + "sentences for payload " + callsign);
			payloadLastUpdate.put(callsign,endTime);
			listRxStr.addAll(data);
			Intent i = new Intent(HABITAT_NEW_DATA);
			if (data.size() > 0)
				i.putExtra(TELEM_STR, data.get(data.size()-1));
			sendBroadcast(i);
			//TODO put latest string in the intent
		}
		
		
		
	}

}
