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
import java.util.ArrayList;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;



public class fsk_receiver implements StringRxEvent {
	
	public int FFT_half_len = 512; 
    private DoubleFFT_1D ft_obj = new DoubleFFT_1D(FFT_half_len*2);
    
    //int FFT_follow_half_len = 512;
    //private DoubleFFT_1D ft_follow = new DoubleFFT_1D(FFT_follow_half_len*2);
    
    private fsk_demodulator decoder = new fsk_demodulator(1200,1800);
    

    ConfidenceCalculator cc = new ConfidenceCalculator(8000);
    
    private Telemetry_handler telem_hand_7 = new Telemetry_handler();
    private Telemetry_handler telem_hand_8 = new Telemetry_handler();
    private Telemetry_handler telem_hand_f = new Telemetry_handler();
    private Binary_frame_handler telem_hand_bin = 
    		new Binary_frame_handler(new boolean[]
    				//{true, false,true, false,true, false,true, false,true, false,true, false});
    				{true, false, false, true, true, false, true, false, false, true, false, true, false, false, true, false, true, true, true, false, false, true, true, true, false, false, false, true, false, true, true, false});
    				//{true,true,false,false,false,true,false,true,true,false,false});//{true,false,false,false,true,false,false,true,false,false,true,true});
    
    private String last_sha = "";
    
    public boolean enableFFT = true;
    
    private moving_average av_shift = new moving_average(10);
    
    public int search_range_rtty = 14;

    public enum Mode { BINARY, RTTY };
    public enum Modulation { FSK, AFSK };
    public enum State { INACTIVE, IDLE, FOUND_SIG, BAUD_KNOWN };
    
    public State current_state = State.INACTIVE;
    public Mode current_mode = Mode.RTTY;
    public Modulation current_modulation = Modulation.FSK;
    public int current_data_bits = 7;
    public int current_stop_bits = 1;
    public int current_baud = 300;
    public boolean auto_rtty_finding = true;
    public boolean enable_afc = true;
    
    public int afc_update_freq = 8000;
    private int samples_since_afc = 0;
    
    public int search_freq = 8000;
    private int samples_since_search = 0;
    
    public int fft_update_freq = 2000;
    private int samples_since_fft = 0;
    
    public int samples_since_last_valid = 0;

    
    private double[] _samples;
    private double[] _fft;
    private boolean _fft_updated = false;
    
    private int[] _peaklocs;
    
    private Bits_to_chars bit2char_7 = new Bits_to_chars(7,Bits_to_chars.Method.WAIT_FOR_START_BIT);
    private Bits_to_chars bit2char_8 = new Bits_to_chars(8,Bits_to_chars.Method.WAIT_FOR_START_BIT);
    private Bits_to_chars bit2char_fixed = new Bits_to_chars(7,2,Bits_to_chars.Method.FIXED_POSITION);
    
    //used for 'string received' event
    protected ArrayList<StringRxEvent> _listeners = new ArrayList<StringRxEvent>();

	public fsk_receiver() {
		// TODO Auto-generated constructor stub
		telem_hand_7.addStringRecievedListener(this);
		telem_hand_8.addStringRecievedListener(this);
		telem_hand_f.addStringRecievedListener(this);
		telem_hand_bin.addStringRecievedListener(this);
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
	

	
	public double[] find_fsk(double[] samples)
	{
		_samples = samples;
		_fft_updated = false;
		return find_fsk(false);
	}
	
	//note: gives the square of the FFT
	private boolean calcuate_FFT()
	{
		if (_fft_updated)
			return true;
		
		
		if (_samples.length < FFT_half_len*2)
			return false;
		
		//get 256 (useful) FFT bins
		double[] fftar = new double[_samples.length];
		System.arraycopy(_samples,0,fftar,0,_samples.length);
		ft_obj.realForward(fftar);
		
		_fft = (new double[FFT_half_len]);
		
		//calculate abs(.)
        for (int i = 0; i < FFT_half_len; i++)
        {
        	_fft[i] = Math.pow(fftar[i*2], 2) + Math.pow(fftar[i*2 +1], 2);
        }
        
        _fft_updated = true;
        return true;
	}
	
	private double[] find_fsk(boolean update)
	{
		//returns array where [0] is f1 and [1] is f2

		
		double[] out = new double[] {0,0};
		
		
		if (!calcuate_FFT())
			return out;
		
        int windows = 15;
        int win_size = (int)FFT_half_len/windows;
        
        double[][] peak = new double[windows][2];	//0: loc; 1: val
        //int[] peak_loc = new int[windows];
        //double[] peak_val = new double[windows];
        int peak_count = 0;
        
        // TODO each peak should be y dB higher than the min in the window (kinda done)
        //peak search
        double win_min_c=1e20;     //current minimum
        double win_min_p=1e20;	   //previous minimum
        int min_win_cnt = 0;
        for (int i = 1; i < FFT_half_len-1; i++)
        {
        	//used to get minimum values for 2 windows in advance
        	if (_fft[i] < win_min_c)
        		win_min_c = _fft[i];
        	min_win_cnt++;
        	if (min_win_cnt >= win_size)
        	{
        		win_min_p = win_min_c;
        		win_min_c = 1e20;
        		min_win_cnt=0;
        	}
        	
        	if ((_fft[i-1] < _fft[i]) && (_fft[i] > _fft[i+1]) && (_fft[i] > win_min_c*10 ||  _fft[i] > win_min_p*10))		//if found peak
        	{
        		if (i < peak[peak_count][0]+win_size)   //if another peak in the same window
        		{
        			if (peak[peak_count][1] < _fft[i] )
        			{
        				peak[peak_count][0] = i;
        				peak[peak_count][1] = _fft[i];
        			}
        		}
        		else
        		{
        			peak_count++;
    				peak[peak_count][0] = i;
    				peak[peak_count][1] = _fft[i];
        		}
        	}
        }
        _peaklocs = (new int[peak_count]);
        for (int i = 0; i < peak_count; i++)
        {
        	peak[peak_count][1] = Math.sqrt(peak[peak_count][1]);   //sqrt the peaks
        	_peaklocs[i] = (int)peak[i][0];					//copy to variable to allow for debug access
        }
        //now have a list of peaks in the fft
        //sort peaks by amplitude of peak
       
        java.util.Arrays.sort(peak, new java.util.Comparator<double[]>() {
        	public int compare(double[]a, double[]b) {
        		return (int)(b[1] - a[1]);
        	}
        });
        
        int bb_len = Math.min(2000, _samples.length);
        double bb[][] =  new double [peak_count][bb_len];
        double maxs[] =  new double [peak_count];
        double mins[] =  new double [peak_count];
        double means[] = new double [peak_count];
        double upthre[] = new double [peak_count];
        double lothre[] = new double [peak_count];
        
        // TODO demod and check in order of highest amplitude to reduce the need to demod every signal
            
        
        
        //demodulate at each peak
        for (int i = 0; i < peak_count; i++)
        {
        	//fir_filter filts = new fir_filter(new double[] {-0.00903981521632955, -0.0176057278961508, -0.0214888217308206, -0.00894080820836387, 0.0281449413985572, 0.0880962336584224, 0.156738932301488, 0.211778824066603, 0.232865400131940, 0.211778824066603, 0.156738932301488, 0.0880962336584224, 0.0281449413985572, -0.00894080820836387, -0.0214888217308206, -0.0176057278961508, -0.00903981521632955});
        	//fir_filter filtc = new fir_filter(new double[] {-0.00903981521632955, -0.0176057278961508, -0.0214888217308206, -0.00894080820836387, 0.0281449413985572, 0.0880962336584224, 0.156738932301488, 0.211778824066603, 0.232865400131940, 0.211778824066603, 0.156738932301488, 0.0880962336584224, 0.0281449413985572, -0.00894080820836387, -0.0214888217308206, -0.0176057278961508, -0.00903981521632955});
        	fir_filter filts = new fir_filter(new double[] {0.0856108043700266,	0.0887368103506232,	0.0912267090885950,	0.0930370874042555,	0.0941362126682943,	0.0945047522364111,	0.0941362126682943,	0.0930370874042555,	0.0912267090885950,	0.0887368103506232,	0.0856108043700266});
        	fir_filter filtc = new fir_filter(new double[] {0.0856108043700266,	0.0887368103506232,	0.0912267090885950,	0.0930370874042555,	0.0941362126682943,	0.0945047522364111,	0.0941362126682943,	0.0930370874042555,	0.0912267090885950,	0.0887368103506232,	0.0856108043700266});

        	double lo_phase = 0;
        	double freq = ((double)peak[i][0])/((double)(FFT_half_len*2));
        

        	for (int j = 0; j< bb_len;j++)
        	{
        		//multiply, filter, square and add
        		bb[i][j] = Math.pow(filts.step(_samples[j] * Math.sin(2*Math.PI*lo_phase)),2) + Math.pow(filtc.step(_samples[j] * Math.cos(2*Math.PI*lo_phase)),2);

        		lo_phase = lo_phase + freq;
        		if (j >= 30)
        		{
	        		//maxs[i] = Math.max(maxs[i], bb[i][j]);
	        		if (bb[i][j]> maxs[i] && j > 60)
	        		{
	        			maxs[i] = bb[i][j];
	        		}
	        		mins[i] = Math.min(mins[i], bb[i][j]);
	        		means[i] = means[i] + bb[i][j];
        		}
        	}
        	means[i] = means[i] / (bb_len-30);
        	upthre[i] = means[i]*1.2;  //(maxs[i]-means[i])*0.3 + means[i];
        	lothre[i] = means[i]*0.8; //-(maxs[i]-means[i])*0.3 + means[i];
        }
        
 //       grtty.clearMarkers();
      //  System.out.println();
      //  System.out.println();
        peak_count=Math.min(4, peak_count);
        //iterate through all combinations of peaks to find a signal
        for (int i = 0; i < peak_count-1; i++)
        {
        	for (int j=i+1; j< peak_count; j++)
        	{        		
        		if ((peak[i][1] > peak[j][1]*.16 ) && (peak[i][1] < peak[j][1]*7 ))
        		{
        			if ((means[i] > mins[j]) && (means[j] > mins[i]))
        			{
        				
        				//count the number of transitions between the two potential signals
        				int transitionsl = 0;
        				int transitionsh = 0;
        				int highs = 0;
        				boolean last_state = bb[i][40] > bb[j][40];
        				boolean last_state1 = bb[i][40] > bb[j][40];
        				for (int k = 50; k < bb_len-10; k=k+10)
        				{
        					//Transition checker
        					boolean current_state = (bb[i][k] > bb[j][k]);
        					if (last_state1 != current_state)
							{        						
        						if (   (current_state == (bb[i][k-5] > bb[j][k-5]))  ||   (current_state == (bb[i][k+5] > bb[j][k+5]))   )	
        						{
        							if (current_state)
        							{
        								if (  (bb[i][k] > upthre[i])  && (bb[j][k] < lothre[j])  )
        								{
        									transitionsh++;
        								}
        							}
        							else
        							{
        								if (  (bb[j][k] > upthre[j])  && (bb[i][k] < lothre[i])  )
        								{
        									transitionsl++;
        								}
        							}        							
        						}
							}
        					last_state1 = last_state; //test c 
        					last_state = current_state;
        					
        					//both not high at same time check
        					if (!(bb[j][k]>maxs[j]/4) && !(bb[i][k]>maxs[i]/4))
        					{
        						if (!(bb[j][k+5]>maxs[j]/4) && !(bb[i][k+5]>maxs[i]/4))
        						{
        							if (!(bb[j][k-5]>maxs[j]/4) && !(bb[i][k-5]>maxs[i]/4))
        								highs++;
        						}
        					}  
        				}
        				
        			//	System.out.println(transitionsh + "  " + transitionsl + "  " + highs);
        				
        				
        				
        				//TODO: look at reinstating this
        				if ( /*(transitionsh+transitionsl > 2)  && (transitionsh >0) && (transitionsl > 0) && */(highs < 12))        					
        				{
        					       					
        				
	        				//TODO check there are some transitions in the data
        					if (peak[i][0] > peak[j][0])
        					{
        						out[1] = peak[i][0]*8000/(FFT_half_len*2);
    	        				out[0] = peak[j][0]*8000/(FFT_half_len*2);
        					}
        					else
        					{
        						out[0] = peak[i][0]*8000/(FFT_half_len*2);
    	        				out[1] = peak[j][0]*8000/(FFT_half_len*2);
        					}
	        				
	        				
	        			
	//	    				grtty.drawfft(_fft);
	//	    				grtty.addMarkers(peak[i][0],peak[j][0]);
	//	    				//grtty.addMarkers(peak[][0]);
	//	    				for (int z = 0; z < peak_count; z++)
	//	    					grtty.addMarkers(peak[z][0]);
		    				
		    				if (update)
		    				{
			    				decoder._f1=out[0]/8000;
			    				decoder._f2=out[1]/8000;
		    				}
		    						    					  
	        				return out;
        				}
        			}
        		}        		
        	}        	
        }
		return out;
	}

	private void follow_fsk(boolean initial_solution)
	{
		//calls follow RTTY, then updates the demod by first making sure the shift is averaged.
		//if initial solution, the update is applied without any averaging
	
		double[] new_pos = follow_fsk_getpos( search_range_rtty);
	
		if (av_shift.getMA() == 0 || initial_solution)
		{
			av_shift.init(decoder._f2 - decoder._f1);
		}
		
		if (new_pos[0] < 0)
		{    //update position based only on upper freq
			decoder._f2 = new_pos[1];
			decoder._f1 = new_pos[1] - av_shift.getMA();
		}
		else if (new_pos[1] < 0)
		{    //update position based only on lower freq
			decoder._f1 = new_pos[0];
			decoder._f2 = new_pos[0] + av_shift.getMA();
		}
		else
		{
			if (new_pos[1]-new_pos[0] < 50/8000)
				av_shift.update(50);
			else
				av_shift.update(new_pos[1]-new_pos[0]);
			
			double centre = (new_pos[1]-new_pos[0])/2 + new_pos[0];
			decoder._f1 = centre - av_shift.getMA()/2;
			decoder._f2 = centre + av_shift.getMA()/2;
		}		
	}
	
	private double[] follow_fsk_getpos(int search_range)
	{
		//search range is number of fft bins each side of old_Fx
		
		double[] out = new double[] {0,0};
		
		
		double old_f1=decoder._f1;
		double old_f2=decoder._f2;
		
		
		if (!calcuate_FFT())
			return out;
        
        double[] int_f1 = new double[2*search_range -1];
        double[] int_f2 = new double[2*search_range -1];
        int bin_f1 = (int)(old_f1 * 1024);
        int bin_f2 = (int)(old_f2 * 1024);
        
        int j=0;
        //now integrate the FFT plot around each old freq
        for (int i = bin_f1-search_range+1; i < bin_f1+search_range; i++)
        {
        	if ((i  >= 0) && (i < FFT_half_len))
        	{
        		if (j==0)
        			int_f1[0]=Math.sqrt(_fft[i]);
        		else
        			int_f1[j]=int_f1[j-1]+Math.sqrt(_fft[i]);
        	}
        	else
        	{
        		if (j==0)
        			int_f1[0]=0;
        		else
        			int_f1[j]=int_f1[j-1];
        	}	
        	j++;
        }
        j=0;
        for (int i = bin_f2-search_range+1; i < bin_f2+search_range; i++)
        {
        	if ((i  >= 0) && (i < FFT_half_len))
        	{
        		if (j==0)
        			int_f2[0]=Math.sqrt(_fft[i]);
        		else
        			int_f2[j]=int_f2[j-1]+Math.sqrt(_fft[i]);
        	}
        	else
        	{
        		if (j==0)
        			int_f2[0]=0;
        		else
        			int_f2[j]=int_f2[j-1];
        	}	
        	j++;
        }
        
        int midbin_1=0;
        int midbin_2=0;
        //look for midpoint of integration curve - where the middle of the peak is
        for (int i=0; i < 2*search_range -2; i++)
        {
        	if ((midbin_1 ==0) && (int_f1[i] > int_f1[2*search_range -2]/2))
        	{
        		midbin_1=i+bin_f1-search_range+1;
        	}
        	if ((midbin_2 ==0) && (int_f2[i] > int_f2[2*search_range -2]/2))
        	{
        		midbin_2=i+bin_f2-search_range+1;
        	}
        }

		//return -1 if one peak is somewhat less then the other
		//this indicates that carrier wasnt present in the fft range
		double peak = Math.max(int_f1[2*search_range -2], int_f2[2*search_range -2]);
		
		if ( int_f1[2*search_range -2] < peak/3 )
			out[0] = (double)-1;
		else
			out[0] = (double)(midbin_1)/(2*FFT_half_len);
		
		if ( int_f2[2*search_range -2] < peak/3 )
			out[1] = (double)-1;
		else
			out[1] = (double)(midbin_2)/(2*FFT_half_len);
	
        return out;
	}
		
	public String processBlock (double[] samples, int baud)
	{
		
		//TODO: consider writing samples as a class wide variable rather than passing to each method
		//TODO: search and afc both fft but dont share results
		
		_samples = samples;
		_fft_updated = false;
		
		samples_since_afc += _samples.length;
		samples_since_search += _samples.length;
		samples_since_fft += _samples.length;
		
		cc.samplesElapsed(samples.length);
		
		ConfidenceCalculator.State initialState = cc.state;
		
		if (auto_rtty_finding && cc.fullSearchDue())
		{
			samples_since_fft = 0;
			double[] loc = find_fsk(false);
			boolean up = cc.putFrequencies(loc[0]/8000, loc[1]/8000);
			if (up){
				decoder._f1 = cc.getFrequencies(0);
				decoder._f2 = cc.getFrequencies(1); }
			
		}
		
		if (enable_afc && samples_since_afc >= afc_update_freq)  //step 2 : follow signal if afc is set
		{
			samples_since_fft = 0;
			samples_since_afc = 0;
			follow_fsk(initialState != ConfidenceCalculator.State.SIG_DROPPED);
			
			cc.AFCUpdate(decoder._f1,decoder._f2);
		}
		
		if (samples_since_fft >= fft_update_freq && enableFFT)
		{
			calcuate_FFT();
			samples_since_fft = 0;
		}
		
		/*
		//step 1 : find rtty signal if needed
		if (auto_rtty_finding && current_state == State.INACTIVE && samples_since_search >= search_freq)
		{
			samples_since_fft = 0;
			samples_since_search = 0;
			double[] loc = findRTTY(true);
			if (loc[0] > 0)
			{
				current_state = State.FOUND_SIG;
				followRTTY(true);
			}
		}
		else if (enable_afc && samples_since_afc >= afc_update_freq)  //step 2 : follow signal if afc is set
		{
			samples_since_fft = 0;
			samples_since_afc = 0;
			followRTTY(false);
		}
		
		if (samples_since_fft >= fft_update_freq)
		{
			calcuate_FFT();
			samples_since_fft = 0;
		}
		
		*/
		
		//step 3 : demodulate the signal		
		
		String str = "";	
		
		if (current_mode == Mode.RTTY)
		{
			boolean[] bits = decoder.processBlock_2bits(samples,baud);
			cc.putPowerLevels(decoder.getLastMaxPower(),decoder.getLastAveragePower());
						
			//step 4 : convert a bitstream to telemetry		
				
			boolean valid7 = false,valid8 = false;  	
			
			if (current_state == State.IDLE)		//if data /stops are known then used fixed stops decoder too
			{
				bit2char_fixed.DataBits(current_data_bits);
				bit2char_fixed.StopBits(current_stop_bits);
				str = bit2char_fixed.bits2chars(bits);
				telem_hand_f.ExtractPacket(str);
				if (current_data_bits ==7)
				{
					str = bit2char_8.bits2chars(bits);
					valid8 = telem_hand_8.ExtractPacket(str);
					str = bit2char_7.bits2chars(bits);
					valid7 = telem_hand_7.ExtractPacket(str);
				}
				else
				{
					str = bit2char_7.bits2chars(bits);
					valid7 = telem_hand_7.ExtractPacket(str);
					str = bit2char_8.bits2chars(bits);
					valid8 = telem_hand_8.ExtractPacket(str);
				}
				
				if (cc.getState() == ConfidenceCalculator.State.SIG_DROPPED)
					current_state = State.INACTIVE;
			}
			else if (current_data_bits ==7)			//if data / stops not known try 7nX and 8nX, returning the results of whatever setting was used last
			{
				str = bit2char_8.bits2chars(bits);
				valid8 = telem_hand_8.ExtractPacket(str);
				str = bit2char_7.bits2chars(bits);
				valid7 = telem_hand_7.ExtractPacket(str);
			}
			else
			{
				str = bit2char_7.bits2chars(bits);
				valid7 = telem_hand_7.ExtractPacket(str);
				str = bit2char_8.bits2chars(bits);
				valid8 = telem_hand_8.ExtractPacket(str);
			}
			//System.out.println(bit2char_7.Average_bit_period());
			
			//at this stage, if valid7/8 is high, then the databits info is known, and the fixed extractor can be used
			if (valid7)
			{
				current_state = State.IDLE;
				cc.gotDecode();
				current_data_bits = 7;
				current_stop_bits = (int) Math.round(bit2char_7.average_stop_bits());
			}
			else if (valid8)
			{
				current_state = State.IDLE;
				cc.gotDecode();
				current_data_bits = 8;
				current_stop_bits = (int) Math.round(bit2char_8.average_stop_bits());
			}
		}
		else
		{
			double[] bits = decoder.processBlock_2bits_llr(samples,baud);
			boolean valid_bin = false;
			str = telem_hand_bin.bits2chars(bits);
			valid_bin = telem_hand_bin.get_last_valid();
			
			if (current_modulation == Modulation.AFSK)
			{
				if (cc.getState() == ConfidenceCalculator.State.SIG_DROPPED)
					current_state = State.INACTIVE;
				if (valid_bin)
				{
					cc.gotDecode();
					current_state = State.IDLE;
				}
			}
		}
		
		return str;
	}

	public void StringRx(Byte[] str, boolean checksum)
	{
		
	}
	
	public void StringRx(String str, boolean checksum)
	{

		if (!last_sha.equals(str))
		{
			last_sha = str;		
			if (_listeners.size() > 0)
			{
				fireStringReceived(str, checksum);
			}
		}
	}

	
	public double[] get_fft() {
		return _fft;
	}
	
	public double get_fft(int i)
	{
		if (_fft == null)
			return 0;
		if (i < _fft.length)
			return _fft[i];
		else
			return 0;
		
	}

	public int[] get_peaklocs() {
		return _peaklocs;
	}


	public double get_f1() {
		return decoder._f1;
	}
	
	public double get_f2() {
		return decoder._f2;
	}
	
	public void setFreq(double f1, double f2)
	{
		decoder.setFreq(f1, f2);
	}
	
	public boolean get_fft_updated()
	{
		return _fft_updated;
	}
	//IG_LOST,SIG_JUST_FOUND,SIG_TRACKING,SIG_DROPPED};
	public String statusToString()
	{
		if (current_modulation == Modulation.AFSK)
			return "Tracking Signal";
		switch (cc.state)
		{
			case SIG_LOST : 
				return "Searching";		
			case SIG_JUST_FOUND :
				return "Found Signal";
			case SIG_TRACKING :
				return "Tracking Signal";
			case SIG_DROPPED :
				return "Dropped Signal";
			default : 
				return "Inactive";
		}
	}
	
	public void setModulation(Modulation set)
	{
		current_modulation = set;
		if (current_modulation == Modulation.AFSK)
		{
			auto_rtty_finding = false;
			enable_afc = false;
		}
		else
		{
			auto_rtty_finding = true;
			enable_afc = true;
		}
	}
	
	public void setMode(Mode set)
	{
		current_mode = set;
	}
	
	public boolean paramsValid()
	{
		return (current_state == State.IDLE);
	}


	
	
	
}