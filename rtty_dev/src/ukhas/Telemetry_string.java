// Copyright 2012 (C) Matthew Brejza
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.


package ukhas;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.codec.binary.Base64;


public class Telemetry_string {

	public String callsign = "";
	
	public Date time;
	
	public int packetID=0;
	
	public Gps_coordinate coords;
	
	private String raw_string = "";
	public String[] user_fields;
	
	public double frequency = 0;
	
	public boolean checksum_valid;
	
	
	public String doc_time_created;
	
	public String getSentence()
	{
		
		return "$$" + raw_string + "\n";
	}
	
	public Telemetry_string(String telem) {
		parse_telem(telem, System.currentTimeMillis() / 1000L);
		checksum_valid = check_checksum(telem,0);
	}
	public Telemetry_string(String telem, long timerx) {
		parse_telem(telem,timerx);
		checksum_valid = check_checksum(telem,0);
	}
	
	public Telemetry_string(String telem, boolean _checksum_valid) {
		parse_telem(telem, System.currentTimeMillis() / 1000L);
		checksum_valid = _checksum_valid;
		
		//get time created
		Date time = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		String t = dateFormat.format(time);
		t = t.substring(0, t.length()-2) + ":" + t.substring(t.length()-2, t.length());
		doc_time_created = t;
	}
	
	private void parse_telem(String str, long timerx)
	{
		int start = str.lastIndexOf('$');
		
		if (start < 0)
			start = 0;
		else
			start++;
		
		raw_string = str.substring(start, str.length()).trim();
		
		String[] fields;
		String[] cksplit = raw_string.split("\\*",0);  //remove checksum
		
		if (cksplit.length>0)			
			fields = cksplit[0].split(",",0);
		else
			fields = raw_string.split(",",0);
		
		if (fields.length > 6)
		{
			user_fields = new String[fields.length-6];
			System.arraycopy(fields, 6, user_fields, 0, fields.length-6);		
		}
		
		int ci = 0;
		int offset = 0;
		
		if (fields.length > 1)
		{
			//see if counter exists
			ci = fields[1].indexOf(':');
			offset = 0;
			if (ci > 0)
				offset = -1;	
		}
		
		if (fields.length >= 6+offset)
		{
			callsign = fields[0];
			
					
			
			//handle time			
			SimpleDateFormat ft;
			if (fields[2+offset].length() > 6)
				ft = new SimpleDateFormat ("HH:mm:ss");
			else
				ft = new SimpleDateFormat ("HHmmss");
			
			try //this is all a bit horrible :(
			{
				
				if (offset == 0)
					packetID = Integer.parseInt(fields[1]);
				
				
				
				//get time rx @ 12am
				Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
				Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
				ft.setTimeZone(TimeZone.getTimeZone("UTC"));
				time = ft.parse(fields[2+offset]);		
				cal2.setTime(time);
				
				cal.setTimeInMillis(timerx*1000);
				cal.set(Calendar.HOUR_OF_DAY, cal2.get(Calendar.HOUR_OF_DAY));
				cal.set(Calendar.MINUTE, cal2.get(Calendar.MINUTE));
				cal.set(Calendar.SECOND, cal2.get(Calendar.SECOND));
				
				//long best_guess = cal2.getTimeInMillis();
				
						

				if (cal.getTimeInMillis() < timerx*1000 - 12*60*60*1000)
					cal.roll(Calendar.DAY_OF_YEAR, 1);
				if (cal.getTimeInMillis() < timerx*1000 + 12*60*60*1000)
					;
				else					
					cal.roll(Calendar.DAY_OF_YEAR, -1);
				
				time = cal.getTime(); 
				
				
			}
			catch (Exception e)
			{
				System.out.println("Error parsing - " + e.toString());
			}
			
			coords = new Gps_coordinate(fields[3+offset],fields[4+offset],fields[5+offset]);			
		}
	}
	
	public String toSha256()
	{
		String str = "$$" + raw_string + "\n";
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
	
	public String raw_64_str()
	{
		String str = "$$" + raw_string + "\n";
		byte [] enc = Base64.encodeBase64(str.getBytes());
		String out = new String(enc);
	
		return out;
	}
	
	public static boolean check_checksum(String in, int start)
	{
		int crc = 0xFFFF;
		int i=0;
		while (i < in.length() && in.charAt(i) != '*')
		{
			if (in.charAt(i) != '$')
			{
				int j;
				
				crc =  (crc ^ (in.charAt(i) << 8 ));
				for (j=0; j< 8; j++)
				{
					if ((crc & 0x8000) != 0)
						crc = ((crc << 1) ^ 0x1021);
					else
						crc = (crc << 1);
				}
			}	
			i++;
		}
		
		int ckloc = in.indexOf((int)'*',start);
		if (ckloc < 0)
			return false;
		
		if (ckloc + 4 >= in.length())
			return false;
		
		//now extract checksum based on its known location and compare
		String crcstr = in.substring(ckloc+1, ckloc+5);
		crcstr = crcstr.toLowerCase();
		for (i = 0; i < crcstr.length(); i++)
		{			
			int c = (int)crcstr.charAt(i);
			if (c < 48 || c > 102 || (c<97 && c >57))
				return false;			
		}
		
		int rccrc = Integer.parseInt(crcstr, 16);
		
		if (rccrc == (crc & 0xFFFF))
			return true;
		else
			return false;
		
	}
	
	public boolean isZeroGPS(){
		return (coords.latitude == 0 || coords.longitude == 0);
	}

}
