package rtty;

import java.util.ArrayList;
import java.util.List;

public class Binary_frame_handler {

	protected ArrayList<StringRxEvent>  _listeners = new ArrayList<StringRxEvent>() ;
	
	private List<Extractor> extractors = new ArrayList<Extractor>();
	int max_extractors = 10;
	
	private boolean _last_valid = false;
	//private double[] _sync_search_buff = null;
	//private int _sync_search_ptr = 0;
	private boolean[] _sync_pattern = null;
	
	int _max_buff = 6000;
	private double[] rx_buffer = new double[_max_buff];
	int rx_buff_ptr = 0;    //points to last inserted item
	
	public Binary_frame_handler(boolean[] sync_pattern) {
		setSync(sync_pattern);
	}
	
	public void addStringRecievedListener(StringRxEvent listener)
	{	
		_listeners.add(listener);
	}
	
	protected void fireStringReceived(String str, boolean checksum)
	{
		
		for (int i = 0; i < _listeners.size(); i++)
		{
			_listeners.get(i).StringRx(str,checksum);
		}
	}

	public boolean get_last_valid() {
		// TODO Auto-generated method stub
		return _last_valid;
	}
	
	public void setSync(boolean[] pattern)
	{
		_sync_pattern = pattern;
	}

	
	public String bits2chars(double[] bits) {
		// TODO Auto-generated method stub
		String out_buff = "";
		
		int score = 0;
		
		int old_ptr = rx_buff_ptr;
		
		//put new data onto buffer
		for (int i = 0; i < bits.length; i++)
		{
			rx_buff_ptr++;
			if (rx_buff_ptr >= _max_buff)
				rx_buff_ptr = 0;
			rx_buffer[rx_buff_ptr] = bits[i];
		}
		
		//look for sync sequence
		int search_start = old_ptr;
		for (int i = 0; i < bits.length; i++)
		{
			search_start++;
			if (search_start >= _max_buff)
				search_start = 0;
			int cmp = compare_sync(search_start);
			score = Math.max(score,cmp);
			if (cmp > 400) //change
			{
				if (extractors.size() < max_extractors)
					extractors.add(new Extractor(search_start));
				else
					System.out.println("Warning: Unable to create new extractor");
			}
		}
		
		//go through and run each extractor
		for (int i = 0; i < extractors.size(); i++)
		{
			if (extractors.get(i).process(old_ptr, rx_buff_ptr) > rx_buffer.length)
				extractors.remove(i);
		}
		
		out_buff = out_buff +" " +score;
		if (score == _sync_pattern.length)
			out_buff = out_buff + "!!";
		return out_buff;
	}
	
	private int compare_sync(int last_added)
	{
		int count=0;

		
		for (int i = _sync_pattern.length-1; i >= 0; i--)
		{
			if (_sync_pattern[i] == rx_buffer[last_added]>0)
				count++;
			last_added--;
			if (last_added < 0)
				last_added = rx_buffer.length-1;
		}
		
		return count;
	}
	
	
	class Extractor
	{
		int _cycles_active = 0;
		int _ptr_post_sync = 0;
		
		public Extractor(int ptr_end_sync)
		{
			_ptr_post_sync = ptr_end_sync;
			_ptr_post_sync++;
			if (_ptr_post_sync > _max_buff)
				_ptr_post_sync = 0;
		}
		
		public int process(int last_ptr, int new_ptr)
		{

			int ptr = last_ptr;
			
			do
			{
				ptr++;
				if (ptr >= _max_buff)
					ptr = 0;
				
				
				_cycles_active++;
			} while(ptr != new_ptr);

			
			
			return _cycles_active;
		}
		
	}
	
}
