package ukhas;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import rtty.StringRxEvent;


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
	
	protected ArrayList<HabitatRxEvent> _listeners = new ArrayList<HabitatRxEvent>();
	
	public ConcurrentHashMap<String,String> payload_configs = new ConcurrentHashMap<String,String>();
	public ConcurrentHashMap<String,String> flight_configs = new ConcurrentHashMap<String,String>();
	
	private Session s;
	private Database db;
	private Thread sdThread;
	
	private int _prev_query_time = 5 * 60 * 60;
	
	private boolean _queried_current_flights = false;
    
	//TODO: if failed due to connection error, identify error and dont clear the list.
	
	private Queue<Telemetry_string> out_buff = new LinkedBlockingQueue<Telemetry_string>();
	private Queue<QueueItem> _operations = new LinkedBlockingQueue<QueueItem>();
	
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
	
	public void addHabitatRecievedListener(HabitatRxEvent listener)
	{	
		_listeners.add(listener);
	}
	
	public void addDataFetchTask(String callsign, long startTime, long stopTime, int limit)
	{
		_operations.offer(new QueueItem(1,callsign, startTime, stopTime, limit));
		StartThread();
	}
	
	public void addGetActiveFlightsTask()
	{
		_operations.offer(new QueueItem(2,0));
		StartThread();
	}
	
	private String resolvePayloadID(String callsign)
	{
		if (payload_configs.contains(callsign))
			return payload_configs.get(callsign);
		else if (!_queried_current_flights)
			_queried_current_flights = queryActiveFlights();
			
		if (payload_configs.contains(callsign))
			return payload_configs.get(callsign);
		else
		{
			queryAllPayloadDocs(callsign);
			if (payload_configs.containsKey(callsign))
				return payload_configs.get(callsign);
			else
				return null;   //TODO: add list of non payloads
		}
		

	}
	
	public boolean queryAllPayloadDocs(String callsign)
	{
		try
		{
			//open DB connection
			if (s == null)
			{
				 s = new Session(_habitat_url,80);
				 db = s.getDatabase(_habitat_db);// + "/_design/payload_telemetry/_update/add_listener");
			}
		
			List<Document> docsout;
			View v = new View("payload_configuration/name_time_created");
			//startkey=["CRAAG1",1357396479]&endkey=["CRAAG1",0]&descending=true
			v.setStartKey("[%22" + callsign + "%22," + Long.toString((System.currentTimeMillis() / 1000L)) + "]");
			v.setEndKey("[%22" + callsign + "%22,0]");
			//v.setWithDocs(true);
			v.setLimit(1);
			v.setDescending(true);
			
			ViewResults r = db.view(v);
			docsout = r.getResults();
			
			docsout.toString();
 

			//docsout.get(0).getJSONObject().getJSONObject("doc").getString("type")
			//docsout.get(1).getJSONObject().getJSONObject("doc").getJSONArray("sentences").getJSONObject(0).getString("callsign")
			 
			if (docsout.size() > 0)
			{
				JSONArray ja = docsout.get(0).getJSONArray("key");
				if (ja.getString(0).equals(callsign))
					payload_configs.put(callsign,docsout.get(0).getId());
			}
			
			
			
			
			return true;
		}
		catch (Exception e)
		{
			System.out.println("ERROR: "+ e.toString());
			return false;
		}
	}
	
	public boolean queryActiveFlights()
	{
		try
		{
			//open DB connection
			if (s == null)
			{
				 s = new Session(_habitat_url,80);
				 db = s.getDatabase(_habitat_db);// + "/_design/payload_telemetry/_update/add_listener");
			}
		
			List<Document> docsout;
			View v = new View("flight/launch_time_including_payloads");
			
			v.setStartKey("[" + Long.toString((System.currentTimeMillis() / 1000L)-(2*60*60)) + "]");
			
			v.setWithDocs(true);
			v.setLimit(30);
			
			ViewResults r = db.view(v);
			docsout = r.getResults();
			
			docsout.toString();
 

			//docsout.get(0).getJSONObject().getJSONObject("doc").getString("type")
			//docsout.get(1).getJSONObject().getJSONObject("doc").getJSONArray("sentences").getJSONObject(0).getString("callsign")
			 
			for (int i = 0; i < docsout.size(); i++)
			{
				
				
				JSONObject obj;
				
				obj = docsout.get(i).getJSONObject().getJSONObject("doc");
				if (obj != null) {
					if (obj.containsKey("type")) {
						if (obj.getString("type").equals("payload_configuration")) {
							if (obj.containsKey("sentences")){	
								JSONArray jar = obj.getJSONArray("sentences");
								for (int j = 0; j < jar.size(); j++){
									if (jar.getJSONObject(j).containsKey("callsign")){
										String call = jar.getJSONObject(j).getString("callsign");
										if (!flight_configs.containsKey(call))
											flight_configs.put(call, docsout.get(i).getId());
										if (obj.containsKey("_id"))										
											payload_configs.put(call,obj.getString("_id"));
									}									
								}
							}							
						}
					}
				}				
			}
			
			
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	//TODO: if flight doc exists, query that view instead
	private boolean getPayloadDataSince(long timestampStart, long timestampStop, int limit, String payloadID, String callsign)
	{
		try
		{
			System.out.println("DEBUG: STARTING GET PAYLOAD");
			//open DB connection
			if (s == null)
			{
				 s = new Session(_habitat_url,80);
				 db = s.getDatabase(_habitat_db);// + "/_design/payload_telemetry/_update/add_listener");
			}
			System.out.println("DEBUG: DATABASE OPEN");
			 List<Document> docsout;
			 View v = new View("payload_telemetry/payload_time");

			 long starttime;
			 if (timestampStart > 0)
				 starttime = timestampStart;
			 else
				 starttime = (System.currentTimeMillis() / 1000L)-_prev_query_time;
			 
			 v.setStartKey("[%22" + payloadID + "%22," +Long.toString(starttime) + "]");
			 v.setEndKey("[%22" + payloadID + "%22," +Long.toString(timestampStop)+ "]");

			 v.setWithDocs(true);
			 v.setLimit(limit);

			 System.out.println("DEBUG: DATABASE START QUERY");
			 
			 //ViewResults r = db.view(v);
			 String body = db.view_dont_parse(v);
			 
			 //cut off the "preamble"
			 //int g = body.indexOf("\"rows\":");
			 
			 //if (g > 0)
			 //{
			//	 body = body.substring(g+7,body.length()-2);
			 //}
			 
			 System.out.println("DEBUG: DATABASE GOT QUERY");
			 
			 List<String> out = new LinkedList<String>();
			 
			 JsonFactory fac = new JsonFactory();
			 JsonParser jp = fac.createJsonParser(body);
			 
			// while(jp.nextValue() != JsonToken.END_OBJECT)
			 String str,str1;
			 int nullcount = 0;
			while((jp.nextToken() != JsonToken.END_OBJECT || (jp.getCurrentLocation().getCharOffset() < body.length()-50)) && nullcount < 20) //100000 > out.size())
			 {
				//jp.nextToken();

				str  = jp.getCurrentName();
				str1 = jp.getText();
				if (str != null && str1 != null)
				{
				 if (str.equals("_sentence") && !str1.equals("_sentence"))
					 out.add(str1);  
				 	nullcount = 0;
				}
				else
					nullcount++;
			 }
			jp.close();
			 
			 /*
			 docsout = r.getResults();

			 System.out.println("DEBUG: DATABASE GOT LIST OF JSON");
			 
			 
			 
			 List<String> out = new LinkedList<String>();
			 
			 
			for (int i = 0; i < docsout.size(); i++)
			{
				JSONObject obj;
				if (docsout.get(i).containsKey("doc"))
				{
					obj = docsout.get(i).getJSONObject("doc");
					if (obj.containsKey("data"))
					{
						obj = obj.getJSONObject("data");
						if (obj.containsKey("_sentence"))
						{
							out.add(obj.getString("_sentence"));
						}
					}
				}
			} */
			System.out.println("DEBUG: DATABASE PROCESSING DONE");
			fireDataReceived(out,true,callsign,timestampStart, timestampStop); 
			return true;
		}
		catch (Exception e)
		{
			fireDataReceived(null,false,callsign,timestampStart, timestampStop);
			return false;
		}
			
		
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
	
			 v.setStartKey(Long.toString((System.currentTimeMillis() / 1000L)-_prev_query_time));
			 v.setWithDocs(true);
			 v.setLimit(40);

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
		


		added=out_buff.offer(input);
	

		if (added)
		{
			_operations.offer(new QueueItem(0,1));
			StartThread();
		}
	
		
	}
	
	private synchronized void StartThread()
	{
		if (sdThread == null)
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
						JSONObject objdata = result.getJSONObject().getJSONObject("data");
						if (objdata.containsKey("_parsed"))
						{
							objdata = objdata.getJSONObject("_parsed");
							if (objdata.containsKey("payload_configuration"))
								payload_configs.put(input.callsign, objdata.getString("payload_configuration"));
							if (objdata.containsKey("flight"))
								flight_configs.put(input.callsign, objdata.getString("flight"));
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
	
	protected void fireDataReceived(List<String> data, boolean success, String callsign, long startTime, long endTime)
	{
		for (int i = 0; i < _listeners.size(); i++)
		{
			_listeners.get(i).HabitatRx(data, success, callsign, startTime, endTime);
		}
	}
	
	public void addStringRecievedListener(HabitatRxEvent listener)
	{	
		_listeners.add(listener);
	}
	
	class SendThread extends Thread
	{
		  
		  public void run()
		  {
			  boolean buff_empty = false;
			  while(!buff_empty)
			  {
				
				  
				  if (!_operations.isEmpty())
				  {
					  QueueItem qi = _operations.poll();
					  if (qi != null)
					  {
						  if (qi.type == 0)
						  {			//upload telem
							  Telemetry_string tosend = out_buff.poll();
							  boolean res = true;
							  if (tosend != null)								
								  res = _upload(tosend);  //now we have some telem, lets send it							  
							  if (!res)
								  System.out.println("UPLOAD FAILED :(");
						  }
						  else if (qi.type == 1)
						  {			//get data
							  String id = resolvePayloadID(qi.callsign);
							  if (id != null)
								  getPayloadDataSince(qi.startTime, qi.stopTime,qi.count, id, qi.callsign);
						  }
						  else     //update list of active flights
						  {
							  _queried_current_flights = queryActiveFlights();
						  }
					  }
					  else
						  buff_empty = true;
				  }
				  else
					  buff_empty = true;
				  
			  }
			
			  //deal with any items in the send queue which are left over
			  buff_empty = false;
			  while(!buff_empty)
			  {
				  
				  if (out_buff.isEmpty())
				  {
					  buff_empty = true;
				  }
				  else
				  {
					  Telemetry_string tosend = out_buff.poll();

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
	
	
	class QueueItem
	{
		public int type;				//0: upload payload, 1: get payload telem, 2: update active flight list
		public String callsign = "";
		public long startTime = 0;
		public long stopTime = 0;
		public int count = 0;
		public QueueItem(int _type,int _count)
		{
			type = _type;
			count = _count;
		}
		public QueueItem(int _type, String _callsign, long _startTime, long _stopTime, int _count)
		{
			type = _type;
			callsign = _callsign;
			startTime = _startTime;
			stopTime = _stopTime;
			count = _count;
		}
	}
}
