package com.brejza.matt.habmodem;

import java.util.ArrayList;

import com.brejza.matt.habmodem.AddPayloadFragment.NoticeDialogListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;



public class LocationSelectFragment extends DialogFragment {
	public interface NoticeDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog, boolean enPos, boolean enChase);
    }
	
	ArrayList mSelectedItems;
	boolean enChase = false;
	boolean enPos = false; 
	NoticeDialogListener mListener;
	
	public void onAttach(Activity activity) {
        super.onAttach(activity);

       mListener = (NoticeDialogListener) activity;
      
	} 
	
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	 mSelectedItems = new ArrayList();
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //builder//.setMessage("")
               
        
        builder.setMultiChoiceItems(R.array.location_dialog_items, new boolean[]  { enPos, enChase },
        		 new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which,
                    boolean isChecked) {
                if (isChecked) {
                    // If the user checked the item, add it to the selected items
                    mSelectedItems.add(which);
                } else if (mSelectedItems.contains(which)) {
                    // Else, if the item is already in the array, remove it 
                    mSelectedItems.remove(Integer.valueOf(which));
                }
            }
        })
        .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // FIRE ZE MISSILES!
                	   if (mSelectedItems.contains(0))
                		   enPos = true;
                	   else
                		   enPos = false;
                	   if (mSelectedItems.contains(1))
                		   enChase = true;                		  
                	   else
                		   enChase = false;
                	   
                	  
                	   mListener.onDialogPositiveClick(LocationSelectFragment.this, enPos, enChase); 
                   }
               })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                   }
               });
        
        
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
