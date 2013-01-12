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

public class Gps_coordinate {

	public double latitude = 0;
	public double longitude = 0;
	public boolean latlong_valid = false;
	
	public double altitude = 0;
	public boolean alt_valid = false;
	
	public Gps_coordinate(double lat, double longi, double alt) {
		Set_altitude(alt);
		Set_decimal(lat, longi);
	}
	
	public Gps_coordinate(String lat, String longi, String alt)
	{
		Set_str(lat,longi);
		Set_altitude(alt);
		
	}
	
	//uses meters
	public void Set_altitude(double alt)
	{
		if (alt > -500 && alt < 50000000)
		{
			altitude = alt;
			alt_valid = true;
		}
		else
		{
			alt_valid = false;
		}
	}
	
	public void Set_altitude(String alt)
	{
		try
		{
			double a = Double.parseDouble(alt);
			Set_altitude(a);
		}
		catch (Exception e)
		{
			alt_valid = false;
		}
		
	}
	
	public void Set_str(String lat, String longi)
	{
		
		//different formats:
		//NMEA:    (-)DDMM.mmm...     (-)DDDMM.mmm...
		//decimal: (-)(D)D.ddd...       (-)(DD)D.ddd...
		//int:     something else, no decimal places

		int offset_lat=0;
		int offset_long=0;
		
		if (lat.length() < 3 || lat.length() < 3)
		{
			latlong_valid = false;
			return;
		}
		
		if (lat.charAt(0) == '-')
			offset_lat = 1;
		
		if (longi.charAt(0) == '-')
			offset_long = 1;
		
		int i,j;
		
		i = lat.indexOf('.');
		j = longi.indexOf('.');
		if (i < 0 || j < 0)   //TODO: unimplemented format
		{
			latlong_valid = false;
			return;
		}
		
		try
		{
		
			if (i == 4+offset_lat) //NMEA
			{
				if (j == 5 + offset_long)  //confirmed NMEA
				{
					int la1 = Integer.parseInt(lat.substring(0, offset_lat+2));    //get the (-)DD part
					int lo1 = Integer.parseInt(longi.substring(0, offset_long+3)); //get the (-)DDD part
					double la2 = Double.parseDouble(lat.substring(offset_lat+2,lat.length()));       //get the MM.mmmm part
					double lo2 = Double.parseDouble(longi.substring(offset_long+3,longi.length()));  //get the MM.mmmm part
	
					if (la1 < 0)
						la2 = -1 * la2;
					if (lo1 < 0)
						lo2 = -1 * lo2;
					
					Set_decimal((double)la1 + la2/60,(double)lo1 + lo2/60);
				}
				else
				{
					latlong_valid = false;
					return;
				}
			}
			else if (i > 4+offset_lat || j > 5+offset_lat)   //junk
			{
				latlong_valid = false;
				return;
			}
			else    //decimal
			{
				Set_decimal(Double.parseDouble(lat), Double.parseDouble(longi));
			}
		}
		catch (Exception e)
		{
			latlong_valid = false;
			return;
		}
		
			
		
	}
		
	public void Set_decimal(double lat, double longi)
	{
		if (verify(lat,longi))
		{
			latitude = lat;
			longitude = longi;
			latlong_valid = true;			
		}
		else
			latlong_valid = false;
		
	}
	
	public void Set_NMEA(String lat, String longi)
	{
		
	}
	
	public void Set_NMEA(String lat, String longi, char NS, char EW)
	{
		
	}
	
	private boolean verify(double lat, double longi)
	{
		if (lat < 90 && lat > -90 && longi > -180 && longi < 180)
			return true;
		else
			return false;
	}

}
