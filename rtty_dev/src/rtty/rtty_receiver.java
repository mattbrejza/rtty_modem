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



import javax.swing.event.EventListenerList;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;



public class rtty_receiver implements StringRxEvent {
	
	int FFT_find_half_len = 256;
    private DoubleFFT_1D ft_find = new DoubleFFT_1D(FFT_find_half_len*2);
    
    int FFT_follow_half_len = 512;
    private DoubleFFT_1D ft_follow = new DoubleFFT_1D(FFT_follow_half_len*2);
    
    private rtty_decode decoder = new rtty_decode(1200,1800,7);
    

    
    private Telemetry_handler telem_hand_7 = new Telemetry_handler();
    private Telemetry_handler telem_hand_8 = new Telemetry_handler();
    private Telemetry_handler telem_hand_f = new Telemetry_handler();
    
    
    
    
    private moving_average av_shift = new moving_average(10);
    
    public int search_range_rtty = 14;

    public enum State { INACTIVE, IDLE, FOUND_SIG, BAUD_KNOWN};
    
    public State current_state = State.INACTIVE;
    public int current_data_bits = 7;
    public int current_stop_bits = 1;
    public int current_baud = 300;
    
    private Bits_to_chars bit2char_7 = new Bits_to_chars(7,Bits_to_chars.Method.WAIT_FOR_START_BIT);
    private Bits_to_chars bit2char_8 = new Bits_to_chars(8,Bits_to_chars.Method.WAIT_FOR_START_BIT);
    private Bits_to_chars bit2char_fixed = new Bits_to_chars(7,2,Bits_to_chars.Method.FIXED_POSITION);
    
    //used for 'string received' event
    protected EventListenerList  _listeners = new EventListenerList() ;

    
	/*Plot2DPanel plot = new Plot2DPanel();
	int plotint = -100;
	int plotint2 = -100;
	int plotint3 = -100;*/
    
    private graph_line grtty = new graph_line();

	public rtty_receiver() {
		// TODO Auto-generated constructor stub
		telem_hand_7.addStringRecievedListener(this);
		telem_hand_8.addStringRecievedListener(this);
		telem_hand_f.addStringRecievedListener(this);
	}
	
	public void addStringRecievedListener(StringRxEvent listener)
	{	
		_listeners.add(StringRxEvent.class, listener);
	}
	
	protected void fireStringReceived(String str, boolean checksum)
	{
		Object[] listeners = _listeners.getListenerList();
		for (int i =listeners.length-2; i>=0; i-=2)   //urgh, why does java have to make this so horrible
		{			
			((StringRxEvent)listeners[i+1]).StringRx(str,checksum);
		}
	}
	
	public double[] findRTTY(double[] samples,boolean update)
	{
		//returns array where [0] is f1 and [1] is f2
		//TODO remove below
		//fireStringReceived("moo");
		
		double[] out = new double[] {0,0};
		
		if (samples.length < FFT_find_half_len*2)
			return out;
		
		//get 256 (useful) FFT bins
		double[] fftar = new double[samples.length];
		System.arraycopy(samples,0,fftar,0,samples.length);
		ft_find.realForward(fftar);
		
		double c[] = new double[FFT_find_half_len];
		
		//calculate abs(.)
        for (int i = 0; i < FFT_find_half_len; i++)
        {
        	c[i] = Math.pow(fftar[i*2], 2) + Math.pow(fftar[i*2 +1], 2);
        }
		
        int windows = 15;
        int win_size = (int)FFT_find_half_len/windows;
        
        double[][] peak = new double[windows][2];	//0: loc; 1: val
        //int[] peak_loc = new int[windows];
        //double[] peak_val = new double[windows];
        int peak_count = 0;
        
        // TODO each peak should be y dB higher than the min in the window
        //peak search
        for (int i = 1; i < FFT_find_half_len-1; i++)
        {
        	if ((c[i-1] < c[i]) && (c[i] > c[i+1]))		//if found window
        	{
        		if (i < peak[peak_count][0]+win_size)   //if another peak in the same window
        		{
        			if (peak[peak_count][1] < c[i])
        			{
        				peak[peak_count][0] = i;
        				peak[peak_count][1] = c[i];
        			}
        		}
        		else
        		{
        			peak_count++;
    				peak[peak_count][0] = i;
    				peak[peak_count][1] = c[i];
        		}
        	}
        }
        for (int i = 0; i < peak_count; i++)
        {
        	peak[peak_count][1] = Math.sqrt(peak[peak_count][1]);
        }
        //now have a list of peaks in the fft
        //sort peaks by amplitude of peak
       
        java.util.Arrays.sort(peak, new java.util.Comparator<double[]>() {
        	public int compare(double[]a, double[]b) {
        		return (int)(b[1] - a[1]);
        	}
        });
        
        int bb_len = Math.max(1000, samples.length);
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
        	double freq = ((double)peak[i][0])/((double)(FFT_find_half_len*2));
        	/*
        	double[] ph = new double[bb_len];
        	double[] los = new double[bb_len];
        	double[] loc = new double[bb_len];
        	double[] ir = new double[bb_len];
        	double[] qr = new double[bb_len];*/
        	
        	for (int j = 0; j< bb_len;j++)
        	{
        		
        		//multiply, filter, square and add
        		bb[i][j] = Math.pow(filts.step(samples[j] * Math.sin(2*Math.PI*lo_phase)),2) + Math.pow(filtc.step(samples[j] * Math.cos(2*Math.PI*lo_phase)),2);
        		//bb[i][j] = Math.pow((samples[j] * Math.sin(2*Math.PI*lo_phase)),2) + Math.pow((samples[j] * Math.cos(2*Math.PI*lo_phase)),2);
        	/*	ph[j]=lo_phase;
        		los[j] = Math.sin(2*Math.PI*lo_phase);
        		loc[j] = Math.cos(2*Math.PI*lo_phase);
        		ir[j] = samples[j] * los[j];
        		qr[j] = samples[j] * loc[j];
        		bb[i][j] = Math.pow(filts.step(ir[j]), 2) + Math.pow(filtc.step(qr[j]), 2);
        				*/
        		lo_phase = lo_phase + freq;
        		if (j >= 30)
        		{
	        		maxs[i] = Math.max(maxs[i], bb[i][j]);
	        		mins[i] = Math.min(mins[i], bb[i][j]);
	        		means[i] = means[i] + bb[i][j];
        		}
        	}
        	means[i] = means[i] / (bb_len-30);
        	upthre[i] = (maxs[i]-means[i])*0.3 + means[i];
        	lothre[i] = -(maxs[i]-means[i])*0.3 + means[i];
        	
        }
        
        grtty.clearMarkers();
       // grtty.clearlines();
        System.out.println();
        System.out.println();
        
        //iterate through all combinations of peaks to find a signal
        for (int i = 0; i < peak_count-1; i++)
        {
        	for (int j=i+1; j< peak_count; j++)
        	{        		
        		if ((peak[i][1] > peak[j][1]*.5 ) && (peak[i][1] < peak[j][1]*2 ))
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
        					last_state1 = last_state;
        					last_state = current_state;
        					
        					//both not high at same time check
        					if (!(bb[j][k]>maxs[j]/4) && !(bb[i][k]>maxs[i]/4))
        						highs++;
        					
        				}
        				
        				System.out.println(transitionsh + "  " + transitionsl + "  " + highs);
        				
        				
        				
        				
        				if ( (transitionsh+transitionsl > 2)  && (transitionsh >0) && (transitionsl > 0) && (highs < 12))        					
        				{
        					       					
        				
	        				
	        				//TODO check there are some transitions in the data
        					if (peak[i][0] > peak[j][0])
        					{
        						out[1] = peak[i][0]*8000/(FFT_find_half_len*2);
    	        				out[0] = peak[j][0]*8000/(FFT_find_half_len*2);
        					}
        					else
        					{
        						out[0] = peak[i][0]*8000/(FFT_find_half_len*2);
    	        				out[1] = peak[j][0]*8000/(FFT_find_half_len*2);
        					}
	        				
	        				
	        			
		    				grtty.drawfft(c);
		    				grtty.addMarkers(peak[i][0],peak[j][0]);
		    				//grtty.addMarkers(peak[][0]);
		    				for (int z = 0; z < peak_count; z++)
		    					grtty.addMarkers(peak[z][0]);
		    				
		    				if (update)
		    				{
			    				decoder._f1=out[0]/8000;
			    				decoder._f2=out[1]/8000;
		    				}
		    				
		    				//grtty.drawlinedual(bb[i],bb[j],100);
		    				
		    					  
	        				return out;
        				}
        			}
        		}        		
        	}        	
        }
        
        
       
        //plotint = plot.addLinePlot("my plot", c);
		
        grtty.drawfft(c);
		grtty.clearMarkers();
		for (int z = 0; z < peak_count; z++)
			grtty.addMarkers(peak[z][0]);
		return out;
	}

	public void followRTTY(double[] samples)
	{
		//calls follow RTTY, then updates the demod by first making sure the shift is averaged.
	
		double[] new_pos = followRTTY_getpos(samples, search_range_rtty,false);
	
		if (av_shift.getMA() == 0)
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
		
		grtty.addMarkers(decoder._f1*(2*FFT_follow_half_len),decoder._f2*(2*FFT_follow_half_len));
		
	}
	
	private double[] followRTTY_getpos(double[] samples, int search_range, boolean update)
	{
		//search range is number of fft bins each side of old_Fx
		
		double[] out = new double[] {0,0};
		
		
		double old_f1=decoder._f1;
		double old_f2=decoder._f2;
		
		
		if (samples.length < FFT_follow_half_len*2)
			return out;
		
		//get 512 (useful) FFT bins
		double[] fftar = new double[samples.length];
		System.arraycopy(samples,0,fftar,0,samples.length);
		ft_follow.realForward(fftar);
		
		double c[] = new double[FFT_follow_half_len];
		
		//calculate abs(.)
        for (int i = 0; i < FFT_follow_half_len; i++)
        {
        	c[i] = Math.pow(fftar[i*2], 2) + Math.pow(fftar[i*2 +1], 2);
        }
        
        double[] int_f1 = new double[2*search_range -1];
        double[] int_f2 = new double[2*search_range -1];
        int bin_f1 = (int)(old_f1 * 1024);
        int bin_f2 = (int)(old_f2 * 1024);
        
        int j=0;
        //now integrate the FFT plot around each old freq
        for (int i = bin_f1-search_range+1; i < bin_f1+search_range; i++)
        {
        	if ((i  >= 0) && (i < FFT_follow_half_len))
        	{
        		if (j==0)
        			int_f1[0]=Math.sqrt(c[i]);
        		else
        			int_f1[j]=int_f1[j-1]+Math.sqrt(c[i]);
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
        	if ((i  >= 0) && (i < FFT_follow_half_len))
        	{
        		if (j==0)
        			int_f2[0]=Math.sqrt(c[i]);
        		else
        			int_f2[j]=int_f2[j-1]+Math.sqrt(c[i]);
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
        grtty.clearMarkers();
        grtty.drawfft(c);
        //grtty.addMarkers(new double[] {midbin_1,midbin_2});
        
		
		//return -1 if one peak is somewhat less then the other
		//this indicates that carrier wasnt present in the fft range
		double peak = Math.max(int_f1[2*search_range -2], int_f2[2*search_range -2]);
		
		if ( int_f1[2*search_range -2] < peak/3 )
			out[0] = (double)-1;
		else
			out[0] = (double)(midbin_1)/(2*FFT_follow_half_len);
		
		if ( int_f2[2*search_range -2] < peak/3 )
			out[1] = (double)-1;
		else
			out[1] = (double)(midbin_2)/(2*FFT_follow_half_len);
		
       
        
        if (update)
        {
        	decoder._f1=(double)(midbin_1)/(2*FFT_follow_half_len);
        	decoder._f2=(double)(midbin_2)/(2*FFT_follow_half_len);
        }
        
       
        
        return out;
	}
	
	/*
	public String extractTelem (String input)
	{
		if (!(telem_buff == ""))   //if in middle of a string
		{
			telem_buff = telem_buff + input;
		}
		else //search for start
		{
			
		}
		
		
		return "";
	}
*/
	
	public String processBlock (double[] samples)
	{
		boolean[] bits = decoder.processBlock_2bits(samples,50);
		String str ="";
		
		boolean valid7 = false,valid8 = false;  
		
		
		
		if (current_state == State.IDLE)		//if data /stops are known then used fixed stops decoder too
		{
			bit2char_fixed.DataBits(current_data_bits);
			bit2char_fixed.StopBits(current_stop_bits);
			str = bit2char_fixed.bits2chars(bits);
			telem_hand_f.ExtractPacket(str);
			if (current_data_bits ==7)
			{
				str = bit2char_7.bits2chars(bits);
				telem_hand_7.ExtractPacket(str);
			}
			else
			{
				str = bit2char_8.bits2chars(bits);
				telem_hand_8.ExtractPacket(str);
			}
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
		
		//System.out.println(bit2char_7.average_stop_bits());
		
		//at this stage, if valid7/8 is high, then the databits info is known, and the fixed extractor can be used
		if (valid7)
		{
			current_state = State.IDLE;
			current_data_bits = 7;
			current_stop_bits = (int) Math.round(bit2char_7.average_stop_bits());
		}
		else if (valid8)
		{
			current_state = State.IDLE;
			current_data_bits = 8;
			current_stop_bits = (int) Math.round(bit2char_8.average_stop_bits());
		}
		
		
		return str;
	}

	
	public void StringRx(String str, boolean checksum)
	{
		if (_listeners.getListenerList().length > 0)
		{
			fireStringReceived(str, checksum);
		}
	}
	
	
	
	
}