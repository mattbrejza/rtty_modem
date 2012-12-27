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

public class moving_average {

	private double[] _elements;
	private int _ptr;
	private double _current_total;
	private int _length;
	
	public moving_average(int length) {
		
		_elements = new double[length];
		_length = length;
		
	}
	
	public void init(double in)
	//sets the moving average value
	{
		for (int i=0; i < _elements.length; i++)
		{
			_elements[i] = in;
		}
		_current_total = in;
	}
	
	public double update(double in)
	{
		double off = _elements[_ptr];
		_elements[_ptr] = in;
		_ptr = ( _ptr + 1) % _length;
		
		_current_total = _current_total - off/_length + in/_length;
		
		
		return _current_total;
	}
	
	public double getMA()
	{
		return _current_total;
	}

}
