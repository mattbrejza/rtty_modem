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

import rtty.StringRxEvent;
import rtty.rtty_receiver;
import ukhas.Telemetry_string;
import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Binder;
import android.os.IBinder;

public class Dsp_service extends Service implements StringRxEvent {

	 // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    rtty_receiver rcv = new rtty_receiver();
    private AudioRecord mRecorder;
	int buffsize;
	boolean isRecording = false;

	
	public Dsp_service() {
		// TODO Auto-generated constructor stub
		rcv.addStringRecievedListener(this);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		startAudio();
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
		sendBroadcast(new Intent(Intent.ACTION_VIEW));
	}
	

	
	public double[] getFFT()
	{
		return rcv.get_fft();
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
	                rcv.processBlock(s,300);
	               
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

}
