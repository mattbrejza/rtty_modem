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


package rtty;

import javax.swing.event.EventListenerList;

public class Telemetry_handler {

	private boolean in_str=false;
	private String telem_buff = "";
	
    protected EventListenerList  _listeners = new EventListenerList() ;
	
	public Telemetry_handler() {
		// TODO Auto-generated constructor stub
	}
	
	public void addStringRecievedListener(StringRxEvent listener)
	{	
		_listeners.add(StringRxEvent.class, listener);
	}

	public void clearBuff()
	{
		telem_buff = "";
		in_str = false;
	}
	
	public boolean ExtractPacket (String str)
	{
		boolean out = false;
		int i = 0;
		int j=0;
		while ( i >= 0)
		{
			if (in_str)  //if in string
			{
				i=str.indexOf(10,j);
				if (i < 0)
				{
					i=str.indexOf(13,j);
					if (i<0)
					{
						i=str.indexOf((int)'$',j);
						if (!(((i-j) > 10) || telem_buff.length() > 10))
							i=-1;
					}
				}
				
				if (i<0)	//end of string not found, save input to buffer and wait until next time
				{
					telem_buff = telem_buff + str.substring(j,str.length());
				}
				else        //found string, add to what we already have and send out
				{
					telem_buff = telem_buff + str.substring(j,i+1);
					if (_listeners.getListenerList().length > 0)
					{
						boolean ck = check_checksum(telem_buff,0);
						Telemetry_string ts = new Telemetry_string(telem_buff,ck);    //TODO: consider remove from here
						fireStringReceived(ts, ck);
						
						out = ck;
					}
						
					telem_buff = "";
					in_str = false;
					j=i+1;
				}
			}
			else   //look for the start of a string ($)
			{
				i=str.indexOf((int)'$',j);
				if (i >= 0)
				{
					in_str = true;
					telem_buff = "";
					j=i+1;
				}	
			
			}
		}		
		return out;
	}
	
	protected void fireStringReceived(Telemetry_string str, boolean checksum)
	{
		Object[] listeners = _listeners.getListenerList();
		for (int i =listeners.length-2; i>=0; i-=2)   //urgh, why does java have to make this so horrible
		{			
			((StringRxEvent)listeners[i+1]).StringRx(str,checksum);
		}
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
	
}
