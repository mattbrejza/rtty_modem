package com.brejza.matt.habmodem;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ukhas.Payload;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;


public class AddPayloadFragment extends DialogFragment {
	
	 public interface NoticeDialogListener {
	        public void onDialogPositiveClick(DialogFragment dialog, String callsign);
	    }
	 
	// Use this instance of the interface to deliver action events
	NoticeDialogListener mListener;
	ConcurrentHashMap<String,Payload> _active_payloads =  new ConcurrentHashMap<String,Payload>();
	List<String> _list_active_payloads = new ArrayList<String>();
	AutoCompleteTextView s;
	    
	
	public void onAttach(Activity activity) {
        super.onAttach(activity);
       mListener = (NoticeDialogListener) activity;      
	}
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        
        View v = inflater.inflate(R.layout.dialog_add_payload, null);
        builder.setTitle(R.string.dialog_add_payload_title);
        builder.setView(v)        
        // Add action buttons
               .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int id) {
                       // add payload
                	   mListener.onDialogPositiveClick(AddPayloadFragment.this, s.getText().toString());
                   }
               })
               .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   AddPayloadFragment.this.getDialog().cancel();
                   }
               });      
        
        s = (AutoCompleteTextView) v.findViewById(R.id.auto_payload);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item, _list_active_payloads);
        s.setAdapter(adapter);
       
        
        return builder.create();
    }
    
    @Override
    public void onStart()
    {
    	super.onStart();
    }
    
    public void setAutoPayload(ConcurrentHashMap<String,Payload> in)
    {
    	_active_payloads = in;
    	for (Map.Entry<String, Payload> entry : in.entrySet())
    		_list_active_payloads.add(entry.getValue().callsign);    		   	
    }
    
   // public void AddAutoPayload(String str)
   // {
   // 	_active_payloads.add(str);
   // }
 
}
