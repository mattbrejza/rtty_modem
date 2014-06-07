package rtty;

public class Turbo_encoder {

	private static int[][] interleaver_parameters = new int[][]{{40,3,10}, {48,7,12}, {56,19,42}, {64,7,16}, {72,7,18}, {80,11,20}, {88,5,22}, {96,11,24}, {104,7,26}, {112,41,84}, {120,103,90}, {128,15,32}, {136,9,34}, {144,17,108}, {152,9,38}, {160,21,120}, {168,101,84}, {176,21,44}, {184,57,46}, {192,23,48}, {200,13,50}, {208,27,52}, {216,11,36}, {224,27,56}, {232,85,58}, {240,29,60}, {248,33,62}, {256,15,32}, {264,17,198}, {272,33,68}, {280,103,210}, {288,19,36}, {296,19,74}, {304,37,76}, {312,19,78}, {320,21,120}, {328,21,82}, {336,115,84}, {344,193,86}, {352,21,44}, {360,133,90}, {368,81,46}, {376,45,94}, {384,23,48}, {392,243,98}, {400,151,40}, {408,155,102}, {416,25,52}, {424,51,106}, {432,47,72}, {440,91,110}, {448,29,168}, {456,29,114}, {464,247,58}, {472,29,118}, {480,89,180}, {488,91,122}, {496,157,62}, {504,55,84}, {512,31,64}, {528,17,66}, {544,35,68}, {560,227,420}, {576,65,96}, {592,19,74}, {608,37,76}, {624,41,234}, {640,39,80}, {656,185,82}, {672,43,252}, {688,21,86}, {704,155,44}, {720,79,120}, {736,139,92}, {752,23,94}, {768,217,48}, {784,25,98}, {800,17,80}, {816,127,102}, {832,25,52}, {848,239,106}, {864,17,48}, {880,137,110}, {896,215,112}, {912,29,114}, {928,15,58}, {944,147,118}, {960,29,60}, {976,59,122}, {992,65,124}, {1008,55,84}, {1024,31,64}, {1056,17,66}, {1088,171,204}, {1120,67,140}, {1152,35,72}, {1184,19,74}, {1216,39,76}, {1248,19,78}, {1280,199,240}, {1312,21,82}, {1344,211,252}, {1376,21,86}, {1408,43,88}, {1440,149,60}, {1472,45,92}, {1504,49,846}, {1536,71,48}, {1568,13,28}, {1600,17,80}, {1632,25,102}, {1664,183,104}, {1696,55,954}, {1728,127,96}, {1760,27,110}, {1792,29,112}, {1824,29,114}, {1856,57,116}, {1888,45,354}, {1920,31,120}, {1952,59,610}, {1984,185,124}, {2016,113,420}, {2048,31,64}, {2112,17,66}, {2176,171,136}, {2240,209,420}, {2304,253,216}, {2368,367,444}, {2432,265,456}, {2496,181,468}, {2560,39,80}, {2624,27,164}, {2688,127,504}, {2752,143,172}, {2816,43,88}, {2880,29,300}, {2944,45,62}, {3008,157,188}, {3072,47,96}, {3136,13,28}, {3200,111,240}, {3264,443,204}, {3328,51,104}, {3392,51,212}, {3456,451,192}, {3520,257,220}, {3584,57,336}, {3648,313,228}, {3712,271,232}, {3776,179,236}, {3840,331,120}, {3904,363,244}, {3968,375,248}, {4032,127,168}, {4096,31,64}, {4160,33,130}, {4224,43,264}, {4288,33,134}, {4352,477,408}, {4416,35,138}, {4480,233,280}, {4544,357,142}, {4608,337,480}, {4672,37,146}, {4736,71,444}, {4800,71,120}, {4864,37,152}, {4928,39,462}, {4992,127,234}, {5056,39,158}, {5120,39,80}, {5184,31,96}, {5248,113,902}, {5312,41,166}, {5376,251,336}, {5440,43,170}, {5504,21,86}, {5568,43,174}, {5632,45,176}, {5696,45,178}, {5760,161,120}, {5824,89,182}, {5888,323,184}, {5952,47,186}, {6016,23,94}, {6080,47,190}, {6144,263,480}};

	
	public static boolean[] encode(boolean[] input, int num_out, boolean termination)
	{
		boolean[] a = input;			//original input sequence
		boolean[] b = new boolean[input.length]; 	//interleaved input sequence
		boolean[] c = null, d = null;				//urc outputs
		
		//termination bits, see Fig5.1.3-2
		boolean[] xk = new boolean[3];				//uncoded termination
		boolean[] zk = new boolean[3];				//coded termination
		boolean[] zpk = new boolean[3];				//uncoded termination interleaved
		boolean[] xpk = new boolean[3];				//coded termination interleaved
		
		int inter_len = input.length;				//interleaver length, number of bits before termination added
		int bits_per_stream = inter_len;			//number of bits in each of d0,d1,d2
		if (termination)
			bits_per_stream += 4;
		
		int[] interleaver = get_interleaver(inter_len);
		
		//interleave
		for (int i = 0; i < inter_len; i++)
			b[i] = a[interleaver[i]];
		
		
		//encode
		if (termination)
		{
			c = urc_encoder_lte(a,xk,zk);
			d = urc_encoder_lte(b,xpk,zpk);
		}
		else
		{
			c = urc_encoder_lte(a);
			d = urc_encoder_lte(b);
		}
		
		
		
		boolean[] d0 = new boolean[bits_per_stream];
		boolean[] d1 = new boolean[bits_per_stream];
		boolean[] d2 = new boolean[bits_per_stream];
		
		//copy into output streams
		for (int i = 0; i < inter_len; i++)
		{
			d0[i] = a[i];
			d1[i] = c[i];
			d2[i] = d[i];
		}
		
		//termination multiplexing
		if (termination)
		{
			//5.1.3.2.2
			int k = inter_len;
			d0[k] = xk[0];  d0[k+1] = zk[1];    d0[k+2] = xpk[0];  d0[k+3] = zpk[1];
			d1[k] = zk[0];  d1[k+1] = xk[2];    d1[k+2] = zpk[0];  d1[k+3] = xpk[2];
			d2[k] = xk[1];  d2[k+1] = zk[2];    d2[k+2] = xpk[1];  d2[k+3] = zpk[2];
		}
		
		//rate matching
		return output_rate_matching(d0,d1,d2,num_out);
		
	}

	private static boolean[] urc_encoder_lte(boolean[] in)
	{
		boolean s1,s2,s3,s1_plus,s2_plus,s3_plus;
		s1 = false;
		s2 = false;
		s3 = false;
		boolean[] out = new boolean[in.length];
		
		for (int i = 0; i < in.length; i++)
		{
			s1_plus = in[i]^s2^s3;
	        s2_plus = s1;
	        s3_plus = s2;
		
	       
	        out[i] = s1_plus^s1^s3;
	        

	        s1 = s1_plus;
	        s2 = s2_plus;
	        s3 = s3_plus;
		}
		
		return out;
	}
	
	private static boolean[] urc_encoder_lte(boolean[] in, boolean[] termination_un, boolean[] termination_en)
	{
		boolean s1,s2,s3,s1_plus,s2_plus,s3_plus;
		s1 = false;
		s2 = false;
		s3 = false;
		boolean[] out = new boolean[in.length];
		//termination_en = new boolean[3];
		//termination_un = new boolean[3];
		
		for (int i = 0; i < in.length; i++)
		{
			s1_plus = in[i]^s2^s3;
	        s2_plus = s1;
	        s3_plus = s2;
		
	       
	        out[i] = s1_plus^s1^s3;
	        

	        s1 = s1_plus;
	        s2 = s2_plus;
	        s3 = s3_plus;
		}
		
		for (int i = 0; i < 3; i++)
		{			       
			s1_plus = false;
			s2_plus = s1;
			s3_plus = s2;				
			        
			termination_un[i] = s2^s3;
			termination_en[i] = s1_plus^s1^s3;			        
			        
			s1 = s1_plus;
			s2 = s2_plus;
			s3 = s3_plus;
		}
		
		return out;
	}
	private static boolean[] output_rate_matching(boolean[] r0, boolean[] r1, boolean[] r2, int totalOut)
	{
		if (r0.length != r1.length)
			throw new IllegalArgumentException("Check Input Array Lengths");
		if (r0.length != r2.length)
			throw new IllegalArgumentException("Check Input Array Lengths");
		
		int D = r0.length;
		int colTcSb = 32;
		int rowTcSb = (int) Math.ceil((double)D/colTcSb);
		int Nd = colTcSb*rowTcSb - D;
		
		int[] v0 = subBlockInterleaver1(r0,colTcSb,rowTcSb,Nd);
		int[] v1 = subBlockInterleaver1(r1,colTcSb,rowTcSb,Nd);
		int[] v2 = subBlockInterleaver2(r2,colTcSb,rowTcSb,Nd);
		
		//output all systematic, then alternate v1/v2 until enough bits outputted
		boolean[] out = new boolean[totalOut];
		int k = 0;  //total outputted
		int i = 0;  //array index
		
		//output systematic first
		while (k < totalOut && i < v0.length)
		{			
			if (v0[i] >= 0)
			{
				if (v0[i] == 0)
					out[k] = false;
				else
					out[k] = true;
				k++;
			}
			i++;			
		}
		//now parity
		i = 0;
		while (k < totalOut && i < v1.length)
		{			
			if (v1[i] >= 0)
			{
				if (v1[i] == 0)
					out[k] = false;
				else
					out[k] = true;
				k++;
			}
			if (v2[i] >= 0 && k < totalOut)
			{
				if (v2[i] == 0)
					out[k] = false;
				else
					out[k] = true;
				k++;
			}
			i++;			
		}
		
		return out;
		
	}

	//As per TS 36.212 v10.0.0, Section 5.1.4.1.
	public static int[] subBlockInterleaver1(boolean[] in, int colTcSb, int rowTcSb, int Nd)
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




}
