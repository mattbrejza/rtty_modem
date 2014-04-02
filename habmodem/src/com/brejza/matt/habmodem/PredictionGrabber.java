package com.brejza.matt.habmodem;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.codehaus.jackson.map.DeserializationConfig;
import org.mapsforge.core.GeoPoint;

import ukhas.AscentRate;
import ukhas.HabitatRxEvent;
import ukhas.PredictedPath;
import ukhas.Telemetry_string;

public class PredictionGrabber {

	Context mContext;
	public String predictions_url = "";
	protected ArrayList<PredictionRxEvent> _listeners = new ArrayList<PredictionRxEvent>();
	
	public PredictionGrabber(Context mContext, String url){
		this.mContext = mContext;
		if (!url.toLowerCase().startsWith("http://"))
		{
			url = "http://" + url;
		}
		predictions_url = url;
	}
	
	public void addPredictorUpdateListener(PredictionRxEvent listener)
	{	
		_listeners.add(listener);
	}
	
	public interface PredictionRxEvent extends EventListener
	{
		void PredictionRx(HashMap<String,List<GeoPoint>> data);
	}
	
	public void getPredictions()
	{
		
		
		
		ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
	    if (networkInfo != null && networkInfo.isConnected()) {
	        // fetch data
	    	new DownloadWebpageText().execute(predictions_url);
	    } else {
	        // display error
	    }
		
	}
	
	
	private class DownloadWebpageText extends AsyncTask<String,String, HashMap<String,List<GeoPoint>>> {
        @Override
        protected HashMap<String,List<GeoPoint>> doInBackground(String... urls) {
              
            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return null;
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(HashMap<String,List<GeoPoint>> result) {
           fireNewPrediction(result);
       }

    }
	
	
	private HashMap<String,List<GeoPoint>> downloadUrl(String myurl) throws IOException {
	    InputStream is = null;
        
	    try {
	        URL url = new URL(myurl);
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setReadTimeout(10000 /* milliseconds */);
	        conn.setConnectTimeout(15000 /* milliseconds */);
	        conn.setRequestMethod("GET");
	        conn.setDoInput(true);
	        // Starts the query
	        conn.connect();
	        int response = conn.getResponseCode();
	        Log.d("DEBUG", "The response is: " + response);
	        is = conn.getInputStream();

	        ObjectMapper mapper = new ObjectMapper();
	        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	        List<PredictedPath> pp = mapper.readValue(is, new TypeReference<List<PredictedPath>>(){ });
	        
	        HashMap<String,List<GeoPoint>> output = new HashMap<String,List<GeoPoint>>();
	        
	        for (int i = 0; i < pp.size(); i++)
	        {
	        	try
	        	{
		        	List<GeoPoint> path = new ArrayList<GeoPoint>();
		        	List<Path> p = mapper.readValue(pp.get(i).data, new TypeReference<List<Path>>(){ });
		        	for (int j = 0; j < p.size(); j++)
		        	{
			        	try{
			        		double la = Double.parseDouble(p.get(j).lat);
			        		double lo = Double.parseDouble(p.get(j).lon);
			        		path.add(new GeoPoint(la,lo));
			        	}
			        	catch(Exception e)
			        	{
			        		
			        	}
		        	}
		        	if (path.size()>0)
		        	{
		        		output.put(pp.get(i).vehicle, path);
		        	}
	        	}
	        	catch (Exception e)
	        	{
	        		
	        	}
	        }
	        

	        return output;
	        

	    }catch (Exception e){
	    	if (e != null){
	    		if (e.getMessage() != null)
	    			Log.d("DEBUG", e.getMessage());
	    	}
	    	return null;
	    } finally {
	        if (is != null) {
	            is.close();
	        } 
	    }
	}
	
	public static class Path{
		public String time;
		public String lat;
		public String lon;
		public String alt;
		
	}
	
	protected void fireNewPrediction(HashMap<String,List<GeoPoint>> data)
	{
		for (int i = 0; i < _listeners.size(); i++)
		{
			_listeners.get(i).PredictionRx(data);
		}
	}
	
}
