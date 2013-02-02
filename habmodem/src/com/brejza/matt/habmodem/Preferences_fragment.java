package com.brejza.matt.habmodem;

import java.io.File;
import java.util.List;

import group.pals.android.lib.ui.filechooser.FileChooserActivity;
import group.pals.android.lib.ui.filechooser.io.localfile.LocalFile;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.app.Activity;

import group.pals.android.lib.ui.filechooser.FileChooserActivity;
import group.pals.android.lib.ui.filechooser.io.localfile.LocalFile;

public class Preferences_fragment extends PreferenceFragment{

	private static final int _ReqChooseFile=0;
	
	public Preferences_fragment() {
		// TODO Auto-generated constructor stub
	}
	
    private void showMapChooser()
    {
  
    	Intent intent = new Intent(getActivity(), FileChooserActivity.class);
		intent.putExtra(FileChooserActivity._Rootpath, (Parcelable) new LocalFile(Environment.getExternalStorageDirectory().getPath() ));
		intent.putExtra(FileChooserActivity._RegexFilenameFilter, "(?si).*\\.(map)$");
		intent.putExtra(FileChooserActivity._Theme, android.R.style.Theme_Dialog);
		startActivityForResult(intent, _ReqChooseFile);
    }
    
    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case _ReqChooseFile:
            if (resultCode == android.app.Activity.RESULT_OK) {
                /*
                 * you can use two flags included in data
                 */
               // IFileProvider.FilterMode filterMode = (IFileProvider.FilterMode)
               //     data.getSerializableExtra(FileChooserActivity._FilterMode);
               // boolean saveDialog = data.getBooleanExtra(FileChooserActivity._SaveDialog, false);

                /*
                 * a list of files will always return,
                 * if selection mode is single, the list contains one file
                 */
            	
                List<LocalFile> files = (List<LocalFile>)
                    data.getSerializableExtra(FileChooserActivity._Results);
                for (File f : files)
                {
                	PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).edit().putString("pref_map_path", f.getPath()).commit();
                	System.out.println(f.toString());
                } 
                setMapStr();
            }
            break;
        }
    }
	
    private void setMapStr()
    {
    	 Preference pref = (Preference)getPreferenceScreen().findPreference("pref_map_path");
    	 pref.setSummary(PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getString("pref_map_path", ""));
    }
    
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        setMapStr();
        Preference myPref = (Preference) findPreference("pref_map_path");
       
        myPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {        	

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				// TODO Auto-generated method stub
				showMapChooser();
				return false;
			}
			
        });
    }


}



    