package ukhas;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;

import net.sf.json.JSONObject;


public class Listener {

	private String _callsign;
	private Gps_coordinate _coords;
	private float _speed = 0.0f;
	private boolean _speedValid = false;
	private Date _time_created;
	private boolean _changed;
	private boolean _isChase = false;
	
	public Listener(String callsign, boolean isChase) {
		_callsign = callsign;
		_coords = new Gps_coordinate();
		_time_created = new Date();
		_changed = true;
		_isChase = isChase;
	}
	
	public Listener(String callsign, Gps_coordinate coords, boolean isChase) {
		// TODO Auto-generated constructor stub
		_callsign = callsign;
		_coords = coords;
		_time_created = new Date();
		_changed = true;
		_isChase  = isChase;
	}
	public Listener(String callsign, Gps_coordinate coords, float speed, boolean isChase) {
		// TODO Auto-generated constructor stub
		_callsign = callsign;
		_coords = coords;
		_time_created = new Date();
		_changed = true;
		_isChase  = isChase;
		_speed = speed;
		_speedValid = true;
	}

	public JSONObject getJSONDataField()
	{
		_changed = false;
		
		JSONObject data = new JSONObject();
		try
		{
			data.put("callsign", _callsign);
			if (_coords.latlong_valid){
				data.put("latitude", _coords.latitude);
				data.put("longitude", _coords.longitude);
				if (_isChase)
					data.put("chase", true);
			}
			if (_coords.alt_valid)
				data.put("altitude",_coords.altitude);
			if (_speedValid)
				data.put("speed",_speed);
			
			
		}
		catch (Exception e)
		{
			return null;
		}
		return data;
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
	
	public String get_time_created()
	{
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		String t = dateFormat.format(_time_created);
		t = t.substring(0, t.length()-2) + ":" + t.substring(t.length()-2, t.length());
		return t;
	}
	
	public String toSha256()
	{
		String str = _callsign + get_time_created();
		byte [] enc = Base64.encodeBase64(str.getBytes());
		byte[] sha = null;
		
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-256");
			md.update(enc); 
			sha = md.digest();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return bytesToHexStr(sha);
	}
	
	//ref: http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
		public static String bytesToHexStr(byte[] bytes) {
		    final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
		    char[] hexChars = new char[bytes.length * 2];
		    int v;
		    for ( int j = 0; j < bytes.length; j++ ) {
		        v = bytes[j] & 0xFF;
		        hexChars[j * 2] = hexArray[v >>> 4];
		        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		    }
		    return new String(hexChars);
		}
	
}
