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


import com.brejza.matt.habmodem.Dsp_service.LocalBinder;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;


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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import rtty.*;
import ukhas.*;




public class StatusScreen extends Activity  {
	
//	private AudioRecord mRecorder;
//	int buffsize;
//	boolean isRecording = false;
//	private Handler handler;
//	rtty_receiver rcv = new rtty_receiver();
	TextView t;
//	private GraphView graphView;
	private GraphViewSeries viewseries;
	
	private int FFT_half_len = 512;

	private StringRxReceiver strrxReceiver;
	
	Dsp_service mService;
    boolean mBound = false;
    boolean isReg = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_screen);
      //  rcv.addStringRecievedListener(this);
      //  handler = new Handler();
        t = (TextView)findViewById(R.id.txttest);
        
        initGraph();

    }

    /////////////////////
    //////// MENU ///////
    /////////////////////
    
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
            case R.id.fft_screen:
            	intent = new Intent(this, FFTActivity.class);
            	startActivity(intent);
                return true;
            case R.id.map_screen:
            	intent = new Intent(this, MapActivity.class);
            	startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
	/////////////////////
	////// BUTTONS //////
	/////////////////////

    public void btnStartService(View view)
	{
		 if (mBound) {
			// mService.addStringRecievedListener(this);
	            // Call a method from the LocalService.
	            // However, if this call were something that might hang, then this request should
	            // occur in a separate thread to avoid slowing down the activity performance.
	            Toast.makeText(this, "number: " + 7, Toast.LENGTH_SHORT).show();
	        }
	}
    
    /*
	public void btnUpdate(View view)
	{
		handler.post(new Runnable() {
            @Override
            public void run() {
             
            
		exampleSeries1.resetData(new GraphViewData[] {
				new GraphViewData(1, getRandom())
				, new GraphViewData(2, getRandom())
				, new GraphViewData(2.5, getRandom()) // another frequency
				, new GraphViewData(3, getRandom())
				, new GraphViewData(4, getRandom())
				, new GraphViewData(5, getRandom())
		});
            }
    	});
	}
	*/
    

    public void btnStart(View view)
    {
    	/*
    	buffsize = AudioRecord.getMinBufferSize(8000,AudioFormat.CHANNEL_IN_MONO ,AudioFormat.ENCODING_PCM_16BIT);
    	buffsize = Math.max(buffsize, 10000);
    	
    	mRecorder = new AudioRecord(AudioSource.MIC,8000,
    			AudioFormat.CHANNEL_IN_MONO ,
    			AudioFormat.ENCODING_PCM_16BIT,buffsize);
    	
    	mRecorder.startRecording();
    	
    	Thread ct = new captureThread();
        ct.start();
      */
      
        //test 
       
    }
    
	/////////////////////
	///// LIFECYCLE /////
	/////////////////////
    
    @Override
    protected void onStart() {
        super.onStart();
     //   // Bind to LocalService
     //   Intent intent = new Intent(this, Dsp_service.class);
     //   bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
       
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
      
    }
    
    @Override
   	public void onResume() {
   		super.onResume();
   		//if (strrxReceiver == null) strrxReceiver = new StringRxReceiver();
   		//IntentFilter intentFilter = new IntentFilter(Intent.ACTION_VIEW);
   		//if (!isReg) { registerReceiver(strrxReceiver, intentFilter); }
   		isReg = true;
   		
   	 // Bind to LocalService
        Intent intent = new Intent(this, Dsp_service.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
   	}
       
    @Override
    public void onPause(){
       	super.onPause();
       	//if (isReg)
        	//   if (strrxReceiver != null) unregisterReceiver(strrxReceiver);
           
           isReg = false;
           
       if (mBound) {
           unbindService(mConnection);
           mBound = false;
       }
    }
    
    ///////////////
	
	private void initGraph()
	{
		/*
	     // draw sin curve  
        int num = 150;  
        GraphViewData[] data = new GraphViewData[num];  
        double v=0;  
        for (int i=0; i<num; i++) {  
           v += 0.2;  
           data[i] = new GraphViewData(i, Math.sin(v));  
        }  
        GraphView graphView = new LineGraphView(  
              this  
              , "GraphViewDemo"  
        );  
        // add data  
        viewseries = new GraphViewSeries(data);
        graphView.addSeries(viewseries);  
  
       // graphView.setScrollable(false);  
        // optional - activate scaling / zooming  
        graphView.setScalable(true);  
          
        LinearLayout layout = (LinearLayout) findViewById(R.id.llgraph);  
        
        layout.addView(graphView);
        */
         /*
		
		exampleSeries1 = new GraphViewSeries(new GraphViewData[] {
				new GraphViewData(1, 2.0d)
				, new GraphViewData(2, 1.5d)
				, new GraphViewData(2.5, 3.0d) // another frequency
				, new GraphViewData(3, 2.5d)
				, new GraphViewData(4, 1.0d)
				, new GraphViewData(5, 3.0d)
		});

		// graph with dynamically genereated horizontal and vertical labels
	
			GraphView graphView = new LineGraphView(
					this // context
					, "GraphViewDemo" // heading
			);
		
		graphView.addSeries(exampleSeries1); // data

		LinearLayout layout = (LinearLayout) findViewById(R.id.graph1);
		layout.addView(graphView);
        */
	}
	
	private void showFFT()
	{
		 GraphViewData[] data = new GraphViewData[FFT_half_len];
		 
		 for (int i = 0; i < FFT_half_len; i++)
		 {
			 data[i] = new GraphViewData(i,10 * Math.log10(mService.rcv.get_fft(i)));
		 }
		 
		 viewseries.resetData(data); 
	
		 LinearLayout layout = (LinearLayout) findViewById(R.id.llgraph);
		 
		 layout.removeAllViewsInLayout();
		 
		 GraphView graphView = new LineGraphView(  
	              this  
	              , "GraphViewDemo"  
	        ); 
		 
		 graphView.addSeries(new GraphViewSeries(data));

		 layout.addView(graphView);
	     
		
	     
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
    
   
    
    private class StringRxReceiver extends BroadcastReceiver {

    	
    	@Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_VIEW)) {
            //Do stuff
            	System.out.println("GOT INTENTTTTTTTTTTTTTTTTT");
            	showFFT();
            }
        }
        
    
        
    }
    
}
