package ukhas;

import java.util.ArrayList;
import java.util.List;

public class TelemetryConfig {
	public enum GPSFormat {NMEA,DECIMAL,INT, UNKNOWN};
	public enum DataType {INT, FLOAT, STRING, IGNORE};
	
	public int GPSIntPowerOffset = 0;	
	public GPSFormat gpsFormat = GPSFormat.UNKNOWN;
	
	private List<String> _fieldNames = new ArrayList<String>();
	private List<DataType> _fieldTypes = new ArrayList<DataType>();
	
	private List<Double> _fieldScale = new ArrayList<Double>();
	private List<Double> _fieldOffset = new ArrayList<Double>();
	private List<Integer> _fieldRound = new ArrayList<Integer>();
	
	public int getIndex(String field)
	{
		return _fieldNames.indexOf(field);
	}
	
	public int getTotalFields()
	{
		return _fieldNames.size();
	}
	public String getFieldName(int index)
	{
		return _fieldNames.get(index);
	}
	public DataType getFieldDataType(int index)
	{
		return _fieldTypes.get(index);
	}
	public double adjust(int index, double valuein)
	{
		double m = _fieldScale.get(index).doubleValue();
		double c = _fieldOffset.get(index).doubleValue();
		return m*valuein + c;
	}
	public double getFieldScale(int index)
	{
		return _fieldScale.get(index);
	}
	public double getFieldOffset(int index)
	{
		return _fieldOffset.get(index);
	}
	public int getFieldRound(int index)
	{
		return _fieldRound.get(index);
	}
	public int addField(String name, DataType datatype)
	{
		_fieldTypes.add(datatype);
		_fieldNames.add(name);
		_fieldScale.add(Double.valueOf(1));
		_fieldOffset.add(Double.valueOf(0));
		_fieldRound.add(Integer.valueOf(-1));
		return _fieldNames.size()-1;
	}
	public void addFilter(String sourceField, String scale, String offset, String round)
	{
		int fieldindex = _fieldNames.indexOf(sourceField);
		if (fieldindex < 0)
			return;
		
		int iround;
		double doffset;
		double dscale;
		
		if (!scale.equals("")){
			try{
				dscale = Double.parseDouble(scale);
				_fieldScale.set(fieldindex, dscale);
			}
			catch(Exception e)
			{
				
			}
		}
		
		if (!offset.equals("")){
			try{
				doffset = Double.parseDouble(offset);
				_fieldOffset.set(fieldindex, doffset);
			}
			catch(Exception e)
			{
				
			}
		}
		
		if (!round.equals("")){
			try{
				iround = Integer.parseInt(round);
				_fieldRound.set(fieldindex, iround);
			}
			catch(Exception e)
			{
				
			}
		}		
	}	
}
