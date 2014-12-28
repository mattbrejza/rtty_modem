// Copyright 2014 (C) Matthew Brejza
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


package com.brejza.matt.habmodem;

import java.util.UUID;

public class LoraBsGattAttributes {

	//services
	public static UUID UUID_LORA_BS_SERVICE = UUID.fromString("0000200A-FFFF-1000-8000-52004d7a1d0b");
	public static UUID UUID_BATTERY_SERVICE = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
	
	//characteristics
	public static UUID UUID_TELEMETRY_STRING_BINARY = UUID.fromString("00001500-FFFF-1000-8000-52004d7a1d0b");
	public static UUID UUID_TELEMETRY_STRING_ASCII  = UUID.fromString("00001501-FFFF-1000-8000-52004d7a1d0b");
	
	public static UUID UUID_TELEMETRY_MSGPACK0_CALLSIGN = UUID.fromString("10001000-FFFF-1000-8000-52004d7a1d0b");
	public static UUID UUID_TELEMETRY_MSGPACK1_TIME     = UUID.fromString("10001001-FFFF-1000-8000-52004d7a1d0b");
	
}
