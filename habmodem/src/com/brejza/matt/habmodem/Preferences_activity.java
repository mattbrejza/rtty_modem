package com.brejza.matt.habmodem;

import android.app.Activity;
import android.os.Bundle;

public class Preferences_activity extends Activity  {

	public Preferences_activity() {
		// TODO Auto-generated constructor stub
	}
	
	 protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        // Display the fragment as the main content.
	        getFragmentManager().beginTransaction()
	                .replace(android.R.id.content, new Preferences_fragment())
	                .commit();
	    }

}

