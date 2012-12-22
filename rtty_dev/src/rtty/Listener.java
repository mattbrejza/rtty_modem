package rtty;

import java.util.Date;

import net.sf.json.JSONObject;


public class Listener {

	private String _callsign;
	private Gps_coordinate _coords;
	private Date _time_created;
	private boolean _changed;
	
	public Listener(String callsign) {
		_callsign = callsign;
		_coords = new Gps_coordinate(0,0,0);
		_time_created = new Date();
		_changed = true;
	}
	
	public Listener(String callsign, Gps_coordinate coords) {
		// TODO Auto-generated constructor stub
		_callsign = callsign;
		_coords = coords;
		_time_created = new Date();
		_changed = true;
	}

	public String getJSONDataField()
	{
		_changed = false;
		
		JSONObject data = new JSONObject();
		try
		{
			data.put("callsign", _callsign);
			data.put("latitude", _coords.latitude);
			data.put("longitude", _coords.longitude);
			data.put("altitude",_coords.altitude);
			
		}
		catch (Exception e)
		{
			return "";
		}
		return data.toString();
	}
	
	public String CallSign()
	{
		return _callsign;
	}
	
	public boolean Data_changed()
	{
		return _changed;
	}
	
	public void SetCallSign(String callsign)
	{
		_time_created = new Date();
		_callsign = callsign;
		_changed = true;
	}
	
	public Gps_coordinate Coordinates()
	{
		return _coords;
	}
	
	public void set_Gps_coordinates(Gps_coordinate coords)
	{
		_time_created = new Date();
		_coords = coords;
		_changed = true;
	}
	
}
