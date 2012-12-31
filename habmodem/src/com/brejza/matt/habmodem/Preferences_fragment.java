package com.brejza.matt.habmodem;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class Preferences_fragment extends PreferenceFragment{

	public Preferences_fragment() {
		// TODO Auto-generated constructor stub
	}
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }


}



    