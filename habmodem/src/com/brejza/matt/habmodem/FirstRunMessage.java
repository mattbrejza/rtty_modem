package com.brejza.matt.habmodem;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class FirstRunMessage extends DialogFragment {
	
    public interface NoticeDialogListener {
        public void onDialogPositiveClickFirstRun(DialogFragment dialog);
        public void onDialogNegativeClickFirstRun(DialogFragment dialog);
    }
    
    NoticeDialogListener mListener;
    
    
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }
    
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.firstrunmessage)
               .setPositiveButton(R.string.firstruncontinue, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // FIRE ZE MISSILES!
                	   mListener.onDialogPositiveClickFirstRun(FirstRunMessage.this);
                   }
               })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                	   mListener.onDialogNegativeClickFirstRun(FirstRunMessage.this);
                   }
               })
               .setTitle(R.string.firstruntitle);
        // Create the AlertDialog object and return it
        return builder.create();
    }
}