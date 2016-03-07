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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import org.msgpack.MessagePack;

import org.msgpack.type.Value;
import org.msgpack.unpacker.Unpacker;


import org.apache.commons.codec.binary.Base64;


public class Telemetry_string implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 0x5901fa8c0e38abb1L;

	public String callsign = "";
	
	public Date time = null;
	
	public int packetID=0;
	
	public Gps_coordinate coords;
	
	private String raw_string = "";
	public String[] user_fields;
	
	public double frequency = 0;
	
	public boolean checksum_valid;
	
	public Map<String,String> habitat_metadata = null;
	
	private double[] extraFields;
	
	public String doc_time_created;
	
	public String getSentence()
	{
		
		return raw_string + "\n";
	}
	
	
	public Telemetry_string(byte[] telem, TelemetryConfig tc) {
		parse_telem(telem, System.currentTimeMillis() / 1000L, tc);
		checksum_valid = true;
		
		//get time created
		Date time = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		String t = dateFormat.format(time);
		t = t.substring(0, t.length()-2) + ":" + t.substring(t.length()-2, t.length());
		doc_time_created = t;
	}
	public Telemetry_string(String telem, TelemetryConfig tc) {
		parse_telem(telem, System.currentTimeMillis() / 1000L, tc);
		checksum_valid = check_checksum(telem,0);
		
		//get time created
		Date time = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		String t = dateFormat.format(time);
		t = t.substring(0, t.length()-2) + ":" + t.substring(t.length()-2, t.length());
		doc_time_created = t;
	}
	public Telemetry_string(String telem, long timerx, TelemetryConfig tc) {
		parse_telem(telem,timerx, tc);
		checksum_valid = check_checksum(telem,0);
		
		//get time created
		Date time = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		String t = dateFormat.format(time);
		t = t.substring(0, t.length()-2) + ":" + t.substring(t.length()-2, t.length());
		doc_time_created = t;
	}
	
	public Telemetry_string(String telem, boolean _checksum_valid, TelemetryConfig tc) {
		parse_telem(telem, System.currentTimeMillis() / 1000L, tc);
		checksum_valid = _checksum_valid;
		
		//get time created
		Date time = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		String t = dateFormat.format(time);
		t = t.substring(0, t.length()-2) + ":" + t.substring(t.length()-2, t.length());
		doc_time_created = t;
	}
	
	private void parse_telem(byte[] str, long timerx, TelemetryConfig tc)
	{
		
		MessagePack msgpack = new MessagePack();
		
		ByteArrayInputStream in = new ByteArrayInputStream(str);
        Unpacker unpacker = msgpack.createUnpacker(in);
		
		byte[] base64enc = Base64.encodeBase64(str);
        
        try {
        	while(in.available()>0)   //TODO: handle multiple messages
        	{
        		Value v = unpacker.readValue();
        		if (v.isMapValue())
        		{
        			//raw_string = v.toString();
        			
        			boolean valid_lock = true;
        			
        			for (Value s : v.asMapValue().keySet()) 
        			{
        				Value item = v.asMapValue().get(s); 
        				if (s.isIntegerValue()){
        					switch (s.asIntegerValue().intValue())
        					{
        					case 0:        			//CALLSIGN	
        						callsign = item.toString();
        						if (callsign.startsWith("\""))        //TODO: this is horrible
        							callsign = callsign.substring(1);
        						if (callsign.endsWith("\""))
        							callsign = callsign.substring(0, callsign.length() -1);
        						callsign = callsign + "_b";
        						raw_string = raw_string + callsign + ",";
        						break;
        					case 2:					//TIME
        						if (item.isIntegerValue())
        						{        							
        							//Date time_in = new Date(item.asIntegerValue().getInt()*1000);
        							//setTime(time_in,timerx); 
        							//SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        							int time_in = item.asIntegerValue().getInt();
									int hours = (int)Math.floor(time_in/60*60);
									time_in = time_in - (hours*60*60);
									int mins = (int)Math.floor(time_in/60);
									int secs = time_in-mins*60;
									raw_string = raw_string + String.format("%02d:%02d:%02d",hours,mins,secs) + ",";
        						}
        						break;        						
        					case 1:					//PACKET COUNT
        						if (item.isIntegerValue()){
        							packetID = item.asIntegerValue().getInt();
        							raw_string = raw_string + packetID + ",";
        						}
        						break;        						
        					case 3:					//POSITION
        						if (item.isArrayValue())
        						{
        							if (item.asArrayValue().size() >= 2)
        							{
        								if (item.asArrayValue().get(0).isIntegerValue()
        								 && item.asArrayValue().get(1).isIntegerValue())
        								{
        									coords = new Gps_coordinate(item.asArrayValue().get(0).asIntegerValue().intValue(),
        											item.asArrayValue().get(1).asIntegerValue().intValue());
        								}
        								if (item.asArrayValue().size() >= 3){
	        								if (item.asArrayValue().get(2).isIntegerValue())
	           								{
	        									coords.Set_altitude(item.asArrayValue().get(2).asIntegerValue().getInt());
	           								}
        								}
        								raw_string = raw_string + coords.latitude + "," + coords.longitude + "," + coords.altitude + ",";
        							}
        						}
        						break;
        					case 4:					//SATS
        						
        						break;
        					case 5:        						
        						
        						break;        						
        					case 6:
        						
        						break;
        					default:
        						
        						break;
        					}
        				}
        			     
        			}
        			
        			if (coords != null)
        				coords.latlong_valid = valid_lock;
        			
        			break;
        		}
        	}
        	
        	if (raw_string.length() > 0)
        	{
				raw_string = raw_string + new String(base64enc) + "*";;
        		//raw_string = raw_string.substring(0,raw_string.length()-1) + "*";
        		int crc = calculate_checksum(raw_string,0);
        		raw_string = raw_string + String.format("%04x", crc);
        	}
        	
			//Map<Integer, String> dstMap = unpacker.read(mapTmpl);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static byte[][] gen_telem_mask(byte[] input)
	{
		//generates an expected bit pattern and mask based on callsign and markers
		
		byte[][] output = new byte[2][input.length];
		
		if (input.length < 3)
			return null;
		
		if ( (input[0] & 0xF0) == 0x80 &&
			 (input[1] & 0xFF) == 0x00 &&
			 (input[2] & 0xE0) == 0xA0    )
		{
			int calllen = input[2] & 0x1F;
			
			output[0][0] = input[0];
			output[0][1] = input[1];
			output[0][2] = input[2];
			output[1][0] = (byte) 0xFF;
			output[1][1] = (byte) 0xFF;
			output[1][2] = (byte) 0xFF;
			if (input.length > 4+calllen){
				for (int i = 0; i < calllen + 1; i++)
				{
					output[0][i+3] = input[3+i];
					output[1][i+3] = (byte) 0xFF;
				}
			}
		}
		
		//i began to do something that would work for pretty much everything but settled on one that just does the callsign
		/*
				
		boolean incall = false;
		int infield_len = 0;
		int infield_count = 0;
		boolean inmap = false;
		boolean map_val = false;
		
		
		for (int i = 0; i < input.length; i++)
		{
			if (infield_count == 0)
			{
				if ((input[i] & 0xF0)== 0x80)  //map <16 elements
				{
					output[0][i] =  input[i];
					output[1][i] = (byte) 0xFF;
					inmap = true;
				}
				else if ((input[i] & 0xFF)== 0xde && input.length > i+2)  //map <65536 elements
				{
					output[0][i] =  input[i];
					output[1][i] = (byte) 0xFF;
					output[0][i+1] =  input[i];
					output[1][i+1] = (byte) 0xFF;
					output[0][i+2] =  input[i];
					output[1][i+2] = (byte) 0xFF;
					inmap = true;
					i += 2;
				}
				else if ()
			}
		}
		*/
		return output;
		
	}
	
	private void parse_telem(String str, long timerx, TelemetryConfig tc)
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
			fields = cksplit[0].split("\\,",-1);
		else
			fields = raw_string.split("\\,",-1);
		
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
			
			try
			{
				if (offset == 0)
					packetID = Integer.parseInt(fields[1]);
						
				//handle time			
				String format = "";
				if (fields[2+offset].length() > 6)
					format = "HH:mm:ss";
				else
					format = "HHmmss";				
				SimpleDateFormat ft = new SimpleDateFormat (format);
				ft.setTimeZone(TimeZone.getTimeZone("UTC"));
				Date time_in = ft.parse(fields[2+offset]);	
				
				setTime(time_in,timerx); 
			
				coords = new Gps_coordinate(fields[3+offset],fields[4+offset],fields[5+offset]);
			}
			catch (Exception e)
			{
				System.out.println("Error parsing - " + e.toString());
			}

			
			//now parse extra data
			extraFields = new double[fields.length-1];
			
			for (int j = 6+offset; j < fields.length; j++)
			{
				if (tc == null)
				{
					try
					{
						extraFields[j-1] = Double.parseDouble(fields[j]);
					}
					catch (Exception e)
					{
						
					}
				}
				else if (tc.getTotalFields() <= j-1)
				{
					try
					{
						extraFields[j-1] = Double.parseDouble(fields[j]);
					}
					catch (Exception e)
					{
						
					}
				}
				else
				{
					if (tc.getFieldDataType(j-1) == TelemetryConfig.DataType.FLOAT)
					{
						try
						{
							extraFields[j-1] = Double.parseDouble(fields[j]);
						}
						catch (Exception e)
						{
							
						}
					}
					else if (tc.getFieldDataType(j-1) == TelemetryConfig.DataType.INT)
					{
						try
						{
							extraFields[j-1] = (double)Integer.parseInt(fields[j]);
						}
						catch (Exception e)
						{
							
						}
					}
				}
			}		
		}
	}
	
	public boolean getExtraFieldExists(int index)
	{
		if (extraFields == null)
			return false;
		if (!(index < extraFields.length))
			return false;
		return true;
	}
	
	public double getExtraFields(int index)
	{
		if (index < 0)
			return 0;
		if (extraFields == null)
			return 0;
		if (!(index < extraFields.length))
			return 0;
		return extraFields[index];
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
	
	public static int calculate_checksum(String in, int start)
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
		return crc & 0xFFFF;
	}
	
	public static boolean check_checksum(String in, int start)
	{
		int i;
		int crc = calculate_checksum(in,start);
		
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
	
	private void setTime(Date time_in, long timerx)
	{
		
		try //this is all a bit horrible :(
		{
			
						
			//get time rx @ 12am
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

				
			cal2.setTime(time_in);
			
			cal.setTimeInMillis(timerx*1000);
			cal.set(Calendar.HOUR_OF_DAY, cal2.get(Calendar.HOUR_OF_DAY));
			cal.set(Calendar.MINUTE, cal2.get(Calendar.MINUTE));
			cal.set(Calendar.SECOND, cal2.get(Calendar.SECOND));
			
			//long best_guess = cal2.getTimeInMillis();
			
					

			if (cal.getTimeInMillis() < timerx*1000 - 1*60*60*1000)
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
	}
	
	public boolean isZeroGPS(){
		if (coords == null)
			return true;
		return (coords.latitude == 0.0 || coords.longitude == 0.0);
	}

}
