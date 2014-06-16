package rtty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Binary_frame_handler {

	protected ArrayList<StringRxEvent>  _listeners = new ArrayList<StringRxEvent>() ;
	
	private List<Extractor> extractors = new ArrayList<Extractor>();
	int max_extractors = 10;
	
	private int[] lengths = new int[]{40,40, 64, 88, 112, 136, 160, 184, 208, 232, 256, 280, 304, 328, 352, 376, 400, 424, 448, 472, 496, 528, 576, 624, 672, 720, 768, 816, 864, 912, 960, 1008, 1088, 1184, 1280, 1376, 1472, 1568, 1664, 1760, 1856, 1952, 2048, 2240, 2432, 2624, 2816, 3008, 3200, 3392, 3584, 3776, 3968, 4160, 4352, 4544, 4736, 4928, 5120, 5312, 5504, 5696, 5888, 6144};
	
	private boolean _last_valid = false;
	//private double[] _sync_search_buff = null;
	//private int _sync_search_ptr = 0;
	private boolean[] _sync_pattern = null;
	
	private Extractor primary_extractor = null;
	private int last_length = -1;
	
	int _max_buff = 6000;
	private double[] rx_buffer = new double[_max_buff];
	int rx_buff_ptr = 0;    //points to last inserted item
	
	Map <String,boolean[]> data_sync_map = new HashMap<String, boolean[]>();
	Map <String,boolean[]> mask_sync_map = new HashMap<String, boolean[]>();
	Map <String,Integer>   d_len_sync_map = new HashMap<String, Integer>();
	
	Turbo_decoder tdec = new Turbo_decoder();
	
	public Binary_frame_handler(boolean[] sync_pattern) {
		setSync(sync_pattern);
	}
	
	public void addStringRecievedListener(StringRxEvent listener)
	{	
		_listeners.add(listener);
	}
	
	
	protected void fireStringReceived(byte[] str, boolean checksum, int length, int flags)
	{
		last_length = length;
		for (int i = 0; i < _listeners.size(); i++)
		{
			_listeners.get(i).StringRx(str,checksum, length, flags);
		}
	}
	
	public void provide_binary_sync_helper(byte[] data, byte[] mask, String id, int len)
	{
		//TODO: add start sync and len fields
		
		boolean[] data_b = new boolean[len+4];
		boolean[] mask_b = new boolean[len+4];
		
		int m = 0x80;
		int j = 0;
		
		//place into boolean buffer
		for (int i = 0; i < len; i++)
		{
			
			if ((data[j] & m) > 0)
				data_b[i] = true;
			if ((mask[j] & m) > 0)
				mask_b[i] = true;
			m >>= 1;
			
			if (m == 0){
				m = 0x80;
				j++;
			}			
		}
		
		//do interleaving
		int D = len + 4;
		int rowTcSb = (int) Math.ceil((double)D/32);
		int Nd = 32*rowTcSb - D;
		int[] d0 = Turbo_encoder.subBlockInterleaver1(data_b,32,rowTcSb,Nd);
		int[] m0 = Turbo_encoder.subBlockInterleaver1(mask_b,32,rowTcSb,Nd);
		int i = 0;
		int k = 0;
		
		boolean[] out_data = new boolean[len + 4 + 16];
		boolean[] out_mask = new boolean[len + 4 + 16];
		
		k = 16;
		while (k < (len + 4 + 16) && i < d0.length)
		{			
			if (d0[i] >= 0)
			{
				if (d0[i] == 0)
					out_data[k] = false;
				else
					out_data[k] = true;
				if (m0[i] == 0)
					out_mask[k] = false;
				else
					out_mask[k] = true;
				k++;
			}
			i++;			
		}
		
		//add on length field   TODO: hamming stuff
		int len_f = java.util.Arrays.binarySearch(lengths, len);
		if (len_f > 0)
		{
			len_f = len_f << 2;
			len_f |= (len_f<<4) & 0xF00;
			len_f &= 0xF0F;
			int len_m =0xF0C;
			//interleave length field
			int addr = 0;
			for (i = 0; i < 16; i++)
			{
				if ((len_f & (1<<addr))>0)
					out_data[i] = true;
				if ((len_m & (1<<addr))>0)
					out_mask[i] = true;
				addr += 8;
				if (addr > 15){
					addr -= 16;
					addr++;
					if (addr >= 8)
						addr -=8;
				}			
			}
		}
		
		if (data_sync_map.containsKey(id))		
			data_sync_map.remove(id);
		if (mask_sync_map.containsKey(id))		
			mask_sync_map.remove(id);
		if (d_len_sync_map.containsKey(id))		
			d_len_sync_map.remove(id);
		data_sync_map.put(id, out_data);
		mask_sync_map.put(id, out_mask);		
		d_len_sync_map.put(id, new Integer(len+4+16));
		
	}

	public boolean get_last_valid() {
		return _last_valid;
	}
	
	public void setSync(boolean[] pattern)
	{
		_sync_pattern = pattern;
	}

	
	public String bits2chars(double[] bits) {

		String out_buff = "";
		

		
		int old_ptr = rx_buff_ptr;
		
		//put new data onto buffer
		for (int i = 0; i < bits.length; i++)
		{
			rx_buff_ptr++;
			if (rx_buff_ptr >= _max_buff)
				rx_buff_ptr = 0;
			rx_buffer[rx_buff_ptr] = -bits[i];
		}
		
		
		
		//go through and run each extractor
		boolean remove_all = false;  //set if one extractor indicates it has a valid string
		for (int i = 0; i < extractors.size(); i++)
		{
			int r = extractors.get(i).process(old_ptr, rx_buff_ptr);
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
		else
		{
			//look for sync based on partial packets
			int search_start = old_ptr;
			for (int i = 0; i < bits.length; i++)
			{
				search_start++;
				if (search_start >= _max_buff)
					search_start = 0;
				
				for (Map.Entry<String, boolean[]> entry : data_sync_map.entrySet())
				{
				    			
					double cmp = compare_sync(search_start,entry.getValue(),mask_sync_map.get(entry.getKey()));
	
					if (cmp > 0.75) //change
					{
						out_buff = out_buff + " <sync p>";
						System.out.print("sync partial (" + cmp + ")  ");
						
						//calculate start address
						int ptr_end_sync = search_start - d_len_sync_map.get(entry.getKey());
						int ptr_post_sync = ptr_end_sync + 1;
						if (ptr_end_sync < 0)
							ptr_end_sync += _max_buff;
						if (ptr_post_sync < 0)
							ptr_post_sync += _max_buff;
						
						//check there isnt already an extractor with this start address
						boolean exist = false;
						for (int j = 0; j < extractors.size(); j++)
						{
							if (extractors.get(j)._ptr_post_sync == ptr_post_sync){ 
								exist = true;
								extractors.get(j).flags_set_got_packet_sync();
							}
						}
						
						//if new string detected, add to list
						if (exist == false)
						{
							out_buff = out_buff + " <sync p>";
							System.out.println("new");
							Extractor e = null;
							if (extractors.size() < max_extractors){
								e = new Extractor(ptr_end_sync);
								e.process(ptr_end_sync, rx_buff_ptr);
								e.flags_set_got_packet_sync();
								extractors.add(e);
								primary_extractor = extractors.get(extractors.size()-1);
								if (last_length > 0){
									e = new Extractor(ptr_end_sync,last_length);
									e.process(ptr_end_sync, rx_buff_ptr);
									e.flags_set_got_packet_sync();
									extractors.add(e);
								}
							}
							else
								System.out.println("Warning: Unable to create new extractor"); 
						}
						else
							System.out.println("not new");
						
					
					}
				}
			}
		}
		
		//look for sync sequence	
		int search_start = old_ptr;
		search_start = old_ptr;
		for (int i = 0; i < bits.length; i++)
		{
			search_start++;
			if (search_start >= _max_buff)
				search_start = 0;
			int cmp = compare_sync(search_start);
			if (cmp > 29) //change
			{
				out_buff = out_buff + " <sync>";
				Extractor e = null;
				if (extractors.size() < max_extractors){
					e = new Extractor(search_start);
					e.process(old_ptr, rx_buff_ptr);
					e.flags_set_got_sync();
					extractors.add(e);
					primary_extractor = extractors.get(extractors.size()-1);
					if (last_length > 0){
						e = new Extractor(search_start,last_length);
						e.process(old_ptr, rx_buff_ptr);
						e.flags_set_got_sync();
						extractors.add(e);
					}
				}
				else
					System.out.println("Warning: Unable to create new extractor");
			}
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
	
	private double compare_sync(int last_added, boolean[] pattern, boolean[] mask)
	{
		int count=0;
		int total = 0;
		
		for (int i = pattern.length-1; i >= 0; i--)
		{
			if (mask[i])
			{
				total++;
				if (pattern[i] == rx_buffer[last_added]>0)
					count++;
			}
			last_added--;
			if (last_added < 0)
				last_added = rx_buffer.length-1;
		}
		
		return (double)count/(double)total;
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
		private int parity_counter = 0;
		
		int flags = 0;
		
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
			flags_set_preset_len();
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
					Turbo_decoder.hamming_decode84(hamming1);  //TODO: this
					Turbo_decoder.hamming_decode84(hamming2);
					if (internal_interleaver_len < 0)
						internal_interleaver_len = lengths[((hamming1&0xC) >> 2) + ((hamming2&0xF) << 2)];
					System.out.println("length: " + internal_interleaver_len);
					last_string = last_string + " <L:" + internal_interleaver_len + ">";
					//internal_interleaver_len = 40; //////////////////////////////change
					
					calcuate_addresses();
					
				}
				else if (since_sync == systematic_end)   //get systematic bits
				{
					double[] systematic = Turbo_decoder.systematic_subblock_deinterleave(
							buffer_copy(wrap(_ptr_post_sync+systematic_start),ptr));
					
					boolean[] sys_bits = new boolean[internal_interleaver_len];
					
					for (int i = 0; i < internal_interleaver_len; i++)
						sys_bits[i] = systematic[i]>0 ? true:false;
					
					last_string = last_string + " <P>";
						
					if (Turbo_decoder.check_checksum(sys_bits))
					{
						flags_set_no_parity_needed();
						System.out.println("checksum passed");
						flags_set_parity_needed(0);
						fireStringReceived(toByteArray(sys_bits), true,internal_interleaver_len,flags);
						return -2;
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
					int i = wrap(_ptr_post_sync + systematic_start);
					int j = 0;
					int end_ptr = wrap(_ptr_post_sync + systematic_end);
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
					i = wrap(_ptr_post_sync + parity_start);
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
						last_string = last_string + " <fixed: " + tdec.last_fixed + ">";
						flags_set_parity_needed(parity_counter);
						fireStringReceived(toByteArray(out), true,internal_interleaver_len,flags);
						return -2;
					}
					else
					{
						parity_counter++;
						System.out.println("turbo: checksum failed");
					}
					if (since_sync == parity_3_end){						
						last_string = last_string + " < :( >";
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
			
			parity_0_end = parity_start + (internal_interleaver_len + 4)/4 - 1;
			parity_1_end = parity_start + (internal_interleaver_len + 4)*7/13 - 1;
			parity_2_end = parity_start + (internal_interleaver_len + 4) - 1;
			parity_3_end = parity_start + (internal_interleaver_len + 4)*2 - 1;
		}
		
		private int wrap(int in)
		{
			if (in < 0)
				in += _max_buff;
			if (in >= _max_buff)
				in -= _max_buff;
			return in;
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
			
			if (start >=_max_buff )
				start = start - _max_buff;
			
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

		void flags_set_got_sync()
		{
			flags |= (1<<0);
		}
		void flags_set_got_packet_sync()
		{
			flags |= (1<<1);
		}
		void flags_set_no_parity_needed()
		{
			flags |= (1<<2);
		}
		void flags_set_parity_needed(int i)
		{
			flags |= ((i&0x3)<<3);
		}
		void flags_set_preset_len()
		{
			flags |= (1<<5);
		}
	
	}
	
}
