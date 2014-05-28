package rtty;

import java.util.ArrayList;
import java.util.List;

public class Binary_frame_handler {

	protected ArrayList<StringRxEvent>  _listeners = new ArrayList<StringRxEvent>() ;
	
	private List<Extractor> extractors = new ArrayList<Extractor>();
	int max_extractors = 10;
	
	private int[] lengths = new int[]{0,40, 64, 88, 112, 136, 160, 184, 208, 232, 256, 280, 304, 328, 352, 376, 400, 424, 448, 472, 496, 528, 576, 624, 672, 720, 768, 816, 864, 912, 960, 1008, 1088, 1184, 1280, 1376, 1472, 1568, 1664, 1760, 1856, 1952, 2048, 2240, 2432, 2624, 2816, 3008, 3200, 3392, 3584, 3776, 3968, 4160, 4352, 4544, 4736, 4928, 5120, 5312, 5504, 5696, 5888, 6144};
	
	private boolean _last_valid = false;
	//private double[] _sync_search_buff = null;
	//private int _sync_search_ptr = 0;
	private boolean[] _sync_pattern = null;
	
	private Extractor primary_extractor = null;
	private int last_length = -1;
	
	int _max_buff = 6000;
	private double[] rx_buffer = new double[_max_buff];
	int rx_buff_ptr = 0;    //points to last inserted item
	
	Turbo_decoder tdec = new Turbo_decoder();
	
	public Binary_frame_handler(boolean[] sync_pattern) {
		setSync(sync_pattern);
	}
	
	public void addStringRecievedListener(StringRxEvent listener)
	{	
		_listeners.add(listener);
	}
	
	
	protected void fireStringReceived(byte[] str, boolean checksum, int length)
	{
		last_length = length;
		for (int i = 0; i < _listeners.size(); i++)
		{
			_listeners.get(i).StringRx(str,checksum, length);
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
			rx_buffer[rx_buff_ptr] = -bits[i];
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
			if (cmp > 29) //change
			{
				out_buff = out_buff + " <sync>";
				if (extractors.size() < max_extractors){
					extractors.add(new Extractor(search_start));
					primary_extractor = extractors.get(extractors.size()-1);
					if (last_length > 0)
						extractors.add(new Extractor(search_start,last_length));
				}
				else
					System.out.println("Warning: Unable to create new extractor");
			}
		}
		
		//go through and run each extractor
		boolean remove_all = false;  //set if one extractor indicates it has a valid string
		for (int i = 0; i < extractors.size(); i++)
		{
			int r = extractors.get(i).process(old_ptr, rx_buff_ptr);
			//if (primary_extractor == extractors.get(i))
			//{
			//	out_buff = out_buff + primary_extractor.
			//}
			//System.out.print(extractors.get(i).last_string);
			if (r  > rx_buffer.length || r < 0)
				extractors.remove(i);
			if (r == -2){
				remove_all = true;
				break;
			}
		}
		if (remove_all)
		{
			while(extractors.size() > 0)
				extractors.remove(0);
		}
		
		if (primary_extractor != null)
		{
			out_buff = out_buff + primary_extractor.last_string;
			primary_extractor.last_string = "";
			if (remove_all)
				out_buff = out_buff + "\n";
		}
			
		
		//out_buff = out_buff +" " +score;
		//if (score == _sync_pattern.length)
		//	out_buff = out_buff + "!!";
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
		int internal_interleaver_len = -1;
		int systematic_start = -1;
		int systematic_end = -1;
		int parity_start = -1;
		int parity_0_end = -1;
		int parity_1_end = -1;
		int parity_2_end = -1;
		int parity_3_end = -1;
		
		int partial_byte = 0;
		int partial_byte_mask = 0x80;
		
		String last_string = "";
		
		
		public Extractor(int ptr_end_sync)
		{
			_ptr_post_sync = ptr_end_sync;
			_ptr_post_sync++;
			if (_ptr_post_sync > _max_buff)
				_ptr_post_sync = 0;
		}
		
		public Extractor(int ptr_end_sync, int length)
		{
			_ptr_post_sync = ptr_end_sync;
			_ptr_post_sync++;
			if (_ptr_post_sync > _max_buff)
				_ptr_post_sync = 0;
			internal_interleaver_len = length;
			calcuate_addresses();
		}
		
		public int process(int last_ptr, int new_ptr)
		{

			int ptr = last_ptr;
			last_string = "";
			do
			{
				ptr++;
				if (ptr >= _max_buff)
					ptr = 0;
				
				int since_sync = ptr - _ptr_post_sync;
				if (since_sync < 0)
					since_sync = since_sync + _max_buff;
				
				
				//add this bit to the output buffer		
				if (since_sync >= 0 && since_sync < 5500)
				{
					if (rx_buffer[ptr] > 0)
						partial_byte |= partial_byte_mask;
					partial_byte_mask >>= 1;
					if (partial_byte_mask == 0)
					{
						partial_byte_mask = 0x80;
						
						last_string = last_string + " " + toHexString(partial_byte);
						partial_byte = 0;
					}
				}
					
				
				if (since_sync == 2*8-1)  //get length
				{
					double[] size_f = buffer_copy(_ptr_post_sync,ptr);
					boolean[] sf = new boolean[size_f.length];
					int addr = 0;
					for (int i = 0; i < size_f.length; i++)    //deinterleave length
					{
						sf[addr] = size_f[i] > 0;
						addr = addr + 8;
						if (addr > 15){
							addr = addr - 16;
							addr += 1;
							if (addr >= 8)
								addr -= 8;
						}						
					}
					
					byte hamming1 = 0;
					byte mask = 1;
					for (int i = 0; i < 8; i++)
					{
						if (sf[i])
							hamming1 |= mask;
						mask <<= 1;
					}
					
					byte hamming2 = 0;
					mask = 1;
					for (int i = 8; i < 16; i++)
					{
						if (sf[i])
							hamming2 |= mask;
						mask <<= 1;
					}
					Turbo_decoder.hamming_decode84(hamming1);
					Turbo_decoder.hamming_decode84(hamming2);
					internal_interleaver_len = lengths[((hamming1&0xC) >> 2) + ((hamming2&0xF) << 2)];
					System.out.println("length: " + internal_interleaver_len);
					last_string = last_string + " <L:" + internal_interleaver_len + ">";
					//internal_interleaver_len = 40; //////////////////////////////change
					
					calcuate_addresses();
					
				}
				else if (since_sync == systematic_end)   //get systematic bits
				{
					double[] systematic = Turbo_decoder.systematic_subblock_deinterleave(
							buffer_copy(_ptr_post_sync+systematic_start,ptr));
					
					boolean[] sys_bits = new boolean[internal_interleaver_len];
					
					for (int i = 0; i < internal_interleaver_len; i++)
						sys_bits[i] = systematic[i]>0 ? true:false;
					
					if (Turbo_decoder.check_checksum(sys_bits))
					{
						System.out.println("checksum passed");
						//return -2;
					}
					else
					{
						System.out.println("checksum failed");
					}
					
				}
				else if (since_sync == parity_0_end 
						|| since_sync == parity_1_end 
						|| since_sync == parity_2_end 
						|| since_sync == parity_3_end)
				{
					double[] bits = new double[since_sync - parity_start + 1 + internal_interleaver_len+4];
								
					//copy the relevent bits into the buffer
					int i = _ptr_post_sync + systematic_start;
					int j = 0;
					int end_ptr = _ptr_post_sync + systematic_end;
					if (end_ptr >= _max_buff)
						end_ptr = end_ptr - _max_buff;
					while(i != end_ptr )
					{
						bits[j] = rx_buffer[i];
						j++;
						i++;
						if (i >= _max_buff)
							i = 0;
					}
					bits[j] = rx_buffer[i];
					j++;
					i = _ptr_post_sync + parity_start;
					if (i >= _max_buff)
						i = i - _max_buff;
					while(i != ptr )
					{
						bits[j] = rx_buffer[i];
						j++;
						i++;
						if (i >= _max_buff)
							i = 0;
					}
					bits[j] = rx_buffer[i];
					
					
					double[][] v = Turbo_decoder.output_rate_dematching(bits,  internal_interleaver_len+4);
					boolean[] out = tdec.decode(v[0], v[1], v[2], true, true);
					if (tdec.last_success)
					{
						System.out.println("turbo: checksum passed");
						fireStringReceived(toByteArray(out), true,internal_interleaver_len);
						return -2;
					}
					else
					{
						System.out.println("turbo: checksum failed");
					}
					if (since_sync == parity_3_end){						
						last_string = last_string + " <Pend>";
						return -1;
					}
				}
				
					
				_cycles_active++;
			} while(ptr != new_ptr);

			
			
			
			
			
			return _cycles_active;
		}
		
		private String toHexString(int input)
		{
			String output = "";
			output = Integer.toHexString(input);
			if (output.length() == 1)
				output = "0"+output;
			return output;
		}
		
		private void calcuate_addresses()
		{
			parity_start = 2*8 +  internal_interleaver_len + 4 + 4 + 8; //(int) (Math.ceil((double)(3*8 + internal_interleaver_len + 4)/8)*8);					
			systematic_end = 2*8-1 + internal_interleaver_len + 4;
			systematic_start = 2*8;
			
		//	parity_0_end = parity_start + (internal_interleaver_len + 4)/4 - 1;
		//	parity_1_end = parity_start + (internal_interleaver_len + 4)*7/13 - 1;
		//	parity_2_end = parity_start + (internal_interleaver_len + 4) - 1;
			parity_3_end = parity_start + (internal_interleaver_len + 4)*2 - 1;
		}
		
		byte[] toByteArray(boolean[] in)
		{
			byte[] out = new byte[(int)Math.ceil((double)in.length/8)];
			int mask = 0x80;
			int j = 0;
			for (int i = 0; i < in.length; i++)
			{
				if (in[i])
					out[j] |= mask;
				mask >>= 1;
				if (mask == 0){
					mask = 0x80;
					j++;
				}
			}
			
			return out;
		}
		
		//inclusive of start and end
		double[] buffer_copy(int start, int end)
		{
			int len = end - start+1;
			if (len < 0)
				len = len + _max_buff;
			double[] out = new double[len];
			
			int i = start;
			int j = 0;
			
			while(i != end)
			{
				out[j] = rx_buffer[i];
				j++;
				i++;
				if (i >= _max_buff)
					i = 0;
			}
			out[j] = rx_buffer[i];
		
			return out;
		}
	}
	
}
