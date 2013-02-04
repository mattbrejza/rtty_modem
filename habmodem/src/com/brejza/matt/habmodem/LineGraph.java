package com.brejza.matt.habmodem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.achartengine.ChartFactory;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import ukhas.Payload;
import ukhas.Telemetry_string;

import android.content.Context;
import android.view.View;

public class LineGraph {
	
	private ConcurrentHashMap<String,Payload> _data;
	List<String> listpayloads = new ArrayList<String>();

	
	public LineGraph(ConcurrentHashMap<String,Payload> data)
	{
		_data = data;
	}
	
	public void putPayload(String call)
	{
		call = call.toUpperCase();
		listpayloads.add(call);
	}
	
	public void togglePayload(String call)
	{
		call = call.toUpperCase();
		if (listpayloads.contains(call))
			listpayloads.remove(call);
		else
			listpayloads.add(call);
	}
	
	public void addPayload(String call)
	{
		call = call.toUpperCase();
		if (listpayloads.contains(call))
			;
		else
			listpayloads.add(call);
	}
	
	public void clearPayload(String call)
	{
		call = call.toUpperCase();
		if (listpayloads.contains(call))
			listpayloads.remove(call);
		else
			;
	}
	
	
	public View getView(Context context)
	{
		long minTime = System.currentTimeMillis() + 60*60*1000;
		long maxTime = 0;
		
		if(listpayloads.size() < 1)
			return null;
		
		
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		XYMultipleSeriesRenderer mrend = new XYMultipleSeriesRenderer();
		int cnt = 0;
		for (int i = 0; i < listpayloads.size(); i++)
		{
			String call = listpayloads.get(i);
			if (_data.containsKey(call))
			{
				TreeMap<Long,Telemetry_string> sen = _data.get(call).data;
				if (sen.size() > 1)
				{
					if (sen.firstKey().longValue() < minTime)
						minTime = sen.firstKey().longValue();
					if (sen.lastKey().longValue() > maxTime)
						maxTime = sen.lastKey().longValue();
					
					TimeSeries series = new TimeSeries(_data.get(call).callsign);
					
					for (TreeMap.Entry<Long,Telemetry_string> entry : sen.entrySet())
					{					
						if (entry.getValue().coords.alt_valid)						
							series.add(entry.getKey(),(int)entry.getValue().coords.altitude);	
					}
								
					dataset.addSeries(series);			
					XYSeriesRenderer renderer = new XYSeriesRenderer();
					renderer.setColor(_data.get(call).colour);
					renderer.setLineWidth(4);
					mrend.addSeriesRenderer(renderer);
					cnt++;
				}
			}
		}
		
		if (cnt == 0)
			return null;

		//mrend.addXTextLabel(System.currentTimeMillis(), "custom label");
		int steps = 13;
		long inc = (maxTime-minTime)/steps;
		for (int i = 0; i < steps; i++)
		{
			Date dt = new Date(minTime + i*inc);
			SimpleDateFormat dtf = new SimpleDateFormat("hh:mm");

			mrend.addXTextLabel(minTime + i*inc, dtf.format(dt));
		}
		
		mrend.setChartTitle("Altitude Plot");
		mrend.setYTitle("Altitude (m)");
		
		mrend.setYLabelsAngle(270);
		
		mrend.setXLabels(0);
				
		return ChartFactory.getLineChartView(context, dataset, mrend);
		
	}
	
}
