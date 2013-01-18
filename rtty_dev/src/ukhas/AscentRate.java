package ukhas;

public class AscentRate {

	
	long time1=0;
	long time2=0;
	double alt1=0;
	double alt2=0;
	
	boolean valid1 = false;
	boolean valid2 = false;
	
	public AscentRate(){
		
	}

	public void AddData(long timestamp, double altitude)
	{
		if (timestamp > time1){
			if (time1>time2){
				time2 = time1;
				time1 = timestamp;
			}
			else
			{
				time1 = timestamp;
			}				
		}
		else if(timestamp > time2){
			time2 = timestamp;							
		} 
	}
	
	public double getAscentRate()
	{
		long dt = (time2-time1)/1000L;
		if (!valid1 || !valid2)
			return 0;
		return Math.abs((alt2-alt1)/dt);
	}
}
