import java.util.Random;

import org.apache.commons.math3.complex.Complex;

import rtty.Turbo_decoder;
import rtty.Turbo_encoder;


public class turbo_test {

	/**
	 * @param args
	 * @throws LineUnavailableException 
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		
		
		byte o = Turbo_decoder.hamming_encode84((byte) 2);
		o = (byte) (o | (1<<3));
		Turbo_decoder.hamming_decode84(o);
		

		double[] llrs_a = new double[] {13.7917403717738,19.9813678457758,-21.1466528950995,-11.6617541165164,22.1381526378431,15.6318305943762,-5.90063601403228,16.8360361066166,-9.56122515161601,12.4345221080517,-21.5275683270557,-17.2488935535946,1.47938434926337,-6.44628750698945,8.77660430000461,5.35663591139688,-10.7480255197019,10.1024699949130,-8.28144080950538,12.7860396584500,2.56791662202844,-16.1566699878907,13.4590766372621,24.3370987015752,13.0898692907124,-14.6819731080785,-16.9716644984382,-14.0629980815448,-16.7682609076669,-6.56379369635259,-22.4863505978601,11.0599864819425,-14.8773523866563,-15.8966152185339,11.2888165243592,5.02327728466667,7.31153097961037,-7.39257822465166,12.4892393004764,18.7407322202952,8.36646921526048,13.2793450457068,-1.19826408942916,-15.7342392233724,-14.3275161850407,-5.17383694906878,-9.29880951476395,-1.23251603934327,-8.38753774940504,-13.1742246916078,19.0294430382835,-13.0962112693627,-12.1965445511175,16.0763872621313,-11.9549194540379,20.2728199833001,-12.0691245457708,-12.5035125837785,12.4548101752456,11.7280317481721,-14.8138069718299,-9.76958346343104,11.5436352499340,-7.77476068427691,-18.2655529187182,16.8119627721583,7.19836652548638,-25.3623497691011,-16.1120348324542,-12.6561405606018,-16.7896533599758,4.80266477181475,9.87822744105038,-27.1363139719950,11.5558663966468,-5.28496774745578,16.7845542161428,-9.85981384509803,15.2249918739325,-11.4027960922224,-17.2724769361728,14.2236671240289,22.8864290064633,-4.19433866586543,11.9185288364027,-13.7934589535854,-16.5508922713867,-1.71024868618634};
		double[] llrs_b = new double[] {8.49418317295546,17.4210061001646,-2.72274854398597,16.2001583873531,18.0861400452259,10.5689980658294,-0.659071398504053,-11.8638496049284,-16.1349743086229,-10.4237749839949,-11.2234494496883,-14.9251380716106,-5.63489831290271,13.8548355476132,3.53051253515150,-6.93519405896020,14.2739370975518,9.25571303243076,19.0864270501954,13.3334533353848,7.73711461977854,11.4714399594746,-13.8528080424369,22.5269390246829,15.2616011347274,12.2892695308032,12.2631092291143,10.5919963633926,-3.86312046816046,-23.3251492770391,0.817063095320097,17.3447581437288,-13.2635100919267,-16.0624471086285,12.5019223887747,-15.0856517374914,8.26062760574834,12.2108700041432,13.8718503941174,11.8526586133807,-13.2848475715063,7.28673096384963,-6.24877797448587,15.9451814702991,-9.37814442226219,-16.6060215134351,12.0466103780960,11.8506557265499,-13.0556317873453,-13.2707759157466,13.0551136190053,9.85866937646073,11.8302879888692,-12.0704241982105,11.1542743588396,-14.8738479094882,7.72280676223256,4.30363862523989,18.7554159263541,-22.4141634913452,11.3035066765699,-14.1028928038386,14.8901042388707,-14.8284196993678,9.59074250573692,12.1270723375723,15.0025901490634,11.8855577748220,14.5965386990183,10.4449055842993,-16.4988148346059,9.42345160437851,12.0664480960908,-16.0627330200598,-9.00488533991842,12.0579426550490,17.9013331448639,13.3934807858018,8.37725102237290,-20.7185490605222,-11.0732490546399,5.87898080583176,-16.2053940820410,-14.5749868880912,12.2746654397055,-18.1274852290648,17.4518752273312,-19.1137439152611};//,-12.2703015841407,-14.3912117237142,14.9349103225290};
		double[] llrs_c = new double[] {17.9474246813706,-9.03067349765117,8.52863129598948,-10.9114338542875,-16.6846986232343,18.0496302752532,2.84374804190973,-14.5335389138972,-8.18553945107854,-10.7768167218599,12.4773216744392,-16.6056490255247,13.8457301773139,-19.9130756765419,18.4197720856701,-18.4295585142458,-14.9559724950462,-14.1329872861607,-6.53867634527880,-18.9722650102917,-2.02870556349302,-8.99250053116482,-14.9651175118949,-5.01193762006728,10.7061651384574,-14.7101454851702,22.8328038730985,-7.00152340325588,-10.4673718761113,-13.6989063327365,14.7845583673277,-13.9076633386144,13.5629308169121,8.95038392614167,15.8361201832159,-15.3334665319965,11.3002491239470,-11.1154593618801,15.3007962524369,-7.45147695051217,-5.62535090026484,16.4624972356706,-10.3443121246261,11.3108415949424,8.51015552740980,14.8462851635274,17.9707542338775,14.9886145019332,-15.0866608940701,-14.6791510844786,11.1267583567413,15.8597755789111,-11.9535329926770,18.4923046227273,-5.83568753908683,-4.46348857665508,-17.3098779964943,-18.5769627438529,-3.33696216049953,18.9185677186362,-11.1807583223345,-21.3516022410926,-15.3971602057535,15.6443934275688,-11.1858946099513,14.3101656120238,-16.8740565678853,15.6527714017219,10.8050778296323,-17.0021398608468,-17.3248817238456,-13.4443514804022,15.9749769341052,15.8086830047143,-19.5102508214480,-18.7268519252309,-14.4669477562993,16.4508713043065,-17.0225837539877,23.5763833318361,12.1893983719854,-10.6264734370575,-18.3956898385110,-17.7948009811503,-17.6797212785914,-15.3828551136071,-6.91153846158852,-12.3014263709035};//,-13.1960973938023,-14.0322606335872,-6.72003307131861};
		
		boolean[] input = new boolean[] {false,false,true,true,false,false,true,false,true,false,true,true,false,true,false,false,true,false,true,false,false,true,false,false,false,true,true,true,true,true,true,false,true,true,false,false,false,true,false,false,false,false,false,true,true,true,true,false,true,true,false,true,true,false,true,false,true,true,false,false,true,true,false,true,true,false,false,true,true,true,true,false,false,true,false,true,false,true,false,true,true,false,false,true,false,true,true,true};
		
		double[] llrs_a1 = new double[] {-13.8749328999979, -11.5643475349668, 6.78522610771948, -18.4230074402116, -12.1216187452829, 16.2818561000456, 25.6534431734434, -16.0033931338079, -11.7068854697843, -13.0640356335692, 2.92652009167719, -14.8569935065012, -21.6758673238348, 16.8759757790738, -17.1156755484793, 13.1525509385178, 9.91027512067514, -11.1224818654894, -15.6685933924879, -10.1847155437557, -8.93031100749327, 21.2594503454208, -13.6255003340763, -23.4044682420033, -16.8720184730202, -5.83586187390580, -18.0417662463763, 17.4824527214534, -12.0251731787955, 19.8753120875510, -22.5119154865409, 11.6547412073579, 6.57396947644441, 27.2756166871757, 16.7997419383091, -5.71324878616012, -17.9714755647688, 10.2920990512886, -14.0195592157021, 18.1738939787530, 11.2514888108424, 16.1776773678856, -22.9692002689800, -14.4288819058527, 8.50668972479899, 4.71693375673768, 15.2040878705961, -11.2308058874847, -12.4807157480494, -19.3571555626627, 18.3200965706579, -10.8878014515564, -14.1533331375666, 12.7642402030912, 11.3313433672708, 3.84600847615536, -14.0858589642950, 8.46755846481746, -17.5742576113682, 6.83271824078307, -15.3327608242692, 2.57638615356534, -7.79929340106998, -10.0333468126160, -12.7498454013937, -12.8239999445079, -16.6636609350196, 17.7728263529714, 11.9790621920690, 9.05521418282233, -5.85200007420376, 11.5185720810509, -15.6117698178018, 11.1716082651449, -16.9139538592968, 7.01516355460223, 25.3542164656270, 20.9758227048982, -11.1022906805573, 6.32614004020377, 8.29603690490767, -13.5370302275299, -8.66849858995410, -19.3487381920626, -24.3677220343919, 5.36053712515963, -10.9716399580401, -10.6807062205102};
		double[] llrs_b1 = new double[] {-14.0476993411348, 14.7752580062421, 4.24844587364572, -10.2769155661861, -18.7494090965455, 12.9820289590793, -9.36793423086019, -11.0040860957156, -7.20375239938520, 17.7094106086043, 9.37521805012444, -11.3561866180203, -17.3990795367667, 6.00086634559277, 17.3007388223244, -12.6488599130989, 12.3728831240664, -8.06638311055845, -9.65851296920702, -10.8876919880973, -6.36067988078210, 17.3257040317026, 13.8550559792140, 9.17677812225414, 9.37196931919967, -6.65315566629921, -20.7561882954828, -12.7721476681967, 2.84692828236995, 17.7819439138648, 16.9833141828025, 12.6549556111545, 12.2928183210993, -25.1544567109754, 15.5722526684303, 1.62174679759608, -24.3144730021878, -12.2470653627221, -17.4197174253139, 14.7187988941885, 16.0541287367555, 16.9632769449465, -16.1254569986215, 14.9093604243594, 13.1552695959822, 16.8040227660104, -9.95238329723337, -8.13297094883741, -13.3127229837514, 11.9087265132231, 17.7179425181794, -23.3305321269549, -15.1870458985010, 6.25835889363750, -14.5734103231265, 15.9117945976415, 16.8022983256870, 7.54421437847281, -15.0184668643761, 13.3383092703904, 11.1811155783829, -11.1310436763383, -10.6375644917245, 7.97165163054801, 11.7597014896370, 1.92524254374491, -6.88824622513448, 9.48495162795860, 6.59406583414023, 11.3718365037691, -19.8348239478740, -12.7540189037197, 9.82911500272309, 23.6027576461931, 18.3752883869342, 0.0904364037602374, 14.8688675901683, -19.6813718580765, -13.9319702790572, 13.4760193517416, 16.4100136980129, -14.0224640888822, 20.5774806385981, -15.0680964495664, 14.2964093981039, -9.30567502016141, -12.2206347034126, 17.0800685699152};//, 14.2747866584427, 8.70506414028344, -21.7296578610399};
		double[] llrs_c1 = new double[] {-5.02269190401663, -12.8129115100704, -4.42046800062732, -14.7870416633280, -9.68441776114531, 12.3332874954376, 2.47919563230128, -17.5889706921139, -9.56834191117906, 12.3730480219788, 7.02218656571277, -15.7996278983035, -11.3941028475431, -17.6437318212575, 17.5528506604921, -15.8717088505655, -3.55101332569437, -18.0805503337350, 13.6509805039543, 4.99875205585760, -16.2887825114638, -15.6330017537316, 14.6677255616125, -7.91043091736681, 14.1604750438955, 10.7726645694877, -8.54742059428244, -8.63092179876670, 13.2537112004991, 15.5223341055940, 14.7253646526947, -17.6132661044696, 16.4695370612077, 9.34356310660424, 9.61156149153053, 13.5391060393662, 11.1024500696778, 11.9860893667973, -9.65462007363295, 17.9144007937674, 11.6534314313117, 14.2972445505697, -13.8477037535330, -11.4942994051273, 14.8621829387312, 9.54643927820259, -11.2667547863960, -9.62572747956955, -12.1848262258199, 21.3497522159229, -15.7099925402591, -16.3563250271027, -21.4505375917223, -8.06962570950458, -8.28791628719035, -13.0509503471689, -8.13001566143245, -11.7251313393050, 14.1117075871764, -12.0810287891201, -10.4362682816720, 13.1604456616818, 1.37044334006854, 6.78109619983835, -21.9757412614912, -18.3864330563399, -18.1483370886766, 10.4681713414385, -13.4964692680262, 11.5499450251333, 15.3718787051219, -10.6912052896404, 16.4275923954490, 21.5932642978711, 18.8007895972471, 6.19467414157458, 0.935089580730621, 17.1855857519857, -21.8818844236648, -12.3133411522611, -12.4706582744330, 23.8511730207174, 12.3009813734020, -15.2008105650554, 13.8351705020333, -11.4127776888752, -12.2968018890342, -15.7101085527856};//, 6.49979149205061, -11.0571981039785, -19.4033853199584};
		boolean[] input1 = new boolean[] {true, true, false, true, true, false, false, true, true, true, false, true, true, false, true, false, false, true, true, true, true, false, true, true, true, true, true, false, true, false, true, false, false, false, false, true, true, false, true, false, false, false, true, true, false, false, false, true, true, true, false, true, true, false, false, false, true, false, true, false, true, false, true, true, true, true, true, false, false, false, true, false, true, false, true, false, false, false, true, false, false, true, true, true, true, false, true, true};
		
		for (int i = 0; i < 88; i++)
		{
			llrs_a[i] = llrs_a[i]*-1;
			llrs_b[i] = llrs_b[i]*-1;
			llrs_c[i] = llrs_c[i]*-1;
			llrs_a1[i] = llrs_a1[i]*-1;
			llrs_b1[i] = llrs_b1[i]*-1;
			llrs_c1[i] = llrs_c1[i]*-1;
		}
		
		Turbo_decoder tc = new Turbo_decoder();
		
		
		double[] d0_1 = new double[]{-20.6948946066454, -21.5763464011190, 22.0687254399962, -18.0664500669786, -25.5239605224383, 19.8572027467271, 19.0034655698124, -16.0728093934551, -13.1248659776760, -13.0235047195960, 14.5787882085358, -19.5576469699874, -27.7353508182248, 12.9967362930698, -20.0908596988138, 29.7521957912942, 15.1739189124484, -17.6958965710909, -21.4759032235394, -12.9723236874358, -26.9435093620457, 20.2536451483827, -16.5488553206272, -13.0783598567282, -10.2694475984333, -19.5033682889241, -29.4923280804931, 15.3471898755166, -26.7694881433869, 34.9307245584322, -23.9455171441449, 24.7843561196962, 18.8290838692364, 25.6742235138776, 15.2044183994295, -28.9267400244559, -29.0540584665174, 23.1387622752976, -21.1706394071313, 18.8060669768658,0,0,0,0};
		double[] d1_1 = new double[]{-1.63380919738828, 25.2728245606725, 28.7792220864316, -26.7479497988764, -23.0147911308609, 18.3221968084303, -13.0921992061257, -21.8069909827436, -15.6052870249922, 7.05526626002240, 17.8068900721757, -25.2624883024330, -30.0335083951240, 23.2640151063959, 21.8330292405487, -19.8354927355501, 11.6025611293419, -12.9081410479064, -17.8301324488848, -21.9411932714220, -19.9025498133146, 18.3885186377735, 8.96504032755874, 18.2387303701311, 14.7832270501625, -26.2478820233195, -27.3698935422794, -23.4260047575855, 7.36668052226903, 26.1530475105852, 23.3405408994845, 19.9206717949692, 19.8273167562428, -25.1015087275689, 26.4978652581117, 19.2039483260094, -24.5719369232368, -11.4904341261875, -21.4707530668130, 16.3177224205189,0,0,0,0};
		double[] d2_1 = new double[]{-18.3922489231670, -26.0090333528076, -21.0754211634849, 19.1226626966597, 16.6787616029466, -9.39630914196744, -25.5926597523758, -23.1110350744120, 15.5390341101128, 12.6123068873894, 18.8302172638893, -21.7829183737104, 29.7359996881417, 18.4706498353453, -26.7861520952498, -9.89430217845277, -12.2294274086584, -21.5014977167348, -29.5845823473702, 17.2320795146123, -21.0349172763051, -18.2994087094418, 18.3937855764910, 22.8552633224724, -17.5659918567858, -27.9668627149105, 14.0449442929873, 15.3547613934733, -23.2630203358588, -22.0773925569620, -19.9685346321207, 0.866557901344412, 17.1536454041868, -12.1802319945107, 13.2930725187090, -14.1350669753294, -17.8292358878003, 19.8638231207447, 21.2027873426235, -29.9575172931849,0,0,0,0};
		
		double[] zk1 = new double[]{18.1874240585517, 14.6783698398480, 12.9547701897321};
		double[] zpk1 = new double[]{19.5121803209180, -9.89120546898732, -19.4247453752240};
		double[] xk1 = new double[]{26.5093435872141, 25.5039283241654, 20.0548477269606};
		double[] xpk1 = new double[]{6.16486077794052, 5.36166823039331, -19.5413443279860};
		
		
		
		int k = 40;
		d0_1[k] = xk1[0];  d0_1[k+1] = zk1[1];    d0_1[k+2] = xpk1[0];  d0_1[k+3] = zpk1[1];
		d1_1[k] = zk1[0];  d1_1[k+1] = xk1[2];    d1_1[k+2] = zpk1[0];  d1_1[k+3] = xpk1[2];
		d2_1[k] = xk1[1];  d2_1[k+1] = zk1[2];    d2_1[k+2] = xpk1[1];  d2_1[k+3] = zpk1[2];
		
		for (int i = 0; i < d0_1.length; i++)
		{
			d0_1[i] = d0_1[i]*-1;
			d1_1[i] = d1_1[i]*-1;
			d2_1[i] = d2_1[i]*-1;
		}
		
		boolean[] out1 = tc.decode(d0_1, d1_1, d2_1, false, true);
		/*
		System.out.println("turbo test 1");
		
		boolean[] out1 = tc.decode(llrs_a1, llrs_b1, llrs_c1, 0, false);
		
		int errors = 0;
		for (int i = 0; i < 88; i++)
		{
			if (input1[i] != out1[i])
				errors++;
		}
		System.out.println("errors: " + errors);
		
		System.out.println("turbo test 2");
		
		boolean[] out = tc.decode(llrs_a, llrs_b, llrs_c, 0, false);
		
		for (int i = 0; i < 88; i++)
		{
			if (input[i] != out[i])
				errors++;
		}
		System.out.println("errors: " + errors); */
		
		
		
		
		
		boolean termination = true;
		int snr = 4;
		int snr_stop = 14;
		int inter_len = 40;
		
		
		int bits = inter_len;
		if (termination)
			bits += 4;		
		int bits_rm = bits*3;
		Random rdn = new Random(4);
		
		int[] interleaver = Turbo_decoder.get_interleaver(inter_len);
		while(snr < snr_stop)
		{
			
			//get some random numbers
			//boolean[] a = new boolean[inter_len];
			boolean[] a = new boolean[inter_len];// {false, true, false, false, true, true, true, false, true, false, false, false, true, true, true, false, true, false, false, false, true, true, true, true, true, false, false, true, false, true, false, false, false, true, true, true, true, false, true, false};
			boolean[] b = new boolean[inter_len];
			boolean[] c;
			boolean[] d;
			double[] mod_r = new double[bits_rm];
			
			for (int i = 0; i < inter_len; i++)
			{
				if (rdn.nextGaussian() > 0)
					a[i] = true;				
			}
			
			int[] aa = new int[]{254,79,143,73,90};
			int maskh = 0x80;
			int j = 0;
			for (int i = 0; i < 5*8; i++)
			{
				a[i] = ((aa[j] & 0x80) == 0x80) ? true : false;
				aa[j] <<= 1;

				maskh >>= 1;
				if (maskh == 0){
					maskh = 0x80;
					j++;
				}
			}
			
			/*
			//interleave
			for (int i = 0; i < inter_len; i++)
				b[i] = a[interleaver[i]];
			
			//encode
			boolean[] xk = new boolean[3];
			boolean[] zk = new boolean[3];
			boolean[] zpk = new boolean[3];
			boolean[] xpk = new boolean[3];
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
			
			
			//boolean[] aa = new boolean[a.length];
			//boolean[] cc = new boolean[c.length];
			//for (int h = 0; h < aa.length; h++)
			//	cc[h] = true;
			
			boolean[] d0 = new boolean[bits];
			boolean[] d1 = new boolean[bits];
			boolean[] d2 = new boolean[bits];
			
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
				k = inter_len;
				d0[k] = xk[0];  d0[k+1] = zk[1];    d0[k+2] = xpk[0];  d0[k+3] = zpk[1];
				d1[k] = zk[0];  d1[k+1] = xk[2];    d1[k+2] = zpk[0];  d1[k+3] = xpk[2];
				d2[k] = xk[1];  d2[k+1] = zk[2];    d2[k+2] = xpk[1];  d2[k+3] = zpk[2];
			}
			
			//rate matching
			boolean[] out_rm = output_rate_matching(d0,d1,d2,bits_rm);
			*/
			
			//for (int i = 39; i >= 40-16; i--)
			//	a[i] = false;
			
			int[] c_in = new int[(int) Math.ceil((double)(a.length+4)/8)];
			int[] c_sys = new int[(int) Math.ceil((double)(a.length+4)/8)];
			int[] c_par = new int[(int) Math.ceil(((double)2*(a.length+4))/8)];


			
			boolean[] out_rm = Turbo_encoder.encode(a,bits_rm,true);
			
			//put into arrays so can be used to test the c code
			int mask = 0x80;
			int c_i = 0;
			for (int i = 0; i < a.length; i++)
			{
				if (a[i])
					c_in[c_i] |= mask;
				mask >>= 1;
				if (mask == 0)
				{
					mask = 0x80;
					c_i++;
							
				}
			}		
			mask = 0x80;
			c_i = 0;
			for (int i = 0; i < a.length+4; i++)
			{
				
				if (out_rm[i])
					c_sys[c_i] |= mask;
				mask >>= 1;
				if (mask == 0)
				{
					mask = 0x80;
					c_i++;
							
				}
			}
			mask = 0x80;
			c_i = 0;
			for (int i = 0; i < 2*(a.length+4); i++)
			{
				if (out_rm[i+(a.length+4)])
					c_par[c_i] |= mask;

				mask >>= 1;
				if (mask == 0)
				{
					mask = 0x80;
					c_i++;
							
				}				
			}
			
			
			System.out.print("uint8_t input[] = {");
			for (int i = 0; i < c_in.length; i++)
				System.out.print(c_in[i] + ",");
			System.out.println("0,0};");
			
			System.out.print("uint8_t answers[] = {");
			for (int i = 0; i < c_sys.length; i++)
				System.out.print(c_sys[i] + ",");
			System.out.println("0,0};");
			
			System.out.print("uint8_t answerp[] = {");
			for (int i = 0; i < c_par.length; i++)
				System.out.print(c_par[i] + ",");
			System.out.println("0,0};");
			
			//bpsk modulate
			for (int i = 0; i < bits_rm; i++)
			{
				if (out_rm[i])
					mod_r[i] = 1;
				else
					mod_r[i] = -1;	
			}
			
			//send over channel
			Complex[] rx_r = new Complex[bits_rm];
			Complex noise;
			double scaling = Math.sqrt((1/(Math.pow(10,((double)snr/10))))/2);
			double N0 = (1/(Math.pow(10,((double)snr/10))));
			for (int i = 0; i < bits_rm; i++)
			{
				rx_r[i] = new Complex(mod_r[i],0);
				noise = new Complex(scaling*rdn.nextGaussian(),scaling*rdn.nextGaussian());
				rx_r[i] = rx_r[i].add(noise);
			}
			
			//demod
			double[] r_dem = new double[bits_rm];
			for (int i = 0; i < bits_rm; i++)
			{
				r_dem[i] = Math.pow((rx_r[i].add(1).abs()),2) - Math.pow((rx_r[i].add(-1).abs()),2);
				r_dem[i] = r_dem[i]/N0;
			}
			
			//deratematch
			double[][] rdm = Turbo_decoder.output_rate_dematching(r_dem,bits); 
			/*int redm1 = 0;
			int redm2 = 0;
			int redm3 = 0;
			for (int i = 0; i < bits; i++)
			{
				if ((rdm[0][i] > 0) ^ a[i])
					redm1++;
				if ((rdm[1][i] > 0) ^ c[i])
					redm2++;
				if ((rdm[2][i] > 0) ^ d[i])
					redm3++;
			}
			System.out.println("rematch errors: " + redm1 + "  " + redm2 + "  " + redm3); */
			
			
			
			//decode
			//boolean[] out2 = tc.decode(a_dem, c_dem, d_dem, 0);
			boolean[] out2 = tc.decode(rdm[0], rdm[1], rdm[2], false, termination);
			
			//count errors
			int ers = 0;
			for (int i = 0; i < inter_len; i++)
			{
				if (a[i] != out2[i])
					ers++;
			}
			System.out.println("SNR: " + snr + "  errors: " + ers);
			
			snr++; 
		} 
		
	}
	
	/*
	
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

	private static double[][] output_rate_dematching(double[] llrs_in, int D)
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
	
	*/
}