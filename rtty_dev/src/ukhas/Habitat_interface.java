package ukhas;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.json.*;

import com.fourspaces.couchdb.CouchResponse;
import com.fourspaces.couchdb.Database;
import com.fourspaces.couchdb.Document;
import com.fourspaces.couchdb.Session;
import com.fourspaces.couchdb.View;
import com.fourspaces.couchdb.ViewResults;

public class Habitat_interface {

	private String _habitat_url = "habitat.habhub.org";
	private String _habitat_db = "habitat";
	private Listener _listener_info;
	private String _listener_UUID="";
	
	public ConcurrentHashMap<String,String> payload_configs = new ConcurrentHashMap<String,String>();
	
	private Session s;
	private Database db;
	private Thread sdThread;
	
	private int _prev_query_time = 5 * 60 * 60;
    
	//TODO: if failed due to connection error, identify error and dont clear the list.
	
	private boolean _lock = false;
	private Queue<Telemetry_string> out_buff = new LinkedList<Telemetry_string>();
	private Queue<String> operations = new LinkedList<String>();
	
	public Habitat_interface(String callsign) {		
		_listener_info = new Listener(callsign);
	}
	
	public Habitat_interface(String habitat_url, String habitat_db, Listener listener_info) {
		_habitat_url = habitat_url;
		_habitat_db = habitat_db;
		_listener_info = listener_info;
		
	}
	
	public Habitat_interface(String habitat_url, String habitat_db) {
		_habitat_url = habitat_url;
		_habitat_db = habitat_db;
		
	}
	
	public void Test()
	{
		
		 s = new Session(_habitat_url,80);
		 db = s.getDatabase(_habitat_db);
		 List<Document> foodoc;
		 View v = new View("payload_configuration/callsign_time_created_index");
		 //View v = new View("payload_configuration/callsign_time_created_index&startkey%3D[%22APEX%22]");
		// v.setKey("startkey=APEX");
		 v.setStartKey("[%22APEX%22]");
		 v.setLimit(1);
		//foodoc = db.view("flight/end_start_including_payloads").getResults();
		 foodoc = db.view(v).getResults();
		foodoc.toString();
		
		
		 
		
	}
	
	public void getActivePayloads()
	{
		
		try
		{
			//open DB connection
			if (s == null)
			{
				 s = new Session(_habitat_url,80);
				 db = s.getDatabase(_habitat_db);// + "/_design/payload_telemetry/_update/add_listener");
			}
			
			 List<Document> foodoc;
			 View v = new View("payload_telemetry/time");
			 //View v = new View("payload_configuration/callsign_time_created_index&startkey%3D[%22APEX%22]");
			// v.setKey("startkey=APEX");
			 v.setStartKey(Long.toString((System.currentTimeMillis() / 1000L)-_prev_query_time));
			 v.setWithDocs(true);
			 v.setLimit(40);
			//foodoc = db.view("flight/end_start_including_payloads").getResults();
			 ViewResults r = db.view(v);
			 foodoc = r.getResults();
			// foodoc = db.view(v).getResults();
			foodoc.toString();
			//((JSONObject)((JSONObject)foodoc.get(1).getJSONObject().get("doc")).get("data")).get("payload")
			
		}
		catch (Exception e)
		{
			
		}
	}
	
	public void upload_payload_telem(Telemetry_string input)
	{
		boolean added=false;
		
		//TODO: create a thread which can wait to have the telem added to the queue
		if (!_getLock())
		{
			System.out.println("DROPPED STRING");
			return;
		}

		added=out_buff.offer(input);
		_lock=false;

		
		
		if (sdThread == null && added)
		{
			sdThread = new SendThread();
			sdThread.start();
		}
		else if (!sdThread.isAlive())			
		{
			sdThread = new SendThread();
			sdThread.start();
		}
		
	}
	

	/* use this method to get lock. can release lock by direct manipulation
	 * 
	 */
	private synchronized boolean _getLock()
	{
		if (_lock)
			return false;
		else
		{
			_lock = true;
			return true;
		}
	}
	
	private boolean _upload(Telemetry_string input)
	{
		try
		{
			//open DB connection
			if (s == null)
			{
				 s = new Session(_habitat_url,80);
				 db = s.getDatabase(_habitat_db);// + "/_design/payload_telemetry/_update/add_listener");
			}
			
			if (_listener_info != null)
			{
				if (_listener_info.Data_changed())   //upload listeners location
				{
					Document doc = new Document ();
					
					//date uploaded
					Date time = new Date();
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
					String t = dateFormat.format(time);
					t = t.substring(0, t.length()-2) + ":" + t.substring(t.length()-2, t.length());
					
					doc.put("type","listener_telemetry");
					doc.put("time_uploaded",t);
					doc.put("time_created", _listener_info.get_time_created());
					doc.put("data", _listener_info.getJSONDataField());
					
					String sha = _listener_info.toSha256();
					
					db.saveDocument(doc,sha);
					CouchResponse cr = s.getLastResponse();
					System.out.println(cr);
					if (cr.isOk())
						_listener_UUID = sha;
					
				}
			}
		
		
		
		
			Document doc = new Document();
			JSONObject data = new JSONObject();
			JSONObject receivers = new JSONObject();
			JSONObject receiver = new JSONObject();
			
			//date uploaded
			Date time = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
			String t = dateFormat.format(time);
			t = t.substring(0, t.length()-2) + ":" + t.substring(t.length()-2, t.length());
			
			String str64 = input.raw_64_str();
			
			data.put("_raw", str64);
			receiver.put("time_created", input.doc_time_created);
			receiver.put("time_uploaded",t);
			if (_listener_UUID != "")
				receiver.put("latest_telemtry",_listener_UUID);
			receivers.put(_listener_info.CallSign(),receiver);
			
		
			doc.put("type","payload_telemetry");
			doc.put("data",data.toString());
			doc.put("receivers",receivers.toString());
			
			
			String sha = input.toSha256();
		
			
			db.saveDocument(doc,sha);  				//try to upload as only listener
			CouchResponse cr = s.getLastResponse(); //see if successful
			
			if (cr.isOk())
			{		//addition went well, but get the payload config ID if not already
				if (payload_configs.containsKey(input.callsign))
					return true;
				
				//if not already existing, query the now parsed document
				Document result = db.getDocument(sha);
				
				if (result != null)
				{
					if (result.getJSONObject().containsKey("data"))
					{
						JSONObject objdata = (JSONObject)(result.getJSONObject().get("data"));
						if (objdata.containsKey("_parsed"))
						{
							JSONObject obparse = (JSONObject)objdata.get("_parsed");
							if (obparse.containsKey("payload_configuration"))
								payload_configs.put(input.callsign, obparse.get("payload_configuration").toString());
						}
					}					
				}
				return true;				
			}
			
			if (!cr.getErrorId().equals("conflict"))
			{
				//throw error but continue
			}
			
			Document result = db.getDocument(sha);
			
			//if payload configs does not contain the payload config ID, get it
			if (!payload_configs.containsKey(input.callsign))
			{				
				
				if (result != null)
				{
					if (result.getJSONObject().containsKey("data"))
					{
						JSONObject objdata = (JSONObject)(result.getJSONObject().get("data"));
						if (objdata.containsKey("_parsed"))
						{
							JSONObject obparse = (JSONObject)objdata.get("_parsed");
							if (obparse.containsKey("payload_configuration"))
								payload_configs.put(input.callsign, obparse.get("payload_configuration").toString());
						}
					}					
				}
			}
			
			int its = 0;
			//start of main loop
			while(its < 30)
			{
				its++;

				if (result == null) 
					return false;           //somethings gone wrong
				
				//instead try to append document
	
				JSONObject existing_receivers = new JSONObject();
				
				JSONObject _dat_a = (JSONObject) result.get("data");
				if (_dat_a == null)
				{
					System.out.println("DID NOT PARSE DATA SECTION");
				}
				/*
				double gf = 4.3600000000000003197;
				double ss = 0.0000000000000003197;
				double p = gf - 4.36;
				System.out.println(_dat_a.get("battery") + "   " + gf + "   " + ss + "   " + p); */
				if (!result.containsKey("receivers"))
					return false;           //somethings gone wrong
				
				existing_receivers = (JSONObject) result.remove("receivers");
				existing_receivers.put(_listener_info.CallSign(),receiver);
				result.put("receivers", existing_receivers);
				
				db.saveDocument(result,sha);
				cr = s.getLastResponse(); 
				//System.out.println(cr.statusCode);
				if (cr.isOk())
					return true;
				if (!cr.getErrorId().equals("conflict"))
				{
					//throw error but continue
					its += 9;
				}
				//get document for next run through
				result = db.getDocument(sha);
			}
			//System.out.println(cr.isOk() + "  " + cr.getErrorId() + "  " + cr.getErrorReason());
			//System.out.println(s.getLastResponse());
			
		}
		catch (Exception e)
		{
			return false;
		}
		return false;
	}
	
	class SendThread extends Thread
	{
		  
		  public void run()
		  {
			  boolean buff_empty = false;
			  while(!buff_empty)
			  {
				  int attempts=0;
				  while (attempts < 10 && !_getLock())
				  {
					  if (attempts >= 9)
						  return ;
					  
					  try {
						Thread.sleep(2);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					  attempts++;
				  }
				  
				  if (out_buff.isEmpty())
				  {
					  _lock = false;
					  buff_empty = true;
				  }
				  else
				  {
					  Telemetry_string tosend = out_buff.poll();
					  
					  _lock = false;
					  boolean res = true;
					  if (tosend == null)
						  buff_empty = true;
					  else
						  res = _upload(tosend);  //now we have some telem, lets send it
					  
					  if (!res)
						  System.out.println("UPLOAD FAILED :(");
					  
					  
				  }
			  }
		  }
	}
}
