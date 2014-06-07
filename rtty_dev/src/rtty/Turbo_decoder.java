package rtty;


public class Turbo_decoder {
	
	int _max_iterations = 10;
	boolean last_success = false;
	int last_fixed = 0;
	
	private static int[][] interleaver_parameters = new int[][]{{40,3,10}, {48,7,12}, {56,19,42}, {64,7,16}, {72,7,18}, {80,11,20}, {88,5,22}, {96,11,24}, {104,7,26}, {112,41,84}, {120,103,90}, {128,15,32}, {136,9,34}, {144,17,108}, {152,9,38}, {160,21,120}, {168,101,84}, {176,21,44}, {184,57,46}, {192,23,48}, {200,13,50}, {208,27,52}, {216,11,36}, {224,27,56}, {232,85,58}, {240,29,60}, {248,33,62}, {256,15,32}, {264,17,198}, {272,33,68}, {280,103,210}, {288,19,36}, {296,19,74}, {304,37,76}, {312,19,78}, {320,21,120}, {328,21,82}, {336,115,84}, {344,193,86}, {352,21,44}, {360,133,90}, {368,81,46}, {376,45,94}, {384,23,48}, {392,243,98}, {400,151,40}, {408,155,102}, {416,25,52}, {424,51,106}, {432,47,72}, {440,91,110}, {448,29,168}, {456,29,114}, {464,247,58}, {472,29,118}, {480,89,180}, {488,91,122}, {496,157,62}, {504,55,84}, {512,31,64}, {528,17,66}, {544,35,68}, {560,227,420}, {576,65,96}, {592,19,74}, {608,37,76}, {624,41,234}, {640,39,80}, {656,185,82}, {672,43,252}, {688,21,86}, {704,155,44}, {720,79,120}, {736,139,92}, {752,23,94}, {768,217,48}, {784,25,98}, {800,17,80}, {816,127,102}, {832,25,52}, {848,239,106}, {864,17,48}, {880,137,110}, {896,215,112}, {912,29,114}, {928,15,58}, {944,147,118}, {960,29,60}, {976,59,122}, {992,65,124}, {1008,55,84}, {1024,31,64}, {1056,17,66}, {1088,171,204}, {1120,67,140}, {1152,35,72}, {1184,19,74}, {1216,39,76}, {1248,19,78}, {1280,199,240}, {1312,21,82}, {1344,211,252}, {1376,21,86}, {1408,43,88}, {1440,149,60}, {1472,45,92}, {1504,49,846}, {1536,71,48}, {1568,13,28}, {1600,17,80}, {1632,25,102}, {1664,183,104}, {1696,55,954}, {1728,127,96}, {1760,27,110}, {1792,29,112}, {1824,29,114}, {1856,57,116}, {1888,45,354}, {1920,31,120}, {1952,59,610}, {1984,185,124}, {2016,113,420}, {2048,31,64}, {2112,17,66}, {2176,171,136}, {2240,209,420}, {2304,253,216}, {2368,367,444}, {2432,265,456}, {2496,181,468}, {2560,39,80}, {2624,27,164}, {2688,127,504}, {2752,143,172}, {2816,43,88}, {2880,29,300}, {2944,45,62}, {3008,157,188}, {3072,47,96}, {3136,13,28}, {3200,111,240}, {3264,443,204}, {3328,51,104}, {3392,51,212}, {3456,451,192}, {3520,257,220}, {3584,57,336}, {3648,313,228}, {3712,271,232}, {3776,179,236}, {3840,331,120}, {3904,363,244}, {3968,375,248}, {4032,127,168}, {4096,31,64}, {4160,33,130}, {4224,43,264}, {4288,33,134}, {4352,477,408}, {4416,35,138}, {4480,233,280}, {4544,357,142}, {4608,337,480}, {4672,37,146}, {4736,71,444}, {4800,71,120}, {4864,37,152}, {4928,39,462}, {4992,127,234}, {5056,39,158}, {5120,39,80}, {5184,31,96}, {5248,113,902}, {5312,41,166}, {5376,251,336}, {5440,43,170}, {5504,21,86}, {5568,43,174}, {5632,45,176}, {5696,45,178}, {5760,161,120}, {5824,89,182}, {5888,323,184}, {5952,47,186}, {6016,23,94}, {6080,47,190}, {6144,263,480}};

	private int[] interleaver = new int[]{0};
	
	
	public static boolean check_checksum( boolean [] systematic)
	{
		
		int crc = 0xFFFF;
		int i = 0;
		
		if (systematic.length < 16)
			return false;
		
		while (i < systematic.length-16)
		{
			byte in = 0;
			int mask = 0x80;
			while (i < systematic.length-16 && mask != 0)
			{
				if (systematic[i])
					in |= mask;
				mask >>= 1;
				i++;
			}
			
			crc = crc_xmodem_update(crc,in);
		}
		
		int in_crc = 0;
		int mask = 0x8000;
		for (i = systematic.length-16; i < systematic.length; i++)
		{
			if (systematic[i])
				in_crc |= mask;
			mask >>= 1;
		}
		
		return ((in_crc & 0xFFFF) == (crc & 0xFFFF));
			
		
	}
	
	public boolean[] decode( double[] d0, double[] d1, double[] d2, boolean checksum, boolean termination)
	{
		if (d0.length != d1.length)
			throw new IllegalArgumentException("Check Input Array Lengths");
		if (d0.length != d2.length)
			throw new IllegalArgumentException("Check Input Array Lengths");
		
		int bits,decoder_bits;
		last_success = false;
		if (termination){
			 bits = d0.length - 4;
			 decoder_bits = bits + 3;
		}else{
			bits = d0.length;
			decoder_bits = bits;
		}
		
		if (interleaver.length != bits)
			interleaver = get_interleaver(bits,termination);
				
		double[] decoder1_out = null;
		double[] decoder2_out = new double[decoder_bits];
		
		//termination bits
		double[] xk = new double[3];
		double[] zk = new double[3];
		double[] zpk = new double[3];
		double[] xpk = new double[3];
		
		//termination demultiplexing
		if (termination)
		{
			//5.1.3.2.2
			int k = bits;
			xk[0] = d0[k];  zk[1] = d0[k+1];    xpk[0] = d0[k+2];  zpk[1] = d0[k+3];
			zk[0] = d1[k];  xk[2] = d1[k+1];    zpk[0] = d1[k+2];  xpk[2] = d1[k+3];
			xk[1] = d2[k];  zk[2] = d2[k+1];    xpk[1] = d2[k+2];  zpk[2] = d2[k+3];
		}
		
		//signals input to the decoder
		double[] llrs_sys = new double[decoder_bits];
		double[] llrs_dec1 = new double[decoder_bits];
		double[] llrs_dec2 = new double[decoder_bits];
		boolean[] out = new boolean[bits];
		if (termination){
			llrs_dec1[bits] = zk[0];  llrs_dec1[bits+1] = zk[1];  llrs_dec1[bits+2] = zk[2];  
			llrs_dec2[bits] = zpk[0];  llrs_dec2[bits+1] = zpk[1];  llrs_dec2[bits+2] = zpk[2];  
		}
		for (int i = 0; i < bits; i++)
		{
			llrs_sys[i] = d0[i];
			llrs_dec1[i] = d1[i];
			llrs_dec2[i] = d2[i];
		}
		
		for (int i = 0; i < _max_iterations; i++)
		{
			if (termination){
				llrs_sys[bits] = xk[0]; llrs_sys[bits+1] = xk[1]; llrs_sys[bits+2] = xk[2]; 
				decoder2_out[bits] = 0; decoder2_out[bits+1] = 0; decoder2_out[bits+2] = 0; 
			}
			decoder1_out = bcjr ( decoder2_out, llrs_sys, llrs_dec1, false, termination);
			
			
			
			if (termination){
				llrs_sys[bits] = xpk[0]; llrs_sys[bits+1] = xpk[1]; llrs_sys[bits+2] = xpk[2]; 
				decoder1_out[bits] = 0; decoder1_out[bits+1] = 0; decoder1_out[bits+2] = 0; 
			}				
			decoder2_out = bcjr ( decoder1_out, llrs_sys, llrs_dec2, true,  termination);
			
			//check checksum
			if (checksum)
			{
				last_fixed = 0;
				for (int j = 0; j < bits; j++){
					out[j] = decoder1_out[j]+decoder2_out[j]+llrs_sys[j]>0 ? true:false;
					if (out[j] != llrs_sys[j]>0)
						last_fixed++;
				}
				if (Turbo_decoder.check_checksum(out))
				{
					last_success = true;
					return out;
				}
			}
			
		}
		
		
		
		return out;
	}
	
	private double[] bcjr (double[] uncoded1, double[] uncoded2, double[] coded, boolean interleave, boolean termination)
	{
		//inter = 0  - no interleaver
		//inter = 1  - interleave on uncoded side

		
		if (uncoded1.length != coded.length)
			throw new IllegalArgumentException("Check Input Array Lengths");
		if (uncoded2.length != coded.length)
			throw new IllegalArgumentException("Check Input Array Lengths");
		
		int bits = uncoded1.length;
		double[][] alphas = new double[bits][8];
		double[][] betas = new double[bits][8];
		double[] deltas = new double[16];
		double[] out = new double[bits];
		
		///////////  forward recursion /////////////
		
		//initialise first state
		alphas[0][0] = 0;
		for (int i = 1; i < 8; i++)
			alphas[0][i] = -9000;
		
		//do the rest
		double un,en,bo,no,o0,o1;
		no = 0;
		for (int i = 1; i < bits; i++)
		{
			if (interleave)					
				un = uncoded1[interleaver[i-1]]+uncoded2[interleaver[i-1]];
			else			
				un = uncoded1[i-1]+uncoded2[i-1];
				
			en = coded[i-1];
			bo = un+en;
			//inverse bodge
			//bo = un;
			//un = en;
			//en = bo;
			//bo = 0;
			//no = en+un;
			/////			
			alphas[i][0] = Math.max(alphas[i-1][0] + no , alphas[i-1][1] + bo );
			alphas[i][1] = Math.max(alphas[i-1][2] + un , alphas[i-1][3] + en );
			alphas[i][2] = Math.max(alphas[i-1][4] + en , alphas[i-1][5] + un );
			alphas[i][3] = Math.max(alphas[i-1][6] + bo , alphas[i-1][7] + no );
			
			alphas[i][4] = Math.max(alphas[i-1][0] + bo , alphas[i-1][1] + no );
			alphas[i][5] = Math.max(alphas[i-1][2] + en , alphas[i-1][3] + un );
			alphas[i][6] = Math.max(alphas[i-1][4] + un , alphas[i-1][5] + en );
			alphas[i][7] = Math.max(alphas[i-1][6] + no , alphas[i-1][7] + bo );
		}
	
		///////////  backward recursion /////////////
		
		//initialise last state
		for (int i = 0; i < 8; i++)
			betas[bits-1][i] = -9000;
		if (termination)
			betas[bits-1][0] = 0;
		
		//do the rest
		for (int i = bits-2; i >= 0; i--)
		{
			if (interleave)					
				un = uncoded1[interleaver[i+1]]+uncoded2[interleaver[i+1]];
			else			
				un = uncoded1[i+1]+uncoded2[i+1];
			
			en = coded[i+1];
			bo = un+en;
			//inverse bodge
			//bo = un;
			//un = en;
			//en = bo;
			//bo = 0;
			//no = en+un;
			/////
			betas[i][0] = Math.max(betas[i+1][0] + no , betas[i+1][4] + bo );
			betas[i][1] = Math.max(betas[i+1][0] + bo , betas[i+1][4] + no );
			betas[i][2] = Math.max(betas[i+1][1] + un , betas[i+1][5] + en );
			betas[i][3] = Math.max(betas[i+1][1] + en , betas[i+1][5] + un );
			
			betas[i][4] = Math.max(betas[i+1][2] + en , betas[i+1][6] + un );
			betas[i][5] = Math.max(betas[i+1][2] + un , betas[i+1][6] + en );
			betas[i][6] = Math.max(betas[i+1][3] + bo , betas[i+1][7] + no );
			betas[i][7] = Math.max(betas[i+1][3] + no , betas[i+1][7] + bo );
		}
		
		///////////  delta and output  /////////////
		
		//output the LLR
		for (int i = 0; i < bits; i++)
		{ 
			if (interleave)					
				un = uncoded1[interleaver[i]]+uncoded2[interleaver[i]];
			else			
				un = uncoded1[i]+uncoded2[i];
			
			en = coded[i];
			bo = un+en;
			//inverse bodge
			//bo = un;
			//un = en;
			//en = bo;
			//bo = 0;
			//no = en+un;
			/////
			
			deltas[0] = alphas[i][0] + betas[i][0] + no; //0
			deltas[1] = alphas[i][1] + betas[i][0] + bo; //1
			deltas[2] = alphas[i][2] + betas[i][1] + un; //1
			deltas[3] = alphas[i][3] + betas[i][1] + en; //0
			
			deltas[4] = alphas[i][4] + betas[i][2] + en; //0
			deltas[5] = alphas[i][5] + betas[i][2] + un; //1
			deltas[6] = alphas[i][6] + betas[i][3] + bo; //1
			deltas[7] = alphas[i][7] + betas[i][3] + no; //0
			
			deltas[8]  = alphas[i][0] + betas[i][4] + bo; //1
			deltas[9]  = alphas[i][1] + betas[i][4] + no; //0
			deltas[10] = alphas[i][2] + betas[i][5] + en; //0
			deltas[11] = alphas[i][3] + betas[i][5] + un; //1
			
			deltas[12] = alphas[i][4] + betas[i][6] + un; //1
			deltas[13] = alphas[i][5] + betas[i][6] + en; //0
			deltas[14] = alphas[i][6] + betas[i][7] + no; //0
			deltas[15] = alphas[i][7] + betas[i][7] + bo; //1
			
			o0 =              Math.max( Math.max( deltas[0], deltas[3] ) , Math.max( deltas[4] , deltas[7] ));
			o0 = Math.max(o0, Math.max( Math.max( deltas[9], deltas[10]) , Math.max( deltas[13], deltas[14])));
			
			o1 =              Math.max( Math.max( deltas[1], deltas[2] ) , Math.max( deltas[5] , deltas[6] ));
			o1 = Math.max(o1, Math.max( Math.max( deltas[8], deltas[11]) , Math.max( deltas[12], deltas[15])));
			
			if (interleave)
				out[interleaver[i]] = o1-o0-un;
			else
				out[i] = o1-o0-un;
		}
		
		return out;
		
	}
	
	
	public static int[] get_interleaver(int len)
	{
		int[] interleaver_o = new int[len];
		
		int f1,f2;
		int index = find_interleaver_param_index(len);
		if (index < 0)
			throw new IllegalArgumentException("Interleaver Length not Supported");
		
		f1 = interleaver_parameters[index][1];
		f2 = interleaver_parameters[index][2];
		
		for (int i = 0; i < len; i++)		
			interleaver_o[i] = (f1*i+f2*i*i)%len;
		return interleaver_o;
	}
	
	public static int[] get_interleaver(int len, boolean termination)
	{
		int[] interleaver_o;
		if (termination)
			interleaver_o = new int[len+3];
		else
			interleaver_o = new int[len];
		
		int f1,f2;
		int index = find_interleaver_param_index(len);
		if (index < 0)
			throw new IllegalArgumentException("Interleaver Length not Supported");
		
		f1 = interleaver_parameters[index][1];
		f2 = interleaver_parameters[index][2];
		
		for (int i = 0; i < len; i++)		
			interleaver_o[i] = (f1*i+f2*i*i)%len;
		
		if (termination){
			interleaver_o[len] = len;
			interleaver_o[len+1] = len+1;
			interleaver_o[len+2] = len+2;
		}
		return interleaver_o;
	}
		
	private static int find_interleaver_param_index(int len)
	{
		for (int i = 0; i < interleaver_parameters.length; i++)
		{
			if (interleaver_parameters[i][0] == len)
				return i;				
		}
		return -1;
	}

	
	public static double[] systematic_subblock_deinterleave(double[] llrs_in)
	{
		int colTcSb = 32;
		int rowTcSb = (int) Math.ceil((double)llrs_in.length/colTcSb);
		int Nd = colTcSb*rowTcSb - llrs_in.length;
		
		boolean[] zeros = new boolean[llrs_in.length];
		
		double[] output = new double[llrs_in.length];

		int[] v0 = subBlockInterleaver1(zeros,colTcSb,rowTcSb,Nd);

		
		double[] llr_v0 = new double[v0.length];

		
		//input all systematic, then alternate v1/v2 until enough bits inputted

		int k = 0;  //total inputted
		int i = 0;  //array index
		
		//input systematic first
		while (k < llrs_in.length && i < v0.length)
		{			
			if (v0[i] >= 0)
			{
				llr_v0[i] = llrs_in[k];				
				k++;
			}
			i++;			
		}

		output = subBlockDeInterleaver1(llr_v0,colTcSb,rowTcSb,Nd);

		
		return output;
	}
	
	

	public static double[][] output_rate_dematching(double[] llrs_in, int D)
	{
		int colTcSb = 32;
		int rowTcSb = (int) Math.ceil((double)D/colTcSb);
		int Nd = colTcSb*rowTcSb - D;
		
		boolean[] zeros = new boolean[D];
		
		double[][] output = new double[D][3];

		int[] v0 = subBlockInterleaver1(zeros,colTcSb,rowTcSb,Nd);
		int[] v2 = subBlockInterleaver2(zeros,colTcSb,rowTcSb,Nd);
		
		double[] llr_v0 = new double[v0.length];
		double[] llr_v1 = new double[v0.length];
		double[] llr_v2 = new double[v2.length];
		
		//input all systematic, then alternate v1/v2 until enough bits inputted

		int k = 0;  //total inputted
		int i = 0;  //array index
		
		//input systematic first
		while (k < llrs_in.length && i < v0.length)
		{			
			if (v0[i] >= 0)
			{
				llr_v0[i] = llrs_in[k];				
				k++;
			}
			i++;			
		}
		
		//now parity
		i = 0;
		while (k < llrs_in.length && i < v0.length)
		{			
			if (v0[i] >= 0)
			{
				llr_v1[i] = llrs_in[k];				
				k++;
			}
			if (v2[i] >= 0 && k < llrs_in.length)
			{
				llr_v2[i] = llrs_in[k];				
				k++;
			}
			i++;			
		}
		
		output[0] = subBlockDeInterleaver1(llr_v0,colTcSb,rowTcSb,Nd);
		output[1] = subBlockDeInterleaver1(llr_v1,colTcSb,rowTcSb,Nd);
		output[2] = subBlockDeInterleaver2(llr_v2,colTcSb,rowTcSb,Nd);
		
		return output;
	}
	

	//As per TS 36.212 v10.0.0, Section 5.1.4.1.
	private static int[] subBlockInterleaver1(boolean[] in, int colTcSb, int rowTcSb, int Nd)
	{
		if (colTcSb*rowTcSb != in.length + Nd)
			throw new IllegalArgumentException("Check Input Array Lengths");
		int[] colPermPat = new int[] {0, 16, 8, 24, 4, 20, 12, 28, 2, 18, 10, 26, 6, 22, 14, 30,
		                  1, 17, 9, 25, 5, 21, 13, 29, 3, 19, 11, 27, 7, 23, 15, 31};
		
		int[][] y = new int[rowTcSb][colTcSb];
		int[] out = new int[rowTcSb*colTcSb];
		
		//fill matrix
		int k = 0;
		for (int i = 0; i < rowTcSb; i++){
			for (int j = 0; j < colTcSb; j++){
				if (Nd > 0)
				{
					y[i][j] = -2;	//null
					Nd--;
				}
				else
				{
					y[i][j] = in[k]?1:0;
					k++;
				}
			}
		}
		
		k=0;
		for (int j = 0; j < colTcSb; j++){		
			for (int i = 0; i < rowTcSb; i++){
				out[k] = y[i][colPermPat[j]];
				k++;
			}
		}
		return out;
	}
	
	//As per TS 36.212 v10.0.0, Section 5.1.4.1.
	private static int[] subBlockInterleaver2(boolean[] in, int colTcSb, int rowTcSb, int Nd)
	{
		if (colTcSb*rowTcSb != in.length + Nd)
			throw new IllegalArgumentException("Check Input Array Lengths");
		int[] colPermPat = new int[] {0, 16, 8, 24, 4, 20, 12, 28, 2, 18, 10, 26, 6, 22, 14, 30,
		                  1, 17, 9, 25, 5, 21, 13, 29, 3, 19, 11, 27, 7, 23, 15, 31};
		
		int[] pi = new int[colTcSb*rowTcSb];		
		int[] out = new int[rowTcSb*colTcSb];
		
		for (int i = 0; i < colTcSb*rowTcSb; i++)
			pi[i] = (colPermPat[(int)Math.floor(i/rowTcSb)] + colTcSb*(i%rowTcSb)+1) % (colTcSb*rowTcSb);
		
		for (int i = 0; i < colTcSb*rowTcSb; i++)
		{
			if (pi[i] < Nd)
				out[i] = -2;   //null
			else
				out[i] = in[pi[i]-Nd]?1:0;
			
		}
		
		return out;
	}

	//As per TS 36.212 v10.0.0, Section 5.1.4.1.
	private static double[] subBlockDeInterleaver1(double[] in, int colTcSb, int rowTcSb, int Nd)
	{
		if (colTcSb*rowTcSb != in.length)
			throw new IllegalArgumentException("Check Input Array Lengths");
		int[] colPermPat = new int[] {0, 16, 8, 24, 4, 20, 12, 28, 2, 18, 10, 26, 6, 22, 14, 30,
		                  1, 17, 9, 25, 5, 21, 13, 29, 3, 19, 11, 27, 7, 23, 15, 31};
		
		double[][] y = new double[rowTcSb][colTcSb];
		double[] out = new double[rowTcSb*colTcSb - Nd];
		
		//read back into matrix
		int k=0;
		for (int j = 0; j < colTcSb; j++){		
			for (int i = 0; i < rowTcSb; i++){
				y[i][colPermPat[j]] = in[k];
				k++;
			}
		}		
		
		//unfill matrix
		k = 0;
		for (int i = 0; i < rowTcSb; i++){
			for (int j = 0; j < colTcSb; j++){
				if (Nd > 0)
					Nd--;
				else
				{
					out[k] = y[i][j];
					k++;
				}
			}
		}		
	
		return out;
	}
		
	private static double[] subBlockDeInterleaver2(double[] in, int colTcSb, int rowTcSb, int Nd)
	{
		if (colTcSb*rowTcSb != in.length)
			throw new IllegalArgumentException("Check Input Array Lengths");
		int[] colPermPat = new int[] {0, 16, 8, 24, 4, 20, 12, 28, 2, 18, 10, 26, 6, 22, 14, 30,
		                  1, 17, 9, 25, 5, 21, 13, 29, 3, 19, 11, 27, 7, 23, 15, 31};
		
		int[] pi = new int[colTcSb*rowTcSb];		
		double[] out = new double[rowTcSb*colTcSb - Nd];
		
		
		for (int i = 0; i < colTcSb*rowTcSb; i++)
			pi[i] = (colPermPat[(int)Math.floor((double)i/rowTcSb)] + colTcSb*(i%rowTcSb)+1) % (colTcSb*rowTcSb);
		
		for (int i = 0; i < colTcSb*rowTcSb; i++)
		{
			if (pi[i] < Nd)
				;   //null
			else
				out[pi[i]-Nd] = in[i];
			
		}
		
		return out;
	}
	

	static int crc_xmodem_update (int crc, byte data)
	{
	    int i;

	    crc = crc ^ ((int)data << 8);
	    for (i=0; i<8; i++)
	    {
	        if ((crc & 0x8000) != 0)
	            crc = (crc << 1) ^ 0x1021;
	        else
	            crc <<= 1;
	    }

	    return crc & 0xFFFF;
	}
	

	public static byte hamming_decode84(byte input)
	{
		byte s = 0;
		byte o = input;
		
		if (  (input&(1<<7))>0  ^  (input&(1<<2))>0  ^  (input&(1<<1))>0  ^  (input&(1<<0))>0  )
			s |= (1<<3);
		if (  (input&(1<<6))>0  ^  (input&(1<<3))>0  ^  (input&(1<<1))>0  ^  (input&(1<<0))>0  )
			s |= (1<<2);
		if (  (input&(1<<5))>0  ^  (input&(1<<3))>0  ^  (input&(1<<2))>0  ^  (input&(1<<0))>0  )
			s |= (1<<1);
		if (  (input&(1<<4))>0  ^  (input&(1<<3))>0  ^  (input&(1<<2))>0  ^  (input&(1<<1))>0  )
			s |= (1<<0);
		
		return o;
	}
	
	public static byte hamming_encode84(byte input)
	{

		byte o = (byte) (input & 0xF);
		
		if (    (input&(1<<3))>0  ^  (input&(1<<2))>0  ^  (input&(1<<1))>0  )
			o |= (1<<4);
		if (    (input&(1<<3))>0  ^  (input&(1<<2))>0  ^  (input&(1<<0))>0  )
			o |= (1<<5);
		if (    (input&(1<<3))>0  ^  (input&(1<<1))>0  ^  (input&(1<<0))>0  )
			o |= (1<<6);
		if (    (input&(1<<2))>0  ^  (input&(1<<1))>0  ^  (input&(1<<0))>0  )
			o |= (1<<7);
		
		return o;
	}
	
}
