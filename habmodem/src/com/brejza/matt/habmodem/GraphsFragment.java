package com.brejza.matt.habmodem;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.brejza.matt.habmodem.Payload;
import ukhas.TelemetryConfig;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;

public class GraphsFragment extends DialogFragment {
	
	List<String> activePayloads = new ArrayList<String>();
	boolean[] selected;
	View v;
	ConcurrentHashMap<String,Payload> _data;
	LineGraph line;
	int id_counter = 0x2d065000;
	View viewGraph;
	String startupPayload = "";
	
	
	public void onAttach(Activity activity) {
        super.onAttach(activity);

    //   mListener = (NoticeDialogListener) activity;
      
	} 
	
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    	LayoutInflater inflater = getActivity().getLayoutInflater();
	    v = inflater.inflate(R.layout.fragment_graphs, null);
        builder.setTitle(R.string.dialog_graphs_title);
        builder.setView(v);
    	
        builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // FIRE ZE MISSILES!
                	    
                	  // mListener.onDialogPositiveClick(LocationSelectFragment.this, enPos, enChase); 
                   }
               });
        
        List<String> fields = new ArrayList<String>();
        fields.add("altitude");
        for (int i = 0; i < activePayloads.size(); i++)
        {
        	String call = activePayloads.get(i).toUpperCase();
        	//get a list of fields that can be displayed for this payload
			if (_data.get(call).telemetryConfig != null){
				for (int j = 0; j < _data.get(call).telemetryConfig.getTotalFields(); j++){
					TelemetryConfig.DataType dt = _data.get(call).telemetryConfig.getFieldDataType(j);
					if (dt == TelemetryConfig.DataType.FLOAT || dt == TelemetryConfig.DataType.INT){
						String f = _data.get(call).telemetryConfig.getFieldName(j);
						if (!fields.contains(f))
							fields.add(f);
					}
				}
			}
        	
        	
        	LinearLayout ll = (LinearLayout)v.findViewById(R.id.llGraphsPayloads);
        	CheckBox ck = new CheckBox(v.getContext());
        	ck.setText(activePayloads.get(i));
        	if (activePayloads.get(i).equals(startupPayload)){
        		ck.setChecked(true);
        		 if (line == null)
          			  line = new LineGraph(_data);
          		 line.addPayload(activePayloads.get(i));
        	}
        	ck.setOnCheckedChangeListener(new OnCheckedChangeListener()
        	{
        		 public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
           
	           		  if (line == null)
	           			  line = new LineGraph(_data);
	           		  if (isChecked)
	           			line.addPayload(buttonView.getText().toString());
	           		  else
	           			line.clearPayload(buttonView.getText().toString());
	           		  
	           		  drawGraph();
        		 }
        	});
        	ll.addView(ck);
        }
        
        for (int i = 0; i < fields.size(); i++){
        	
        	LinearLayout ll = (LinearLayout)v.findViewById(R.id.llGraphsFields);
        	CheckBox ck = new CheckBox(v.getContext());
        	ck.setText(fields.get(i));
        	if (fields.get(i).equals("altitude")){
        		ck.setChecked(true);
        		// if (line == null)
          		//	  line = new LineGraph(_data);
          		// line.addPayload(activePayloads.get(i));
        	}
        	ck.setOnCheckedChangeListener(new OnCheckedChangeListener()
        	{
        		 public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
           
	           		  if (line == null)
	           			  line = new LineGraph(_data);
	           		  if (isChecked){
	           			  	boolean suc = line.addField(buttonView.getText().toString());
	           			  	if (!suc)
	           			  	buttonView.setChecked(false);
	           		  }
	           		  else
	           			line.clearField(buttonView.getText().toString());
	           		  
	           		  drawGraph();
        		 } 
        	});
        	ll.addView(ck);
        }
        
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item, _list_log);
        
        if (line != null){	
        	line.addField("altitude");
        	drawGraph();
        }
        
        // Create the AlertDialog object and return it
        return builder.create();
    }
    
      
    public void setStartCall(String call)
    {
    	startupPayload = call;
    }
    
    public void drawGraph()
    {
    	//LinearLayout ll = (LinearLayout) getActivity().findViewById(R.id.llGraphsN);
    	//line = new LineGraph(_data);
    	//for (int i = 0; i < activePayloads.size(); i++)
    	//	line.putPayload(activePayloads.get(i));
    	View vg = line.getView(getActivity());
    	if (vg == null)
    		return;
    	//v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
    	LinearLayout ll = ((LinearLayout)v.findViewById(R.id.llGraphsN));
    	
    	if (viewGraph == null)
    		ll.addView(vg);
    	else {
    		ll.removeView(viewGraph);
    		ll.addView(vg);
    	}
    	
    	viewGraph = vg;
    	ll.invalidate();
    }
    
    public void setActivePayloads(List<String> ap, ConcurrentHashMap<String,Payload> data)
    {
    	_data = data;
    	activePayloads = ap;
    	selected = new boolean[ap.size()];
    }
}

