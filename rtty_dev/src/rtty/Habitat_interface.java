package rtty;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import net.sf.json.*;

import com.fourspaces.couchdb.CouchResponse;
import com.fourspaces.couchdb.Database;
import com.fourspaces.couchdb.Document;
import com.fourspaces.couchdb.Session;


public class Habitat_interface {

	private String _habitat_url = "habitat.habhub.org";
	private String _habitat_db = "habitat";
	private Listener _listener_info;
	private String _listener_UUID="";
	
	private Session s;
	private Database db;
	private Thread sdThread;
    
	
	private boolean _lock = false;
	private Queue<Telemetry_string> out_buff = new LinkedList<Telemetry_string>();
	
	
	public Habitat_interface(String callsign) {		
		_listener_info = new Listener(callsign);
	}
	
	public Habitat_interface(String habitat_url, String habitat_db, Listener listener_info) {
		_habitat_url = habitat_url;
		_habitat_db = habitat_db;
		
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
	
	public static void test(){
		
		Telemetry_string test = new Telemetry_string("$$TEST,12,34,6,7fgfg5sd,3,56,4,3,444*4532\n",true);
		
		String _habitat_url = "habitat.habhub.org";
		String _habitat_db = "habitat";
		
		Session s = new Session(_habitat_url,80);
		Database db = s.getDatabase(_habitat_db);

		try
		{
		
			//ViewResults result = db.view(test.toSha256());			
			//if (result == null)
			//{
			//	
			//}
			//result.
			Document testdoc = new Document();
			JSONObject data = new JSONObject();
			JSONObject receivers = new JSONObject();
			JSONObject receiver = new JSONObject();

			
			Date time = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
			String t = dateFormat.format(time);
			t = t.substring(0, t.length()-2) + ":" + t.substring(t.length()-2, t.length());
			String str64 = test.raw_64_str();
			
			data.put("_raw", str64);
			receiver.put("time_created", t);
			receiver.put("time_uploaded",t);
			receivers.put("MATT",receiver);
			
		
			testdoc.put("type","payload_telemetry");
			testdoc.put("data",data.toString());
			testdoc.put("receivers",receivers.toString());
			
			
			String sha = test.toSha256();
			db.saveDocument(testdoc,sha);
			/*
			Document doc = db.getDocument("documentid1234");
			doc.put("foo","bar");
			db.saveDocument(doc);
	
			Document newdoc = new Document();
			newdoc.put("foo","baz");
			db.saveDocument(newdoc); // auto-generated id given by the database
*/
		}
		catch (Exception e)
		{
			
			
		}
		// Running a view
/*
		ViewResults result = db.getAllDocuments(); // same as db.view("_all_dbs");
		for (Document d: result.getResults()) {
			System.out.println(d.getId());

			
			//	ViewResults don't actually contain the full document, only what the view
			//	returned.  So, in order to get the full document, you need to request a
			//	new copy from the database.
			
			Document full = db.getDocument(d.getId());
		}

		// Ad-Hoc view

		ViewResults resultAdHoc = db.adhoc("function (doc) { if (doc.foo=='bar') { emit(null, doc); }}");
		*/
	}

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
		
		if (_listener_info != null)
		{
			if (_listener_info.Data_changed())   //upload listeners location
			{
				
			}
		}
		
		//open DB connection
		if (s == null)
		{
			 s = new Session(_habitat_url,80);
			 db = s.getDatabase(_habitat_db);
		}
		
		try
		{
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
			
			//this bit needs updating
			//remove try loop, check getLastReponse
			//firstly assume document doesnt exist, try upload and look for response
			
			db.saveDocument(doc,sha);  				//try to upload as only listener
			CouchResponse cr = s.getLastResponse(); //see if successful
			
			if (cr.isOk())
				return true;
			
			if (cr.getErrorId() != "conflict")
			{
				//throw error but continue
			}
			
			int its = 0;
			//start of main loop
			while(its < 30)
			{
				its++;
				Document result = db.getDocument(sha);
				
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
				if (cr.getErrorId() != "conflict")
				{
					//throw error but continue
					its += 9;
				}
				
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
