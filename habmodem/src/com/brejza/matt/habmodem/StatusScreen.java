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

import ukhas.Telemetry_string;

import com.brejza.matt.habmodem.Dsp_service;
import com.brejza.matt.habmodem.Dsp_service.LocalBinder;


import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;




public class StatusScreen extends Activity  {
	

	
//	private AudioRecord mRecorder;
//	int buffsize;
//	boolean isRecording = false;
//	private Handler handler;
//	rtty_receiver rcv = new rtty_receiver();
//	TextView t;
	ListView list;
	EditText txtchars;
	ImageView wfview;

//	private GraphView graphView;
//	private GraphViewSeries viewseries;


	private StringRxReceiver strrxReceiver;
	private CharRxReceiver charrxReceiver;
	private FFTUpdateReceiver fftupdateReceiver;
	
	Dsp_service mService;
    boolean mBound = false;
    boolean isReg = false;
    boolean initList = false;
    
    ArrayAdapter<String> adapter;
    
    Waterfall wf;

  

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
    	/*
        switch (item.getItemId()) {
            case R.id.fft_screen:
            //	intent = new Intent(this, FFTActivity.class);
            //	startActivity(intent);
                return true;
            case R.id.map_screen:
            	finish();
                return true;
            case R.id.settings_screen:
            	intent = new Intent(this,Preferences_activity.class);
            	startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        } */
        if (item.getItemId() == R.id.status_screen)
        	return true;
        else if (item.getItemId() == R.id.map_screen) {
        	finish();
            return true; }
        else if (item.getItemId() ==  R.id.settings_screen) {
        	intent = new Intent(this,Preferences_activity.class);
        	startActivity(intent); 
            return true;}
       
       return super.onOptionsItemSelected(item); 
    }
    
	/////////////////////
	////// BUTTONS //////
	/////////////////////

   
    

    
	/////////////////////
	///// LIFECYCLE /////
	/////////////////////
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_screen);
        
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        
        
        
        
      //  rcv.addStringRecievedListener(this);
      //  handler = new Handler();
       // t = (TextView)findViewById(R.id.txttest);
        wfview = (ImageView)findViewById(R.id.imgViWF);
        list = (ListView)findViewById(R.id.listRxStrings);
        txtchars = (EditText)findViewById(R.id.txtRawChars);
        txtchars.setEnabled(false);
        initGraph();
        
        
        wf = new Waterfall(BitmapFactory.decodeResource(this.getResources(), R.drawable.grad), 200);
        
        
       

    }
    
    @Override
    protected void onStart() {
        super.onStart();
     //   // Bind to LocalService
       // Intent intent = new Intent(this, Dsp_service.class);
    
       // startService(intent);
        
      	 // Bind to LocalService
        Intent intent = new Intent(this, Dsp_service.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mBound = true;
      
       
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

   		
   	 // Bind to LocalService
   		if (!mBound){
	        Intent intent = new Intent(this, Dsp_service.class);
	        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
   		}
        
     
      //string receiver
   		if (strrxReceiver == null) strrxReceiver = new StringRxReceiver();
   		IntentFilter intentFilter1 = new IntentFilter(Dsp_service.TELEM_RX);
   		if (!isReg) { registerReceiver(strrxReceiver, intentFilter1); }
   		
   	//char receiver
   		if (charrxReceiver == null) charrxReceiver = new CharRxReceiver();
   		IntentFilter intentFilter2 = new IntentFilter(Dsp_service.CHAR_RX);
   		if (!isReg) { registerReceiver(charrxReceiver, intentFilter2); }
   		
   	//fftreceiver
   		if (fftupdateReceiver == null) fftupdateReceiver = new FFTUpdateReceiver();
   		IntentFilter intentFilter3 = new IntentFilter(Dsp_service.FFT_UPDATED);
   		if (!isReg) { registerReceiver(fftupdateReceiver, intentFilter3); }
   		
   		isReg = true;
   		
   		boolean mic = this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
        if (mic)
        	Toast.makeText(this, "MIC AVALIABLE", Toast.LENGTH_LONG).show();
        else
        	Toast.makeText(this, "MIC NOT AVALIABLE :o", Toast.LENGTH_LONG).show();
   	}
    
   
       
    @Override
    public void onPause(){
       	super.onPause();
       	if (isReg)
       	{
        	   if (strrxReceiver != null) unregisterReceiver(strrxReceiver);
        	   if (charrxReceiver != null) unregisterReceiver(charrxReceiver);
        	   if (fftupdateReceiver != null) unregisterReceiver(fftupdateReceiver);
       	}
           
        isReg = false;
           
        mService.rcv.enableFFT = false;
           
     //  if (mBound) {
     //      unbindService(mConnection);
     //      mBound = false;
     //  }
    }
    
    ///////////////
	
    public void updateListView()
    {
    	if (!initList)
    	{
	    	 list = (ListView) findViewById(R.id.listRxStrings);
	     
	
	         // First paramenter - Context
	         // Second parameter - Layout for the row
	         // Third parameter - ID of the TextView to which the data is written
	         // Forth - the Array of data
	         adapter = new ArrayAdapter<String>(this,
	           android.R.layout.simple_list_item_1, android.R.id.text1, mService.listRxStr);
	
	         // Assign adapter to ListView
	         list.setAdapter(adapter);
	         list.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
	         initList = true;
    	}
    	else
    	{
    		
    		adapter.notifyDataSetChanged();
    	}
    }
    
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
	
	/*
	
	private void showFFT()
	{
		 GraphViewData[] data = new GraphViewData[FFT_half_len];
		 
		 for (int i = 0; i < FFT_half_len; i++)
		 {
			 data[i] = new GraphViewData(i,10 * Math.log10(mService.getFFT(i)));
		 }
		 
			
		 LinearLayout layout = (LinearLayout) findViewById(R.id.llgraph);
		 
		 layout.removeAllViewsInLayout();
		 
		 GraphView graphView = new LineGraphView(  
	              this  
	              , "GraphViewDemo"  
	        ); 
		 
		 graphView.addSeries(new GraphViewSeries(data));

		 layout.addView(graphView);
	     
		
	     
	}
     
    */
    
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            mService.rcv.enableFFT = true;
            setBaudButton();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    
   
    
    private class StringRxReceiver extends BroadcastReceiver {

    	@Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Dsp_service.TELEM_RX)) {
            //Do stuff
            	System.out.println("GOT INTENT telem");
            //	list.add
            	updateListView();
            	
            }
        }
    }
    
    private class CharRxReceiver extends BroadcastReceiver {

    	@Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Dsp_service.CHAR_RX)) {
            //Do stuff
            	String ch = intent.getStringExtra(Dsp_service.CHARS);
            	System.out.println(ch);
            	txtchars.append(ch);
            }
        }
    }
    
    private class FFTUpdateReceiver extends BroadcastReceiver {

    	@Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Dsp_service.FFT_UPDATED)) {
            	//TODO: HERE
            	wfview.setImageBitmap(wf.UpdateLine(mService.getFFT(),mService.get_f1_FFTbin(),mService.get_f2_FFTbin()));
            	wfview.invalidate();
            	
            }
        }
    }
    
    public void setBaud(View view)
    {
    	int currentBaud = mService.getBaud();
    	
    	if (currentBaud == 300)
    		mService.setBaud(50);
    	else
    		mService.setBaud(300);
    	
    	setBaudButton();
    }
    
    private void setBaudButton()
    {
    	Button btn = (Button) findViewById(R.id.btnBaud);
    	btn.setText(Integer.toString(mService.getBaud()));
    }
    
}
