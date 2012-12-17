package rtty;

import java.util.EventListener;

public interface StringRxEvent extends EventListener
{
	void StringRx(String strrx, boolean checksum);
}
