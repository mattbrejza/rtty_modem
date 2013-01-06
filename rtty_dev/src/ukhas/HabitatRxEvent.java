package ukhas;

import java.util.EventListener;
import java.util.List;

public interface HabitatRxEvent extends EventListener
{
	void HabitatRx(List<String> data, boolean success, String callsign, long startTime, long endTime);
}