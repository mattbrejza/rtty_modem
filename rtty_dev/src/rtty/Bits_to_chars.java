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


//currently assumes strings begin with $$ when looking for the start of string
// -should be able to change the start sequence


package rtty;

public class Bits_to_chars {

    //bits2char
    private int _bit2char_i=0;   //counter for number of bits into char
    private boolean _bit2char_last_bit = false; //last bit in a window (used in bits2char)
    private int _bitmask=0;      //idle/active state of bits2char
    private int _current_char=0; //current working value for bits2char
    private boolean _next_start = false;  //when high the next bit should be a start bit
	private int _data_bits = 7;
	private int _stop_bits=1;
	private double _average_stop_bits;
	private int _bits_since_last=0;
	private int _total_average=0;
	
	private int _previous_bits;
	private int _search_pattern;
	private int _search_mask;
	private int _search_len;
	
	private int _bit_grouping_total_50 = 0;
	private int _bits_count = 0;
	private int _this_grouping = 0;
	private boolean _last_bit = false;
	
	public enum Method { WAIT_FOR_START_BIT, FIXED_POSITION };
	
	public Method _decoding_method = Method.WAIT_FOR_START_BIT;
	
	public double average_stop_bits()
	{
		return _average_stop_bits/_total_average;
	}
	
	public Bits_to_chars(int default_data_bits, Method default_method) {
		// TODO Auto-generated constructor stub
		_decoding_method = default_method;
		_data_bits = default_data_bits;
		_update_pattern();
				
	}
	public Bits_to_chars(int default_data_bits, int default_stops, Method default_method) {
		// TODO Auto-generated constructor stub
		_decoding_method = default_method;
		_data_bits = default_data_bits;
		_stop_bits = default_stops;
		_update_pattern();
				
	}

	public void DataBits (int bits)
	{
		_data_bits = bits;
		_update_pattern();
	}
	
	public void StopBits (int stops)
	{
		_stop_bits = stops;
		_update_pattern();
	}
	
	public double Average_bit_period()
	{
		if (_bits_count > 0)
			return _bit_grouping_total_50/_bits_count;
		else
			return 0;
	}
	
	private void _update_pattern()
	{
		if (_stop_bits >=5)
			return ;
		if (_data_bits > 8 || _data_bits < 7)
			return ;
		
		int backwardsdollar;
		if (_data_bits == 8)
			backwardsdollar = 36;
		else
			backwardsdollar = 18;
		
		_search_pattern = backwardsdollar;   //1 start bit then $		
		
		for (int i = 0; i < _stop_bits; i++)
		{
			_search_pattern = _search_pattern << 1;
			_search_pattern += 1;   //the stop bits
		}
		
		_search_pattern = _search_pattern << (1+_data_bits);
		_search_pattern += backwardsdollar;  //1 start bit then $
		
		for (int i = 0; i < _stop_bits; i++)
		{
			_search_pattern = _search_pattern << 1;
			_search_pattern += 1;   //the stop bits
		}
		
		_search_mask = (1 << (1+_data_bits+_stop_bits+1+_data_bits+_stop_bits)) - 1 ;
		_search_len = (1+_data_bits+_stop_bits+1+_data_bits+_stop_bits);
	}

	private String bits2chars_fixed (boolean[] input)
	{
	//TODO: THIS!
		String out = "";
		
		if (input.length < 1)
		{
			System.out.println("bits2chars: 0 input");
			return "";
		}
		
		int i = 0;
		int start_score=0;

		
		//loop over input
		for (; i < input.length; i++)
		{
			
			//calculate probability of start sequence
			_previous_bits = _previous_bits << 1;
			if (input[i])
				_previous_bits += 1;
			
			start_score = Integer.bitCount( (~(_previous_bits ^ _search_pattern)) & _search_mask);

			if (start_score >= (double)_search_len*1)
			{
				_bit2char_i = 0;
				_bitmask = 1;			//next loop start extracting
				_current_char = 0;
				_next_start = true;
				out = out + "$$";
			}
			else if (_next_start)
			{
				_next_start = false;
			}
			else if (_bitmask > 0)	//if currently processing a character, read next bit
			{
				if (input[i])  //if 1
					_current_char = _current_char + _bitmask;
				
				_bitmask = _bitmask << 1;		//increment pointers and mask
				_bit2char_i++;
				
				
				if (_bit2char_i >= _data_bits)	//if finished character
				{
					_bitmask = 0;
					_bit2char_i = 0;
					out = out + (char)_current_char;
					_current_char = 0;
				}				
			}
			else //count number of stop bits
			{
				_bit2char_i++;
				if (_bit2char_i >= _stop_bits)
				{
					_bitmask = 1;
					_bit2char_i = 0;
					_next_start = true;
					_current_char = 0;
				}
				
			}
		}
		
		_bit2char_last_bit = input[input.length-1];
		
		return out;
	}

	public String bits2chars (double[] input)
	{		
		return bits2chars(input, _data_bits);
	}
	
	public String bits2chars (double[] input, int data_bits)
	{
		
		boolean[] out = new boolean[input.length];
		for (int i = 0; i < input.length; i++)
		{
			if (input[i]<0)
				out[i] = true;
			else
				out[i] = false;
		}
		
		return bits2chars(out,data_bits);	
		
	}
	
	public String bits2chars (boolean[] input)
	{
		return bits2chars(input,_data_bits);	
	}
	
	public String bits2chars (boolean[] input, int data_bits)
	{
		if (_decoding_method == Method.FIXED_POSITION)
			return bits2chars_fixed(input);
		
		//reset bit grouping counters
		_bit_grouping_total_50 = 0;
		_this_grouping = 0;
		_bits_count = 0;
		
		
		String out = "";
		_average_stop_bits = 0;
		_bits_since_last = 0;
		_total_average = 0;
		boolean first_start_found = false;
		
		if (input.length < 1)
		{
			System.out.println("bits2chars: 0 input");
			return "";
		}
		
		int i = 0;
		
		//special case to handle [i-1] in main loop
		if (_bitmask <= 0)
		{
			if ((!input[0]) && (_bit2char_last_bit))
			{
				_bitmask = 1;				
			}
			i = 1;
		}
		
		//loop over input
		for (; i < input.length; i++)
		{
			if (_bitmask > 0)	//if currently processing a character, read next bit
			{
				
				//this bit counts bit periods signal is high or low to work out incorrect baud rates
				//looks for 50 bauds when demodulated as 300 baud
				_this_grouping++;
				if (input[i] != _last_bit)
				{
					_bit_grouping_total_50 += _this_grouping;
					_bits_count++;
					_this_grouping = 0;
				}				
				
				if (input[i])   //if 1
					_current_char = _current_char + _bitmask;
				
				_bitmask = _bitmask << 1;		//increment pointers and mask
				_bit2char_i++;
				
				
				if (_bit2char_i >= data_bits)	//if finished character
				{
					_bitmask = 0;
					_bit2char_i = 0;
					out = out + (char)_current_char;
					_current_char = 0;
				}				
			}
			else //look for start of char
			{
				if ((!input[i]) && (input[i-1]))
				{
					_bitmask = 1;
					
					//now average stops part
					first_start_found = true;
					if (_bits_since_last > 0 && _bits_since_last < 5)
					{
						_total_average++;
						_average_stop_bits = _average_stop_bits + _bits_since_last;						
					}
					_bits_since_last = 0;
				}
				else if (first_start_found)
					_bits_since_last++;
			}
			_last_bit = input[i];
		}
		
		_bit2char_last_bit = input[input.length-1];
		
		return out;
	}
	
	
	
}
