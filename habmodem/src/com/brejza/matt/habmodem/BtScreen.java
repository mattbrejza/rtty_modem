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
import java.util.HashMap;
import java.util.List;

import com.brejza.matt.habmodem.BluetoothLeService.LocalBinderBt;
import com.brejza.matt.habmodem.Dsp_service.LocalBinder;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2) public class BtScreen extends Activity {
	
	private static final int REQUEST_ENABLE_BT = 54632;
	private static final long SCAN_PERIOD = 10000;
	
	Dsp_service mService;
    boolean mBound = false;
    
    BluetoothLeService mBtService;
    boolean mBtBound = false;
    
    boolean isReg = false;
    
    boolean mConnected = false;
    
    Handler handler;
    
    Menu _menu;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    
    ListView list;
    ArrayAdapter<String> adapter;
    boolean initList = false;
    
    ListView listviewDeviceList;
    ArrayAdapter<String> lvAdapter;
    List<String> lvValues;
    
    private StringRxReceiver strrxReceiver;
    
    List<String> characteristic_list = new ArrayList<String>();
    
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_screen);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        handler = new Handler();
        
        listviewDeviceList = (ListView)findViewById(R.id.lstDevices);
        lvValues = new ArrayList<String>();
        lvValues.add("a");
        lvAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,android.R.id.text1,lvValues);
        listviewDeviceList.setAdapter(lvAdapter);
        
        final Button btnConenct = (Button) findViewById(R.id.btnScan);
        btnConenct.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if (mScanning == false)
            		scanLeDevice(true);
            }
        });
        /*final Button btnScan = (Button) findViewById(R.id.btnConnect);
        btnScan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	ListView lv = (ListView)findViewById(R.id.lstDevices);
            	//lv.getItemAtPosition(lv.getCheckedItemPosition());
            	mBtService.connect(lv.getItemAtPosition(lv.getCheckedItemPosition()).toString());
            }
        });
*/
        listviewDeviceList.setOnItemClickListener(new OnItemClickListener() {
        	  @Override
        	  public void onItemClick(AdapterView<?> parent, View view,int position, long id) {    
        		ListView lv = (ListView)findViewById(R.id.lstDevices);
              	String idd = lv.getItemAtPosition(position).toString();
        	    mBtService.connect(idd);
        	  }

        	});
        
        
    } 

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        _menu = menu;
        if (mService != null){
	   		if (mService.enableUploader)
	        	_menu.findItem(R.id.toggle_online).setChecked(true);
   		}
        return true;
    }
	
	@Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        if (mBtBound) {
            unbindService(mBtConnection);
            mBtBound = false;
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
   		// Bind to LocalService
   		if (!mBtBound){
	        Intent intent = new Intent(this, BluetoothLeService.class);
	        bindService(intent, mBtConnection, Context.BIND_AUTO_CREATE);
   		}
   		
   		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
   		
   		if (strrxReceiver == null) strrxReceiver = new StringRxReceiver();
   		IntentFilter intentFilter1 = new IntentFilter(Dsp_service.TELEM_RX);
   		if (!isReg) { registerReceiver(strrxReceiver, intentFilter1); isReg = true;}
   		
   		//scan for BTLE devices
   		
	   	//check if supported
	   	if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
	   	    Toast.makeText(this, "BT LE not supported".toString(), Toast.LENGTH_SHORT).show();
	   	}
	   	else
	   	{
	   		final BluetoothManager bluetoothManager =
	   		        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
	   		mBluetoothAdapter = bluetoothManager.getAdapter();
	   		
	   		//make sure BT is enabled
	   		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
	   		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	   		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
	   		    //TODO: get some sorta callback from the window to start a scan when done
	   		}
	   		else
	   			scanLeDevice(true);
	   		
	   		
	   		
	   	}
	}
	
	@Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
        if (isReg){
        	if (strrxReceiver != null) unregisterReceiver(strrxReceiver);
        	isReg = false;
        }
    }
	
	private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
        	if (!mConnected){
        		
				lvValues.clear();
				lvAdapter.notifyDataSetChanged();
				
				if (mHandler == null)
	        		mHandler = new Handler();
	            mHandler.postDelayed(new Runnable() {
	                @Override
	                public void run() {
	                    mScanning = false;
	                    updateConnectionState();
	                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
	                }
	            }, SCAN_PERIOD);
	
	            mScanning = true;
	            updateConnectionState();
	            mBluetoothAdapter.startLeScan(mLeScanCallback);
        	}
        } else {
            mScanning = false;
            updateConnectionState();
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        
    }
	
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
	    @Override
	    public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
	        runOnUiThread(new Runnable() {
	           @Override
	           public void run() {
	               //mLeDeviceListAdapter.addDevice(device);
	               //mLeDeviceListAdapter.notifyDataSetChanged();
	        	   Log.i("BTLE", "FOUND SOMETHING!!!!!!!1! " + device.toString());
	        	   lvValues.add(device.toString()); //will cause issues as not modifying from UI thread
	        	   lvAdapter.notifyDataSetChanged();
	           }
	       });
	   }
	};
	
	/** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            
            updateListView();

            if (mService.enableUploader && _menu != null)
            	_menu.findItem(R.id.toggle_online).setChecked(true);

            
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private ServiceConnection mBtConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinderBt binder = (LocalBinderBt) service;
            mBtService = binder.getService();
            mBtBound = true;
            mBtService.initialize();
            
            if (mBtService.isConnected())
            	mConnected = true;
            else
            	mConnected = false;
            	
           	updateConnectionState();


        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBtBound = false;
        }
    };
    
    
	 // Handles various events fired by the Service.
	 // ACTION_GATT_CONNECTED: connected to a GATT server.
	 // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
	 // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
	 // ACTION_DATA_AVAILABLE: received data from the device. This can be a
	 // result of read or notification operations.
	 private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
	     @Override
	     public void onReceive(Context context, Intent intent) {
	         final String action = intent.getAction();
	         if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
	             mConnected = true;
	             updateConnectionState();
	             scanLeDevice(false);
	             lvValues.clear();
	             lvAdapter.notifyDataSetChanged();
	             //invalidateOptionsMenu();
	         } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
	             mConnected = false;
	             updateConnectionState();
	             //invalidateOptionsMenu();
	             //clearUI();
	         } else if (BluetoothLeService.
	                 ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
	             // Show all the supported services and characteristics on the
	             // user interface.
	             Log.i("BTLE",mBtService.getSupportedGattServices().toString());
	             List<BluetoothGattService> l = mBtService.getSupportedGattServices();
	             for (int i = 0; i < l.size(); i++)
	             {
	            	 Log.i("BTLE GATT",(l.get(i)).getUuid().toString());
	            	 List<BluetoothGattCharacteristic> clist = l.get(i).getCharacteristics();
	            	 for (int k = 0; k < clist.size(); k++)
	            	 {
	            		String c = clist.get(k).getUuid().toString();
	            		//if (c.equals("00002a37-0000-1000-8000-00805f9b34fb"))
	            		//	 mBtService.setCharacteristicNotification( clist.get(k), true);
	            		if (LoraBsGattAttributes.UUID_TELEMETRY_STRING_ASCII.equals(clist.get(k).getUuid()))
	            			 mBtService.setCharacteristicNotification( clist.get(k), true);
	            			 
	            		 characteristic_list.add(c);
	            	 }
	             }
	             
	             
	             displayGattServices(l);
	             
	            	 
	         } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
	        	 Log.i("BTLE",intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
	         }
	     }
	 };
	 
	 private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = "unknown service";
        String unknownCharaString = "unknown characteristic";
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData =
                    new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    "NAME", SampleGattAttributes.
                            lookup(uuid, unknownServiceString));
            currentServiceData.put("UUID", uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();
           // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic :
                    gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData =
                        new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                		"NAME", SampleGattAttributes.lookup(uuid,
                                unknownCharaString));
                currentCharaData.put("UUID", uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
         }
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
        //else if (item.getItemId() == R.id.graphs_button) {
        //	if ( mService.getActivePayloadList().size() > 0)
        //	{
        //		showGraphDialog(mService.getActivePayloadList().get(0));
        //	}}
        else if (item.getItemId() == R.id.log_screen) {
        	FragmentManager fm = getFragmentManager();
        	ViewLogFragment di = new ViewLogFragment();
          	di.setLogList(mService.getLog());
          	di.show(fm, "View Logs");}
        else if (item.getItemId() ==  R.id.settings_screen) {
        	intent = new Intent(this,Preferences_activity.class);
        	startActivity(intent); 
            return true;}
        else if (item.getItemId() == R.id.toggle_online){
        	//CheckBox chk = (CheckBox) findViewById(R.id.toggle_online);
        	if (mService.enableUploader){
        		mService.enableUploader = false;
        		item.setChecked(false);
        	}else{
        		mService.enableUploader = true;
        		item.setChecked(true);
        	}
        	return true;}
    	
       
       return super.onOptionsItemSelected(item); 
    }

	
	private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
	
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
	
	private ArrayAdapter<String> initAdapter()
    {
    	return new ArrayAdapter<String>(this,
		           android.R.layout.simple_list_item_1, android.R.id.text1, mService.listRxStr);
    }
	
	public void updateListView()
    {
    	if (mService.listRxStr == null)
    		return;
    	if (mService.listRxStr.size() < 1)
    		return;    	
    	
    	 handler.post(new Runnable() {
             @Override
             public void run() {        	
                 	
		    	if (!initList)
		    	{
			    	 list = (ListView) findViewById(R.id.lstBtTelem);
			     
			    	 adapter = initAdapter();			    	 
			    	 
			         // Assign adapter to ListView
			         list.setAdapter(adapter);
			         list.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
			         initList = true;
		    	}
		    	else
		    		adapter.notifyDataSetChanged();		    	
             }
      	});

    }
	
	private void updateConnectionState()
	{
		
		final TextView tstat = (TextView) findViewById(R.id.txtBtStatus);
		final Button btnConenct = (Button) findViewById(R.id.btnScan);
		
		if (mConnected){
			tstat.setText(getString(R.string.connected) + " to " + mBtService.getMacConnected());
			btnConenct.setText("Disconnect");
		}
		else if (mScanning)
		{
			tstat.setText("Scanning...");
		}
		else
		{
			btnConenct.setText("Scan");
			tstat.setText(getString(R.string.disconnected));
		}
     	     	
	}
	


}
