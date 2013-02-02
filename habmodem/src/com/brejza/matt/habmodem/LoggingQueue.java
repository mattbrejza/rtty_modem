package com.brejza.matt.habmodem;

import java.util.concurrent.ConcurrentLinkedQueue;

public class LoggingQueue extends ConcurrentLinkedQueue<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int _maxLen;
	long _lastStrTime = 0;
	String _lastString = "";
	public int lingerTime = 5;
	
	public LoggingQueue(int maxLen){
		_maxLen = maxLen;
	}
	
	@Override
	public boolean offer (String s)
	{
		if (this.size() >= _maxLen)
			this.poll();		
		
		return super.offer(s);
	}
	
	public String offerAndReturn (String s)
	{
		if (this.size() >= _maxLen)
			this.poll();
		super.offer(s);
		
		String out;
		
		if (System.currentTimeMillis() - (lingerTime*1000L) < _lastStrTime)
			out = _lastString + "\n" + s;		
		else
			out = s;
			
		_lastStrTime = System.currentTimeMillis();
		_lastString  =s;
		
		return out;	
	}

}
