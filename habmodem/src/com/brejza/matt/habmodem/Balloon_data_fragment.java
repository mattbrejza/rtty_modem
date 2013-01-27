package com.brejza.matt.habmodem;

import java.util.HashMap;
import java.util.Map;

import ukhas.Telemetry_string;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class Balloon_data_fragment extends Fragment {


	Map<String, Data_snippet_fragment> info_boxes = new HashMap<String, Data_snippet_fragment>();
	int id_counter = 0x2d005000;
	
	public Balloon_data_fragment() {
		// TODO Auto-generated constructor stub
	}
	
	public void AddPayload (String callsign, int colour)
	{
		if (info_boxes.containsKey(callsign.toUpperCase()))
			return;

		Data_snippet_fragment dsf = new Data_snippet_fragment();
		dsf.setCallsign(callsign);
		dsf.setColour(colour);
		dsf.containg_fragment = this;  //so that it cna remoe itself
		FrameLayout  innerLayout1 = new FrameLayout (getView().getContext());
		
		info_boxes.put(callsign.toUpperCase(), dsf);
 
    	innerLayout1.setId(id_counter);
    	FragmentManager fragmentManager = getFragmentManager();
    	FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    	fragmentTransaction.add(id_counter, dsf);
    	fragmentTransaction.commit();
    	id_counter++;
    	
    	
    	LinearLayout root = (LinearLayout)getView().findViewById(R.id.layout_payloads);
    	
    	
    	
    	root.addView(innerLayout1,0);
    	
	}
	
	public void updatePayload(Telemetry_string str, double ascentrate, double maxAltitude)
	{
		
		String call = str.callsign;
		
		Data_snippet_fragment dsf = info_boxes.get(call.toUpperCase());
		
		if (dsf == null)
		{
			int colour = ((Map_Activity)getActivity()).getColour(str.callsign);
			AddPayload(str.callsign,colour);
			dsf = info_boxes.get(call.toUpperCase());
			if (dsf == null)
				return;
		}
		
		dsf.updateDisplay(str,ascentrate,maxAltitude);
		
	}
	
	public void removePayload(String callsign)
	{
		Data_snippet_fragment dsf = info_boxes.get(callsign.toUpperCase());
		
		if (dsf == null)
			return;
		
		View v = dsf.getView();

    	FragmentManager fragmentManager = getFragmentManager();
    	FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    	fragmentTransaction.remove(dsf).commit();
    	
    	LinearLayout root = (LinearLayout)getView().findViewById(R.id.layout_payloads);
    	
    	root.removeView(v);
    	
    	info_boxes.remove(callsign.toUpperCase());
    	
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_balloon_data, container, false);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
    	
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
    	//AddPayload("ASTRA1");
    	//removePayload("ASTRA1");
    	//AddPayload("ASTRA3");
    	
    	
    	
    	//updatePayload(new Telemetry_string("$$ASTRA1,09:32:51,5122.6553,-00018.6129,10459,12.5*3E4A"));
    	
    	/*FrameLayout  innerLayout1 = new FrameLayout (getView().getContext());
    	innerLayout1.setId(0xFF3);
    	FragmentManager fragmentManager = getFragmentManager();
    	FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    	Data_snippet_fragment asf = new Data_snippet_fragment();
    	fragmentTransaction.add(0xFF3, asf);
      
    	
    	
    	LinearLayout my_root = (LinearLayout)getView().findViewById(R.id.layout_payloads);
    	
    	
    	my_root.addView(innerLayout1);
    	
    	
    	FrameLayout  innerLayout2 = new FrameLayout (getView().getContext());
    	innerLayout1.setId(0xFF3);

    	Data_snippet_fragment asf2 = new Data_snippet_fragment();
    	fragmentTransaction.add(0xFF3, asf2);
        fragmentTransaction.commit();
    	
    	my_root.addView(innerLayout2);*/
  
  
    	
    }
}
