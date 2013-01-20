package com.brejza.matt.habmodem;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class ViewLogFragment extends DialogFragment {
	
	// public interface NoticeDialogListener {
	//        public void onDialogPositiveClick(DialogFragment dialog, String callsign, int lookBehind);
	 //   }
	 
	// Use this instance of the interface to deliver action events
	//NoticeDialogListener mListener;
	//public void onAttach(Activity activity) {
    //    super.onAttach(activity);
    //   mListener = (NoticeDialogListener) activity;      
	//}
	
	List<String> _list_log = new ArrayList<String>();
	//List<String> _list_log = new ArrayList<String>();
	
	//View v;
	    
	
	
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        
        View v = inflater.inflate(R.layout.fragment_log, null);
        builder.setTitle(R.string.dialog_logs_title);
        builder.setView(v)        
        // Add action buttons
               .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int id) {
                                    	   
                	//   mListener.onDialogPositiveClick(AddPayloadFragment.this, s.getText().toString(), i);
                   }
               });
              /* .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   ViewLogFragment.this.getDialog().cancel();
                   }
               });  */    
        
        ListView lv = (ListView) v.findViewById(R.id.listvLog);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item, _list_log);
     
        
        lv.setAdapter(adapter);
       
        
        return builder.create();
    }
    
    @Override
    public void onStart()
    {
    	super.onStart();
    }
    
    public void setLogList(LoggingQueue in)
    {
    	Iterator<String> iterator = in.iterator();
    	while(iterator.hasNext()){
    	  _list_log.add(iterator.next());
    	}
    }
    
}
