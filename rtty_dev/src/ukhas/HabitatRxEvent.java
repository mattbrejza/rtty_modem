package ukhas;

import java.util.EventListener;
import java.util.TreeMap;

public interface HabitatRxEvent extends EventListener
{
	void HabitatRx(TreeMap<Long,Telemetry_string> data, boolean success, String callsign, long startTime, long endTime);
}