package rtty;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class ConfidenceCalculator {

	int _historyLen;
	
	Queue<double[]> freqHistory = new LinkedList<double[]>();
	
	public enum State {SIG_LOST,SIG_JUST_FOUND,SIG_TRACKING,SIG_DROPPED};
	State state = State.SIG_LOST;
	
	int _sampleRate = 8000;
	
	int confidence=0;  //{0..100}
	
	int samplesSinceFullSearch = 0;
	int samplesSinceStateChange = 0;
	
	double _lastPowerMax = 0;
	double _lastPowerAverage = 0;
	
	
	double[] lastDecent = {-1,-1};
	double[] currentBest = {-1,-1};
	
	public ConfidenceCalculator (int samplerate)
	{
		_historyLen = 3;
		_sampleRate = samplerate;
	}
	
	public void samplesElapsed(int samples)
	{
		samplesSinceFullSearch += samples;
		samplesSinceStateChange += samples;
	}
	
	public boolean putFrequencies ( double f1, double f2)
	{
		boolean out = false;
		State entryState = state;
		samplesSinceFullSearch = 0;
		switch(state)
		{
			case SIG_LOST :
				if (f1 > 0 && f2 > 0){
					state = State.SIG_JUST_FOUND;
					currentBest[0] = f1;
					currentBest[1] = f2;
					freqHistory.clear();
					freqHistory.offer(new double[] {f1,f2});
					out = true;
				}
				break;
				
				
			case SIG_JUST_FOUND : 
				if (samplesSinceStateChange > 10 *_sampleRate){
					state = State.SIG_LOST;
				}
				if(!freqsValid(f1,f2))
					break;
				freqHistory.offer(new double[] {f1,f2});
				if (freqHistory.size() > _historyLen)
					freqHistory.poll();
				double[] f = selectFromHistory();
				if (f == null)
					break;
				currentBest = f;
				lastDecent = currentBest;
				state = State.SIG_TRACKING;
				freqHistory.clear();
				out = true;
				break;
				
				
			case SIG_TRACKING :
				if(!freqsValid(f1,f2))
					break;
				if (freqSimilar(currentBest,new double[] {f1,f2}))
				{
					currentBest[0] = f1;
					currentBest[1] = f2;
					lastDecent = currentBest;
					confidence = confidence + (100-confidence)/2 ;
				}
				else
				{
					confidence = confidence/2 ;
					freqHistory.offer(new double[] {f1,f2});
					if (freqHistory.size() > _historyLen)
						freqHistory.poll();
					if (confidence < 20) {
						double[] ff = selectFromHistory();
						if (ff == null)
							state = State.SIG_DROPPED;
						else {
							state = State.SIG_JUST_FOUND;
							currentBest = ff;
						}
					}
				}
				break;
				
				
			case SIG_DROPPED :
				if(freqsValid(f1,f2)){
					if (freqSimilar(lastDecent,new double[] {f1,f2})) {
						state = State.SIG_TRACKING;
						currentBest[0] = f1;
						currentBest[1] = f2;
						lastDecent = currentBest;
						confidence = 50;
					}
					else {
						state = State.SIG_JUST_FOUND;
						currentBest[0] = f1;
						currentBest[1] = f2;
						out = true;
					}
				}
				else {
					if (samplesSinceStateChange > 10 *_sampleRate){
						state = State.SIG_LOST;
					}
				}
				break;
			default : 
				System.out.println("WHY AM I HERE!?!?!?!?!?!?!?!?! :o");
				break;
		}
		if (entryState != state){
			samplesSinceStateChange = 0;
			System.out.println("STATE : " + state);
		}
			
		return out;
	}
	
	private double[] selectFromHistory(){
		if (freqHistory.size() < 2)
			return null;
		
		double[] lows = new double[freqHistory.size()];
		double[] highs = new double[freqHistory.size()];
		
		{
		int i =0;

		Iterator<double[]> iterator = freqHistory.iterator();
		while(iterator.hasNext()){
			double[] v = iterator.next();
			lows[i] = v[0];
			highs[i] =  v[1];
			i++;
		}		
		} 
		
		for (int i = 0; i < freqHistory.size()-1; i++){
			for (int j = i+1; j < freqHistory.size(); j++){
				if (freqSimilar(new double[] {lows[i],highs[i]},new double[] {lows[j],highs[j]}))
					return new double[] {(lows[i]+lows[j])/2,(highs[i]+highs[j])/2};
			}
		}
		return null;
		
	}
	 /*
	private boolean freqsValid(double[] in)
	{
		if (in[1] <= 0 || in[0] <= 0)
			return false;
		else
			return true;
	} */
	private boolean freqsValid(double f1, double f2)
	{
		if (f1 <= 0 || f2 <= 0)
			return false;
		else
			return true;
	}
	
	private boolean freqSimilar(double[] pair1, double[] pair2)
	{
		//similar if shift  < 200Hz difference
		//   && each frequency 500 to a max of 800
		
		if (pair2[1] <= 0 || pair2[0] <= 0 || pair1[0] <= 0 || pair1[1] <= 0)
			return false;
			
		double shiftdiff = Math.abs(Math.abs(pair1[1]-pair1[0]) - Math.abs(pair2[1]-pair2[0]));
		double f1diff = Math.abs(pair1[0] - pair2[0]);
		double f2diff = Math.abs(pair1[1] - pair2[1]);
		
		if (shiftdiff > 200/(double)_sampleRate)
			return false;
		if (f1diff > 400/(double)_sampleRate)
			return false;
		if (f2diff > 400/(double)_sampleRate)
			return false;
		if (f1diff+f2diff > 700/(double)_sampleRate)
			return false;
		
		return true;
	}
	
	public double getFrequencies(int i)
	{
		return currentBest[i];
	}
	
	public void gotDecode()
	{
		confidence = 100 ;
	}
	
	public void putPowerLevels(double lastMax, double lastAverage)
	{
		
		if (lastAverage < _lastPowerAverage/100 && state == State.SIG_TRACKING){
			state = State.SIG_DROPPED;
			System.out.println("STATE : " + state);
		}
		
		_lastPowerMax = lastMax;
		_lastPowerAverage = lastAverage;
		
		
	}
	
	public void AFCUpdate(double f1, double f2)
	{
		currentBest[0] = f1;
		currentBest[1] = f2;
		
		if (state == State.SIG_TRACKING){
			lastDecent[0] = f1;
			lastDecent[1] = f2;
		}
	}
	
	public boolean fullSearchDue()
	{
		switch(state)
		{
			case SIG_LOST :
				if (samplesSinceFullSearch > _sampleRate / 3)    // 1/3 sec
					return true;
				else
					return false;
			case SIG_JUST_FOUND : 
				if (samplesSinceFullSearch > 1 * _sampleRate)    //1sec
					return true;
				else
					return false;
			case SIG_TRACKING :
				if (samplesSinceFullSearch > 5 * _sampleRate)    //5sec
					return true;
				else
					return false;
			case SIG_DROPPED :
				if (samplesSinceFullSearch > _sampleRate / 3)    // 1/3 sec
					return true;
				else
					return false;
			default : 
				System.out.println("WHY AM I HERE!?!?!?!?!?!?!?!?! :o");
				return false;
		}
	}
	
	public State getState(){
		return state;
	}
	
	//getters/setters
	public int getConfidence(){
		return confidence;
	}
}
