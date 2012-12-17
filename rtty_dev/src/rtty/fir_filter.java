package rtty;

public class fir_filter {

	double[] _cooef;
	double[] _delay_line;
	int  _delay_ptr=0;
	
	
	public fir_filter(double[] cooef) {
		// TODO Auto-generated constructor stub
		_cooef = cooef;
		_delay_line = new double[cooef.length-1];
	}
	
	public double step (double input)
	{
		double out = _cooef[0] * input;
		
		for (int i = 1; i < _cooef.length -1; i++)
		{
			out = out + (_delay_line[_delay_ptr] * _cooef[i]);
			_delay_ptr =_delay_ptr-1;
			if (_delay_ptr < 0)
			{
				_delay_ptr = _delay_line.length - 1;			
			}
			
		}
		out = out + (_delay_line[_delay_ptr] * _cooef[_cooef.length -1]);
		_delay_line[_delay_ptr] = input;
		
		
		return out;
	}



}
