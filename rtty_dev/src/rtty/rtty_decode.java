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

import java.awt.Color;


public class rtty_decode {
	//MUST give sample rate of 8000
	
	int _data_bits=7;
	double _f1;         //low LO
	double _f2;         //high LO
	private double _LO2_phase;  //high LO
	private double _LO1_phase;  //low LO
	
	//demod 
	int resample_counter = 0;
	
	//sync
    private int _sync_pos = 0;   //counter in the sync, for position in the bit (0->6)
    private double _sync_error = 0; //stores the error (currently an integrator)
    private double _sync_late = 0;  //late gate value
    private moving_average _sync_thres;//value over which the sync should add/skip a cycle
    private double _last_bit = 0;   //last bit value the sync encountered
	
    private fir_filter fir_res1_300;
    private fir_filter fir_res2_300;
    private fir_filter fir_res3_300;
    private fir_filter fir_res4_300;
    
    private fir_filter fir_res1_50;
    private fir_filter fir_res2_50;
    private fir_filter fir_res3_50;
    private fir_filter fir_res4_50;
    
    private fir_filter fir_outi1;
    private fir_filter fir_outq1;
    private fir_filter fir_outi2;
    private fir_filter fir_outq2;
    
    //previous window for debug
    //private double[] prev_win = new double[200];
    private graph_baseband gbb = new graph_baseband();


	public rtty_decode(double f1, double f2, int data_bits) {
		
		_f1 =  f1 /8000;
		_f2 =  f2 /8000;
		
		fir_res1_300 = new fir_filter(new double[] {0.000786526835886959, 0.000599114690717327, 0.000400204797590807, -0.000307882669456068, -0.00166263695026489, -0.00366837630292647, -0.00612518190577769, -0.00858589989940669, -0.0103628564209468, -0.0105943725464158, -0.00837134797365531, -0.00290532178931729, 0.00628520202560880, 0.0192179861031230, 0.0353433796420604, 0.0535430726303462, 0.0722257626486942, 0.0895215970746844, 0.103547570655055, 0.112691192664381, 0.115866753291496, 0.112691192664381, 0.103547570655055, 0.0895215970746844, 0.0722257626486942, 0.0535430726303462, 0.0353433796420604, 0.0192179861031230, 0.00628520202560880, -0.00290532178931729, -0.00837134797365531, -0.0105943725464158, -0.0103628564209468, -0.00858589989940669, -0.00612518190577769, -0.00366837630292647, -0.00166263695026489, -0.000307882669456068, 0.000400204797590807, 0.000599114690717327, 0.000786526835886959});
		fir_res2_300 = new fir_filter(new double[] {0.000786526835886959, 0.000599114690717327, 0.000400204797590807, -0.000307882669456068, -0.00166263695026489, -0.00366837630292647, -0.00612518190577769, -0.00858589989940669, -0.0103628564209468, -0.0105943725464158, -0.00837134797365531, -0.00290532178931729, 0.00628520202560880, 0.0192179861031230, 0.0353433796420604, 0.0535430726303462, 0.0722257626486942, 0.0895215970746844, 0.103547570655055, 0.112691192664381, 0.115866753291496, 0.112691192664381, 0.103547570655055, 0.0895215970746844, 0.0722257626486942, 0.0535430726303462, 0.0353433796420604, 0.0192179861031230, 0.00628520202560880, -0.00290532178931729, -0.00837134797365531, -0.0105943725464158, -0.0103628564209468, -0.00858589989940669, -0.00612518190577769, -0.00366837630292647, -0.00166263695026489, -0.000307882669456068, 0.000400204797590807, 0.000599114690717327, 0.000786526835886959});
		fir_res3_300 = new fir_filter(new double[] {0.000786526835886959, 0.000599114690717327, 0.000400204797590807, -0.000307882669456068, -0.00166263695026489, -0.00366837630292647, -0.00612518190577769, -0.00858589989940669, -0.0103628564209468, -0.0105943725464158, -0.00837134797365531, -0.00290532178931729, 0.00628520202560880, 0.0192179861031230, 0.0353433796420604, 0.0535430726303462, 0.0722257626486942, 0.0895215970746844, 0.103547570655055, 0.112691192664381, 0.115866753291496, 0.112691192664381, 0.103547570655055, 0.0895215970746844, 0.0722257626486942, 0.0535430726303462, 0.0353433796420604, 0.0192179861031230, 0.00628520202560880, -0.00290532178931729, -0.00837134797365531, -0.0105943725464158, -0.0103628564209468, -0.00858589989940669, -0.00612518190577769, -0.00366837630292647, -0.00166263695026489, -0.000307882669456068, 0.000400204797590807, 0.000599114690717327, 0.000786526835886959});
		fir_res4_300 = new fir_filter(new double[] {0.000786526835886959, 0.000599114690717327, 0.000400204797590807, -0.000307882669456068, -0.00166263695026489, -0.00366837630292647, -0.00612518190577769, -0.00858589989940669, -0.0103628564209468, -0.0105943725464158, -0.00837134797365531, -0.00290532178931729, 0.00628520202560880, 0.0192179861031230, 0.0353433796420604, 0.0535430726303462, 0.0722257626486942, 0.0895215970746844, 0.103547570655055, 0.112691192664381, 0.115866753291496, 0.112691192664381, 0.103547570655055, 0.0895215970746844, 0.0722257626486942, 0.0535430726303462, 0.0353433796420604, 0.0192179861031230, 0.00628520202560880, -0.00290532178931729, -0.00837134797365531, -0.0105943725464158, -0.0103628564209468, -0.00858589989940669, -0.00612518190577769, -0.00366837630292647, -0.00166263695026489, -0.000307882669456068, 0.000400204797590807, 0.000599114690717327, 0.000786526835886959});
		
		//FIR least squares, Fs=8000, Fpass = 150, Fstop = 400, Wp/s = 1, order = 60
		fir_res1_50 = new fir_filter(new double[] {-0.000470150951958517, -0.000998444628627754, -0.00166659230451889, -0.00246057404752554, -0.00335187222880445, -0.00429665490816831, -0.00523593893273512, -0.00609683876406133, -0.00679493819123118, -0.00723774490572776, -0.00732910763626992, -0.00697439793371205, -0.00608618964206127, -0.00459011414921971, -0.00243053347211709, 0.000424340274996863, 0.00397823930812195, 0.00820387248501318, 0.0130414594402352, 0.0183989498042643, 0.0241540141790163, 0.0301577870656315, 0.0362402324935664, 0.0422168980203310, 0.0478967292940066, 0.0530905421049322, 0.0576196972923792, 0.0613244999799519, 0.0640718505133941, 0.0657617102714020, 0.0663320092670076, 0.0657617102714020, 0.0640718505133941, 0.0613244999799519, 0.0576196972923792, 0.0530905421049322, 0.0478967292940066, 0.0422168980203310, 0.0362402324935664, 0.0301577870656315, 0.0241540141790163, 0.0183989498042643, 0.0130414594402352, 0.00820387248501318, 0.00397823930812195, 0.000424340274996863, -0.00243053347211709, -0.00459011414921971, -0.00608618964206127, -0.00697439793371205, -0.00732910763626992, -0.00723774490572776, -0.00679493819123118, -0.00609683876406133, -0.00523593893273512, -0.00429665490816831, -0.00335187222880445, -0.00246057404752554, -0.00166659230451889, -0.000998444628627754, -0.000470150951958517});
		fir_res2_50 = new fir_filter(new double[] {-0.000470150951958517, -0.000998444628627754, -0.00166659230451889, -0.00246057404752554, -0.00335187222880445, -0.00429665490816831, -0.00523593893273512, -0.00609683876406133, -0.00679493819123118, -0.00723774490572776, -0.00732910763626992, -0.00697439793371205, -0.00608618964206127, -0.00459011414921971, -0.00243053347211709, 0.000424340274996863, 0.00397823930812195, 0.00820387248501318, 0.0130414594402352, 0.0183989498042643, 0.0241540141790163, 0.0301577870656315, 0.0362402324935664, 0.0422168980203310, 0.0478967292940066, 0.0530905421049322, 0.0576196972923792, 0.0613244999799519, 0.0640718505133941, 0.0657617102714020, 0.0663320092670076, 0.0657617102714020, 0.0640718505133941, 0.0613244999799519, 0.0576196972923792, 0.0530905421049322, 0.0478967292940066, 0.0422168980203310, 0.0362402324935664, 0.0301577870656315, 0.0241540141790163, 0.0183989498042643, 0.0130414594402352, 0.00820387248501318, 0.00397823930812195, 0.000424340274996863, -0.00243053347211709, -0.00459011414921971, -0.00608618964206127, -0.00697439793371205, -0.00732910763626992, -0.00723774490572776, -0.00679493819123118, -0.00609683876406133, -0.00523593893273512, -0.00429665490816831, -0.00335187222880445, -0.00246057404752554, -0.00166659230451889, -0.000998444628627754, -0.000470150951958517});
		fir_res3_50 = new fir_filter(new double[] {-0.000470150951958517, -0.000998444628627754, -0.00166659230451889, -0.00246057404752554, -0.00335187222880445, -0.00429665490816831, -0.00523593893273512, -0.00609683876406133, -0.00679493819123118, -0.00723774490572776, -0.00732910763626992, -0.00697439793371205, -0.00608618964206127, -0.00459011414921971, -0.00243053347211709, 0.000424340274996863, 0.00397823930812195, 0.00820387248501318, 0.0130414594402352, 0.0183989498042643, 0.0241540141790163, 0.0301577870656315, 0.0362402324935664, 0.0422168980203310, 0.0478967292940066, 0.0530905421049322, 0.0576196972923792, 0.0613244999799519, 0.0640718505133941, 0.0657617102714020, 0.0663320092670076, 0.0657617102714020, 0.0640718505133941, 0.0613244999799519, 0.0576196972923792, 0.0530905421049322, 0.0478967292940066, 0.0422168980203310, 0.0362402324935664, 0.0301577870656315, 0.0241540141790163, 0.0183989498042643, 0.0130414594402352, 0.00820387248501318, 0.00397823930812195, 0.000424340274996863, -0.00243053347211709, -0.00459011414921971, -0.00608618964206127, -0.00697439793371205, -0.00732910763626992, -0.00723774490572776, -0.00679493819123118, -0.00609683876406133, -0.00523593893273512, -0.00429665490816831, -0.00335187222880445, -0.00246057404752554, -0.00166659230451889, -0.000998444628627754, -0.000470150951958517});
		fir_res4_50 = new fir_filter(new double[] {-0.000470150951958517, -0.000998444628627754, -0.00166659230451889, -0.00246057404752554, -0.00335187222880445, -0.00429665490816831, -0.00523593893273512, -0.00609683876406133, -0.00679493819123118, -0.00723774490572776, -0.00732910763626992, -0.00697439793371205, -0.00608618964206127, -0.00459011414921971, -0.00243053347211709, 0.000424340274996863, 0.00397823930812195, 0.00820387248501318, 0.0130414594402352, 0.0183989498042643, 0.0241540141790163, 0.0301577870656315, 0.0362402324935664, 0.0422168980203310, 0.0478967292940066, 0.0530905421049322, 0.0576196972923792, 0.0613244999799519, 0.0640718505133941, 0.0657617102714020, 0.0663320092670076, 0.0657617102714020, 0.0640718505133941, 0.0613244999799519, 0.0576196972923792, 0.0530905421049322, 0.0478967292940066, 0.0422168980203310, 0.0362402324935664, 0.0301577870656315, 0.0241540141790163, 0.0183989498042643, 0.0130414594402352, 0.00820387248501318, 0.00397823930812195, 0.000424340274996863, -0.00243053347211709, -0.00459011414921971, -0.00608618964206127, -0.00697439793371205, -0.00732910763626992, -0.00723774490572776, -0.00679493819123118, -0.00609683876406133, -0.00523593893273512, -0.00429665490816831, -0.00335187222880445, -0.00246057404752554, -0.00166659230451889, -0.000998444628627754, -0.000470150951958517});

		
		fir_outi1 = new fir_filter(new double[] {7.66162137128810e-20, 3.43550906278866e-05, -8.83345603792872e-20, -0.000122408611849792, -0.000346506388319725, -0.000677302805286787, -0.00110849468468061, -0.00162028620808185, -0.00217798010455547, -0.00273157043974124, -0.00321650959397525, -0.00355574358872080, -0.00366301597355252, -0.00344733783933627, -0.00281841824515606, -0.00169275411240634, 1.24442056906536e-18, 0.00231081620366610, 0.00526568968969643, 0.00886109513374814, 0.0130609054973305, 0.0177950964431462, 0.0229604512616186, 0.0284233508924476, 0.0340245905577380, 0.0395860193321182, 0.0449186628386524, 0.0498318729891462, 0.0541429618220168, 0.0576867264750822, 0.0603242638564779, 0.0619505081454146, 0.0625000000000000, 0.0619505081454146, 0.0603242638564779, 0.0576867264750822, 0.0541429618220168, 0.0498318729891462, 0.0449186628386524, 0.0395860193321182, 0.0340245905577380, 0.0284233508924476, 0.0229604512616186, 0.0177950964431462, 0.0130609054973305, 0.00886109513374814, 0.00526568968969643, 0.00231081620366610, 1.24442056906536e-18, -0.00169275411240634, -0.00281841824515606, -0.00344733783933627, -0.00366301597355252, -0.00355574358872080, -0.00321650959397525, -0.00273157043974124, -0.00217798010455547, -0.00162028620808185, -0.00110849468468061, -0.000677302805286787, -0.000346506388319725, -0.000122408611849792, -8.83345603792872e-20, 3.43550906278866e-05, 7.66162137128810e-20});
		fir_outq2 = new fir_filter(new double[] {7.66162137128810e-20, 3.43550906278866e-05, -8.83345603792872e-20, -0.000122408611849792, -0.000346506388319725, -0.000677302805286787, -0.00110849468468061, -0.00162028620808185, -0.00217798010455547, -0.00273157043974124, -0.00321650959397525, -0.00355574358872080, -0.00366301597355252, -0.00344733783933627, -0.00281841824515606, -0.00169275411240634, 1.24442056906536e-18, 0.00231081620366610, 0.00526568968969643, 0.00886109513374814, 0.0130609054973305, 0.0177950964431462, 0.0229604512616186, 0.0284233508924476, 0.0340245905577380, 0.0395860193321182, 0.0449186628386524, 0.0498318729891462, 0.0541429618220168, 0.0576867264750822, 0.0603242638564779, 0.0619505081454146, 0.0625000000000000, 0.0619505081454146, 0.0603242638564779, 0.0576867264750822, 0.0541429618220168, 0.0498318729891462, 0.0449186628386524, 0.0395860193321182, 0.0340245905577380, 0.0284233508924476, 0.0229604512616186, 0.0177950964431462, 0.0130609054973305, 0.00886109513374814, 0.00526568968969643, 0.00231081620366610, 1.24442056906536e-18, -0.00169275411240634, -0.00281841824515606, -0.00344733783933627, -0.00366301597355252, -0.00355574358872080, -0.00321650959397525, -0.00273157043974124, -0.00217798010455547, -0.00162028620808185, -0.00110849468468061, -0.000677302805286787, -0.000346506388319725, -0.000122408611849792, -8.83345603792872e-20, 3.43550906278866e-05, 7.66162137128810e-20});
		fir_outi2 = new fir_filter(new double[] {7.66162137128810e-20, 3.43550906278866e-05, -8.83345603792872e-20, -0.000122408611849792, -0.000346506388319725, -0.000677302805286787, -0.00110849468468061, -0.00162028620808185, -0.00217798010455547, -0.00273157043974124, -0.00321650959397525, -0.00355574358872080, -0.00366301597355252, -0.00344733783933627, -0.00281841824515606, -0.00169275411240634, 1.24442056906536e-18, 0.00231081620366610, 0.00526568968969643, 0.00886109513374814, 0.0130609054973305, 0.0177950964431462, 0.0229604512616186, 0.0284233508924476, 0.0340245905577380, 0.0395860193321182, 0.0449186628386524, 0.0498318729891462, 0.0541429618220168, 0.0576867264750822, 0.0603242638564779, 0.0619505081454146, 0.0625000000000000, 0.0619505081454146, 0.0603242638564779, 0.0576867264750822, 0.0541429618220168, 0.0498318729891462, 0.0449186628386524, 0.0395860193321182, 0.0340245905577380, 0.0284233508924476, 0.0229604512616186, 0.0177950964431462, 0.0130609054973305, 0.00886109513374814, 0.00526568968969643, 0.00231081620366610, 1.24442056906536e-18, -0.00169275411240634, -0.00281841824515606, -0.00344733783933627, -0.00366301597355252, -0.00355574358872080, -0.00321650959397525, -0.00273157043974124, -0.00217798010455547, -0.00162028620808185, -0.00110849468468061, -0.000677302805286787, -0.000346506388319725, -0.000122408611849792, -8.83345603792872e-20, 3.43550906278866e-05, 7.66162137128810e-20});
		fir_outq1 = new fir_filter(new double[] {7.66162137128810e-20, 3.43550906278866e-05, -8.83345603792872e-20, -0.000122408611849792, -0.000346506388319725, -0.000677302805286787, -0.00110849468468061, -0.00162028620808185, -0.00217798010455547, -0.00273157043974124, -0.00321650959397525, -0.00355574358872080, -0.00366301597355252, -0.00344733783933627, -0.00281841824515606, -0.00169275411240634, 1.24442056906536e-18, 0.00231081620366610, 0.00526568968969643, 0.00886109513374814, 0.0130609054973305, 0.0177950964431462, 0.0229604512616186, 0.0284233508924476, 0.0340245905577380, 0.0395860193321182, 0.0449186628386524, 0.0498318729891462, 0.0541429618220168, 0.0576867264750822, 0.0603242638564779, 0.0619505081454146, 0.0625000000000000, 0.0619505081454146, 0.0603242638564779, 0.0576867264750822, 0.0541429618220168, 0.0498318729891462, 0.0449186628386524, 0.0395860193321182, 0.0340245905577380, 0.0284233508924476, 0.0229604512616186, 0.0177950964431462, 0.0130609054973305, 0.00886109513374814, 0.00526568968969643, 0.00231081620366610, 1.24442056906536e-18, -0.00169275411240634, -0.00281841824515606, -0.00344733783933627, -0.00366301597355252, -0.00355574358872080, -0.00321650959397525, -0.00273157043974124, -0.00217798010455547, -0.00162028620808185, -0.00110849468468061, -0.000677302805286787, -0.000346506388319725, -0.000122408611849792, -8.83345603792872e-20, 3.43550906278866e-05, 7.66162137128810e-20});
		
		_sync_thres = new moving_average(16);
	}
	
	

	
	//this returns the raw bitstream for purposes of working out 7n1/8n1 etc
	public boolean[] processBlock_2bits (double[] samples, int baud)
	{
		
		double[] llrs = (sync(demod_samples(samples,baud)));
		boolean[] out = new boolean[llrs.length];
		for (int i = 0; i < llrs.length; i++)
		{
			if (llrs[i]<0)
				out[i] = true;
			else
				out[i] = false;
		}
		
		return out;		
	}
	
	//this returns the raw bitstream for purposes of working out 7n1/8n1 etc
	public double[] processBlock_2bits_llr (double[] samples, int baud)
	{
		return (sync(demod_samples(samples, baud)));		
	}
	
	private double[] demod_samples (double[] samples, int baud)
	{
		
		int upsample_ratio = 1;
		int downsample_ratio = 1;
		
		if (baud == 300)
		{
			upsample_ratio = 3;
			downsample_ratio = 5;
		}
		else  //else 50
		{
			downsample_ratio = 10;
		}

		double[] bb_h_s = new double[samples.length*upsample_ratio];
		double[] bb_h_c = new double[samples.length*upsample_ratio];
		double[] bb_l_s = new double[samples.length*upsample_ratio];
		double[] bb_l_c = new double[samples.length*upsample_ratio];
		
		//perform multiplication for each tone with sin and cos
		for (int i = 0; i < samples.length; i++)
		{
			bb_h_s[i*upsample_ratio] = samples[i] * Math.sin(2 * Math.PI *_LO2_phase);
			bb_h_c[i*upsample_ratio] = samples[i] * Math.cos(2 * Math.PI *_LO2_phase);
			bb_l_s[i*upsample_ratio] = samples[i] * Math.sin(2 * Math.PI *_LO1_phase);
			bb_l_c[i*upsample_ratio] = samples[i] * Math.cos(2 * Math.PI *_LO1_phase);
			//increment phases
			_LO1_phase = _LO1_phase + _f1;
			_LO2_phase = _LO2_phase + _f2;
		}
				
		_LO1_phase = _LO1_phase % 1;
		_LO2_phase = _LO2_phase % 1;
		
		//filter so that the sample rate can be reduced
		if (baud == 300)
		{
			for (int i =0; i<samples.length*upsample_ratio; i++)
			{
				bb_h_s[i] = fir_res1_300.step(bb_h_s[i]);
				bb_h_c[i] = fir_res2_300.step(bb_h_c[i]);
				bb_l_s[i] = fir_res3_300.step(bb_l_s[i]);
				bb_l_c[i] = fir_res4_300.step(bb_l_c[i]);			
			}
		}
		else  //else 50
		{
			for (int i =0; i<samples.length*upsample_ratio; i++)
			{
				bb_h_s[i] = fir_res1_50.step(bb_h_s[i]);
				bb_h_c[i] = fir_res2_50.step(bb_h_c[i]);
				bb_l_s[i] = fir_res3_50.step(bb_l_s[i]);
				bb_l_c[i] = fir_res4_50.step(bb_l_c[i]);			
			}
		}
		
		
		//consider only every 5th sample (300 baud) or every 10th sample (50 baud)
		
		//assume 300 baud for now
		int R = downsample_ratio;
		
		int len = (int) Math.floor(((samples.length*upsample_ratio)-resample_counter)/R);
		if (len > 0)
		{
			double[] out = new double[len];
					
			

			
			//sample, square and add
			int j = 0;
			for (int i = (R-resample_counter) % R; i < samples.length*upsample_ratio; i=i+R)
			{
				//output filter
				bb_l_s[i] = fir_outi1.step(bb_l_s[i]);
				bb_l_c[i] = fir_outq1.step(bb_l_c[i]);
				bb_h_s[i] = fir_outi2.step(bb_h_s[i]);
				bb_h_c[i] = fir_outq2.step(bb_h_c[i]);
				
				out[j] =  (Math.pow(bb_l_s[i],2) + Math.pow(bb_l_c[i],2)) - 
				          (Math.pow(bb_h_s[i],2) + Math.pow(bb_h_c[i],2));
				j++;
			}
			
			//TODO FILTER THEN SQUARE!!! NEEDS CHANGING
			//TODO for science compare performance of each arrangement
			

			
			resample_counter = ((resample_counter + (samples.length*3)) % R) % R;
			return out;
		}
		else
		{
			resample_counter = ((resample_counter + (samples.length*3)) % R) % R;
			return null;
		}
		
	}

	private double[] sync(double[] input)
	{
		// TODO increment values more than just 1/-1;
		// TODO remember that data is already squared coming into this block
		//debug
		double[] gin = new double[400];
		System.arraycopy(input,0,gin,0,400);
		//System.arraycopy(prev_win,0,gin,0,200);
		gbb.drawsingle(gin);
		gbb.clearMarkers();
		//System.arraycopy(input,input.length-200,prev_win,0,200);
		//
		double[] out = new double[input.length/14];
		int out_count = 0;
		
		for (int i = 0; i < input.length; i++)
		{
			switch (_sync_pos)
			{
				case 3:   //early gate
					if (Math.signum(input[i]) != Math.signum(_last_bit))
					{
						_sync_error = _sync_error + Math.pow(input[i],2) - _sync_late;							
					}
					if (i < 400)
						gbb.addMarkers(i, Color.RED);
					break;
				case 6:		//skip or add a cycle
					if (_sync_error > _sync_thres.getMA())		//skip
					{
						_sync_pos = _sync_pos + 1;
						_sync_error = _sync_error - _sync_thres.getMA();
					}
					else if (_sync_error > _sync_thres.getMA())  //add
					{
						_sync_pos = _sync_pos - 1;
						_sync_error = _sync_error + _sync_thres.getMA();
					}
					break;
				case 8:   //sample gate
					out[out_count] = input[i];
					out_count++;
					_last_bit = input[i];
					_sync_thres.update(Math.pow(input[i],2));
					if (i < 400)
						gbb.addMarkers(i, Color.BLUE);
					break;					
				case 12:  //late gate
					_sync_late = Math.pow(input[i],2);
					if (i < 400)
						gbb.addMarkers(i, Color.GREEN);
					break;
											
			}
			_sync_pos = (_sync_pos+1) % 16;
		}
		
		
		//resize output length
		if (out_count == 0)
		{
			return null;
		}
		else
		{
			double[] fo = new double[out_count];
			System.arraycopy(out, 0, fo, 0, out_count);
			return fo;
		
		}
		
	}

	

	
}
