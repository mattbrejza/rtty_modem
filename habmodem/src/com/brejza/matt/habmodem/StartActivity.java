package com.brejza.matt.habmodem;

import group.pals.android.lib.ui.filechooser.FileChooserActivity;
import group.pals.android.lib.ui.filechooser.io.localfile.LocalFile;

import java.io.File;
import java.util.List;

import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.view.Menu;

public class StartActivity extends Activity implements FirstRunMessage.NoticeDialogListener, MapFileMessage.NoticeDialogListener {

	private static final int _ReqChooseFile = 0;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_start, menu);
        return true;
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
    	boolean firstrun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("firstrun1", false);
   	    if (!firstrun){

   	    	FragmentManager fm = getFragmentManager();
   	    	FirstRunMessage di = new FirstRunMessage();	    	
          	di.show(fm, "firstrun");
   	    }
   	    else
   	    {
   	     String mapst = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString("pref_map_path", "");

           File file = new File(mapst);
           if(file.exists())
           {
        	   //start main activity
        	   Intent intent = new Intent(this, Map_Activity.class);
        	   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP );
        	   startActivity(intent);
        	   finish();
           }
           else
           {
        	   	FragmentManager fm = getFragmentManager();
      	    	MapFileMessage di = new MapFileMessage();	    	
             	di.show(fm, "mapmessage");
           }
   	    }
    }

	@Override
	public void onDialogPositiveClickFirstRun(DialogFragment dialog) {
		// TODO Auto-generated method stub
		getSharedPreferences("PREFERENCE", MODE_PRIVATE)
        .edit()
        .putBoolean("firstrun1", true)
        .commit();
		
		FragmentManager fm = getFragmentManager();
	    MapFileMessage di = new MapFileMessage();	    	
     	di.show(fm, "mapmessage");
	}
	
	 @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	        switch (requestCode) {
	        case _ReqChooseFile:
	            if (resultCode == RESULT_OK) {
	              
	                List<LocalFile> files = (List<LocalFile>)
	                    data.getSerializableExtra(FileChooserActivity._Results);
	                for (File f : files)
	                {
	                	PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).edit().putString("pref_map_path", f.getPath()).commit();
	                	System.out.println(f.toString());
	                }	              
	            }
	            break;
	        }
	    }

	
	 private void showMapChooser()
	    {
	    	Intent intent = new Intent(StartActivity.this, FileChooserActivity.class);
			intent.putExtra(FileChooserActivity._Rootpath, (Parcelable) new LocalFile(Environment.getExternalStorageDirectory().getPath() ));
			intent.putExtra(FileChooserActivity._RegexFilenameFilter, "(?si).*\\.(map)$");
			intent.putExtra(FileChooserActivity._Theme, android.R.style.Theme_Dialog);
			startActivityForResult(intent, _ReqChooseFile);
	    }

	@Override
	public void onDialogNegativeClickFirstRun(DialogFragment dialog) {
		// TODO Auto-generated method stub
		this.finish();
		
	}

	@Override
	public void onDialogPositiveClickMapHelp(DialogFragment dialog) {
		// TODO Auto-generated method stub
 	   showMapChooser();
	}

	@Override
	public void onDialogNegativeClickMapHelp(DialogFragment dialog) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, StatusScreen.class);
    	startActivity(intent);
	}

}
