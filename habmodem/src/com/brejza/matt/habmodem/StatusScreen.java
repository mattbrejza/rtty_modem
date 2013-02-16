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

import java.util.List;

import com.brejza.matt.habmodem.Dsp_service;
import com.brejza.matt.habmodem.Dsp_service.LocalBinder;


import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;




public class StatusScreen extends Activity implements AddPayloadFragment.NoticeDialogListener,LocationSelectFragment.NoticeDialogListener  {
	

	
//	private AudioRecord mRecorder;
//	int buffsize;
//	boolean isRecording = false;
//	private Handler handler;
//	rtty_receiver rcv = new rtty_receiver();
//	TextView t;
	ListView list;
	TextView txtchars;
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

    Handler handler;
    
    int lastScrollLength = 0;
  

    final int txtViewLines = 15;
    
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
    	

    	if (item.getItemId() == R.id.status_screen) {
            return true; }
    	else if (item.getItemId() == R.id.map_screen) {
        	finish();
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
        else if (item.getItemId() == R.id.graphs_button) {
        	List<String> ls = mService.getActivePayloadList();
        	if (ls.size() > 0){
	        	FragmentManager fm = getFragmentManager();
	        	GraphsFragment di = new GraphsFragment();	        	
	        	di.setActivePayloads(ls,mService.getPayloadList());
	          	di.show(fm, "View Graphs");}}
        else if (item.getItemId() == R.id.log_screen) {
        	FragmentManager fm = getFragmentManager();
        	ViewLogFragment di = new ViewLogFragment();
          	di.setLogList(mService.getLog());
          	di.show(fm, "View Logs");}
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
        
        handler = new Handler();
        
        
      //  rcv.addStringRecievedListener(this);
      //  handler = new Handler();
       // t = (TextView)findViewById(R.id.txttest);
        wfview = (ImageView)findViewById(R.id.imgViWF);
      //  list = (ListView)findViewById(R.id.listRxDecodedStrings);
        txtchars = (TextView)findViewById(R.id.txtRawRxChars);
       // txtchars.setEnabled(false);
        initGraph();
        
        
        wf = new Waterfall(BitmapFactory.decodeResource(this.getResources(), R.drawable.grad), 200);
        
      
        
       
        txtchars.setMovementMethod(ScrollingMovementMethod.getInstance());
       

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
   		
   		//boolean mic = this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
        //if (mic)
        //	Toast.makeText(this, "MIC AVAILABLE", Toast.LENGTH_LONG).show();
        //else
        //	Toast.makeText(this, "MIC NOT AVAILABLE :o", Toast.LENGTH_LONG).show();
        
        
        
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
    	if (mService.listRxStr == null)
    		return;
    	if (mService.listRxStr.size() < 1)
    		return;
    	if (!initList)
    	{
	    	 list = (ListView) findViewById(R.id.listRxDecodedStrings);
	     
	
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
    		adapter.notifyDataSetChanged();

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
            updateListView();
            refreshButtons();
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
            //	System.out.println(ch);
            	try
            	{
	            	txtchars.append(ch);
	            	  final int scrollAmount = txtchars.getLayout().getLineTop(txtchars.getLineCount())
	            	            -txtchars.getHeight();
	            	
	            	   
	            	  int currentpos = txtchars.getLayout().getLineTop(Math.max(0,txtchars.getLineCount() - txtViewLines));
	            	  boolean enScroll = (txtchars.getScrollY() + 20 > currentpos);
	            	  
	            	  
	            	  if ((getResources().getConfiguration().screenLayout & 
	                  	    Configuration.SCREENLAYOUT_SIZE_MASK) < Configuration.SCREENLAYOUT_SIZE_LARGE) 
	            		  enScroll = true;
	            	  
	            	   // if there is no need to scroll, scrollAmount will be <=0
	            	    if(scrollAmount>0 && (lastScrollLength < txtchars.getLineCount()) && enScroll)
	            	    	txtchars.scrollTo(0, scrollAmount);
	            	   // else
	            	   // 	txtchars.scrollTo(0,0);
	            	  
	            	  lastScrollLength = txtchars.getLineCount();
            	}
            	catch (Exception e)
            	{
            		
            	}
            }
        }
    }
    
    private class FFTUpdateReceiver extends BroadcastReceiver {

    	@Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Dsp_service.FFT_UPDATED)) {
            	//TODO: HERE
            	Bitmap img = wf.UpdateLine(mService.getFFT(),mService.get_f1_FFTbin(),mService.get_f2_FFTbin());
            	if (img != null){
	            	wfview.setImageBitmap(img);
	            	wfview.invalidate();
            	}
            	
            	
            	//tdata.setTe
            	
            	 handler.post(new Runnable() {
                     @Override
                     public void run() {
                    	
                    	 TextView tdata = (TextView) findViewById(R.id.txtDataBits);
                     	TextView tstops = (TextView) findViewById(R.id.txtStopBits);
                     	TextView tstat = (TextView) findViewById(R.id.txtStatus);
                     	
                     	if (mService.rcv.paramsValid()){
                    	 tdata.setText(Integer.toString(mService.rcv.current_data_bits));
                    	 tstops.setText(Integer.toString(mService.rcv.current_stop_bits));
                     	}else{
                       	 tdata.setText("-");
                       	 tstops.setText("-");
                     	}
                     	if (mService.getDecoderRunning())
                     		tstat.setText(mService.rcv.statusToString());
                     	else
                     		tstat.setText("Paused");
                    	 
                     }
                 	});
            	
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
    
    public void toggleEcho(View view)
    {
    	if (mService.getEchoEnabled())
    		mService.disableEcho();
    	else
    		mService.enableEcho();
    	
    	refreshButtons();
    }
    
    public void toggleBell(View view)
    {
    	if (mService.enableBell)
    		mService.enableBell = false;
    	else
    		mService.enableBell = true;
    	
    	refreshButtons();
    }
    
    public void toggleConnected(View view)
    {
    	if (mService.enableUploader)
    		mService.enableUploader = false;
    	else
    		mService.enableUploader = true;
    	
    	refreshButtons();
    }
    
    public void toggleRunning(View view)
    {
    	if (mService.getDecoderRunning()){
    		mService.disableDecoder();
    		TextView tstat = (TextView) findViewById(R.id.txtStatus);
    		tstat.setText("Paused");
    	}
    	else
    		mService.enableDecoder();
    	
    	refreshButtons();
    }
    
    private void refreshButtons()
    {
    	ImageButton btnbell = (ImageButton) findViewById(R.id.btnBell);
    	ImageButton btnecho = (ImageButton) findViewById(R.id.btnEcho);
    	ImageButton btnconn = (ImageButton) findViewById(R.id.btnConnected);
    	ImageButton btnplay = (ImageButton) findViewById(R.id.btnRunning);
    	if (mService.enableBell)
    		btnbell.setImageResource(R.drawable.ic_action_bell_on);
    	else
    		btnbell.setImageResource(R.drawable.ic_action_bell_off);

    	if (mService.getEchoEnabled())
    		btnecho.setImageResource(R.drawable.ic_action_echo_on);
    	else
    		btnecho.setImageResource(R.drawable.ic_action_echo_off);

    	if (mService.enableUploader)
    		btnconn.setImageResource(R.drawable.ic_action_connected);
    	else
    		btnconn.setImageResource(R.drawable.ic_action_disconnected);

    	if (mService.getDecoderRunning())
    		btnplay.setImageResource(R.drawable.ic_action_play);
    	else
    		btnplay.setImageResource(R.drawable.ic_action_pause);
    	
    	TextView tdata = (TextView) findViewById(R.id.txtDataBits);
     	TextView tstops = (TextView) findViewById(R.id.txtStopBits);
     	TextView tstat = (TextView) findViewById(R.id.txtStatus);
     	
     	if (mService.rcv.paramsValid()){
     		tdata.setText(Integer.toString(mService.rcv.current_data_bits));
     		tstops.setText(Integer.toString(mService.rcv.current_stop_bits));
     	}else{
     		tdata.setText("-");
     		tstops.setText("-");
     	}
     	if (mService.getDecoderRunning())
     		tstat.setText(mService.rcv.statusToString());
     	else
     		tstat.setText("Paused");
    }
    
    private void setBaudButton()
    {
    	Button btn = (Button) findViewById(R.id.btnBaud);
    	btn.setText(Integer.toString(mService.getBaud()));
    }
    
	@Override
	public void onDialogPositiveClick(DialogFragment dialog, boolean enPos, boolean enChase) {
		
		mService.changeLocationSettings(enPos,enChase);
		
	}
	
	@Override
	public void onDialogPositiveClick(DialogFragment dialog, String callsign, int lookBehind) {
		// TODO Auto-generated method stub
		mService.addActivePayload(callsign,lookBehind);
    	Balloon_data_fragment fragment = (Balloon_data_fragment) getFragmentManager().findFragmentById(R.id.balloon_data_holder);
    	    	
    	fragment.AddPayload(callsign,mService.getPayloadColour(callsign));
    	
    	mService.updateActivePayloadsHabitat();
	}
    
}
