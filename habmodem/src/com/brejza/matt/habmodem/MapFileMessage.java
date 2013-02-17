package com.brejza.matt.habmodem;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

public class MapFileMessage extends DialogFragment {
	
    public interface NoticeDialogListener {
    	public void onDialogPositiveClickMapHelp(DialogFragment dialog);
    	public void onDialogNegativeClickMapHelp(DialogFragment dialog);
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
    	
    	final SpannableString s =  new SpannableString(getActivity().getText(R.string.maphelpermessage));
    	Linkify.addLinks(s, Linkify.WEB_URLS);
    	final TextView message = new TextView(getActivity());
    	message.setText(s);
    	message.setMovementMethod(LinkMovementMethod.getInstance());
    	
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(message)
        		//.setMessage(s)//R.string.maphelpermessage)
               .setPositiveButton(R.string.maphelpercontinue, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   mListener.onDialogPositiveClickMapHelp(MapFileMessage.this);
                   }
               })
               .setNegativeButton(R.string.maphelpernomap, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   mListener.onDialogNegativeClickMapHelp(MapFileMessage.this);
                   }
               })
               .setTitle(R.string.mapshelptitle);
        // Create the AlertDialog object and return it
        return builder.create();
    }
}