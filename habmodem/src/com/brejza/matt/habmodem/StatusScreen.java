package com.brejza.matt.habmodem;


import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import rtty.*;
import ukhas.*;




public class StatusScreen extends Activity {
	
	private AudioRecord mRecorder;
	int buffsize;
	boolean isRecording = false;
	rtty_receiver rcv = new rtty_receiver();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_screen);
        //rcv.addStringRecievedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_status_screen, menu);
        return true;
    }
    
    public void btnStart(View view)
    {
    	buffsize = AudioRecord.getMinBufferSize(8000,AudioFormat.CHANNEL_IN_MONO ,AudioFormat.ENCODING_PCM_16BIT);
    	buffsize = Math.max(buffsize, 2000);
    	
    	mRecorder = new AudioRecord(AudioSource.MIC,8000,
    			AudioFormat.CHANNEL_IN_MONO ,
    			AudioFormat.ENCODING_PCM_16BIT,buffsize);
    	
    	mRecorder.startRecording();
    	
    	Thread ct = new captureThread();
	      ct.start();
    }
    
    class captureThread extends Thread
    {
    	
    	public void run() {  

    		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

            short[] buffer = new short[buffsize];
            mRecorder.startRecording();
            isRecording = true;
 
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

            while(isRecording) {
               mRecorder.read(buffer, 0, buffsize);  
               double[] s = new double [buffsize];
               for (int i = 0; i < buffsize; i++)
            	   s[i] = (double) buffer[i];
            //   System.out.println(rcv.processBlock(s,300));
               System.out.println("Got some samples");
            }

            mRecorder.stop();
 
            isRecording = false;
       }	
    	
    }
}
