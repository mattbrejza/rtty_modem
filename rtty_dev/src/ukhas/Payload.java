package ukhas;

import java.util.TreeMap;

public class Payload {
	
		String _callsign;
		String _payloadID;
		String _flightID;
		boolean _isActiveFlight = false;
		boolean _useFlightView = false;
		int _total300 = 0;
		int _total50 = 0;
		
		int _maxLookBehind = 3*24*60*60;
		int _maxRecords = 3000;
		long _lastUpdated = 0;
		public TreeMap<Long,Telemetry_string> data = null;
		
		Payload(String callsign)
		{
			_callsign = callsign; 
		}
		
		Payload(String callsign, String payloadID, String flightID)
		{
			_callsign = callsign; 
			_payloadID = payloadID;
			_flightID = flightID;
			_isActiveFlight = true;
		}
		
		Payload(String callsign, String payloadID)
		{
			_callsign = callsign;
			_payloadID = payloadID;
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
		public void setMaxLookBehind(int t){
			_maxLookBehind = t;
		}
		public long getLastUpdate(boolean flightView) {
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
		}
		public void setLastUpdatedNow(){
			_lastUpdated = System.currentTimeMillis()/1000;
		}
		
}
