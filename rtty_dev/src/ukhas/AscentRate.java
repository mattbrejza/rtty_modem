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

	public void addData(long timestamp, double altitude)
	{
		if (timestamp > time1){
			if (time1>time2){
				time2 = time1;
				alt2 = alt1;
				
				time1 = timestamp;
				alt1 = altitude;
				
				valid2 = true;
				valid1 = true;
			}
			else
			{
				time1 = timestamp;
				alt1 = altitude;
				valid1 = true;
			}				
		}
		else if(timestamp > time2){
			time2 = timestamp;
			alt2 = altitude;
			valid2 = true;
		} 
	}
	
	public double getAscentRate()
	{
		long dt = (time2-time1)/1000L;
		if (!valid1 || !valid2)
			return 0;
		return (alt2-alt1)/dt;
	}
	public boolean valid()
	{
		return valid1 && valid2;
	}
}
