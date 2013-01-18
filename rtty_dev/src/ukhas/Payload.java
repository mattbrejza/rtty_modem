package ukhas;

import java.util.TreeMap;

public class Payload {
	
		public String callsign;
		String _payloadID;
		String _flightID;
		boolean _isActiveFlight = false;
		boolean _useFlightView = false;
		boolean _activePayload = false;
		int _total300 = 0;
		int _total50 = 0;
		
		int _maxLookBehind = 4*24*60*60;
		int _maxRecords = 3000;
		long _lastUpdated = 0;
		public TreeMap<Long,Telemetry_string> data = new TreeMap<Long,Telemetry_string>();
		
		AscentRate ascentRate = new AscentRate();
		
		public Payload(String call, boolean activePayload, int lookBehind)
		{
			callsign = call; 
			_activePayload = activePayload;
			_maxLookBehind = lookBehind * 24*60*60;
		}
		
		public Payload(String call, boolean activePayload)
		{
			callsign = call; 
			_activePayload = activePayload;
		}
		
		public Payload(String call, String payloadID, String flightID)
		{
			callsign = call; 
			_payloadID = payloadID;
			_flightID = flightID;
			_isActiveFlight = true;
			_activePayload = false;
		}
		
		public Payload(String call, String payloadID)
		{
			callsign = call;
			_payloadID = payloadID;
			_activePayload = false;
		}
		
		public Payload(Telemetry_string str){
			callsign = str.callsign;
			data.put(Long.valueOf(str.time.getTime()), str);
			_activePayload = true;
		}		
		
		public void setFlightID(String id){
			_flightID = id;
		}
		public String getFlightID(){
			return _flightID;
		}
		public void setPayloadID(String id){
			_payloadID = id;
		}
		public String getPayloadID(){
			return _payloadID;
		}
		public void setMaxLookBehindDays(int t){
			_maxLookBehind = t * 60*60*24;
		}
		public void setMaxLookBehind(int t){
			_maxLookBehind = t;
		}
		public long getLastUpdated(){
			return _lastUpdated;
		}
		public void setMaxRecords(int max){
			_maxRecords = max;
		}
		public boolean isActivePayload(){
			return _activePayload;
		}
		public void setIsActivePayload(boolean ap){
			_activePayload = ap;
		}
		public int getMaxRecords(){
			return _maxRecords;
		}
		public long getUpdateStart(boolean flightView) {
			if (_lastUpdated == 0){
				if (flightView)
					return 0;
				else
					return (System.currentTimeMillis()/1000 - _maxLookBehind);
			}
			else
				return _lastUpdated;
		}
		public void setLastUpdated(long t){
			_lastUpdated = t;
			_activePayload = true;
		}
		public void setLastUpdatedNow(){
			_lastUpdated = System.currentTimeMillis()/1000;
			_activePayload = true;
		}
		public Telemetry_string getLastString()	{
			if (data.size() > 0)
			{
				return data.lastEntry().getValue();
			}
			else
				return null;
		}
		public double getAscentRate(){
			return ascentRate.getAscentRate();
		}

		public void putPacket(Telemetry_string str) {
			data.put(str.time.getTime(), str);
			if (str.coords.alt_valid)
				ascentRate.AddData(str.time.getTime(), str.coords.altitude);
		}
		
		public void putPackets( TreeMap<Long,Telemetry_string> in){
			data.putAll(in);
			Gps_coordinate c = data.lastEntry().getValue().coords;
			if (c.alt_valid)
				ascentRate.AddData(data.lastEntry().getValue().time.getTime(),c.altitude);
		}
		
}
