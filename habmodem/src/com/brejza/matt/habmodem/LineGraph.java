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
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import ukhas.Payload;
import ukhas.Telemetry_string;

import android.content.Context;
import android.view.View;

public class LineGraph {
	
	private ConcurrentHashMap<String,Payload> _data;
	List<String> listpayloads = new ArrayList<String>();
	List<String> listfields = new ArrayList<String>();

	
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
	
	public void addField(String field)
	{
		if (!listfields.contains(field))
		{
			listfields.add(field);
		}
	}
	
	public void clearField(String field)
	{
		if (listfields.contains(field))
		{
			listfields.remove(field);
		}
	}
	
	
	public View getView(Context context)
	{
		long minTime = System.currentTimeMillis() + 60*60*1000;
		long maxTime = 0;
		
		if(listpayloads.size() < 1)
			return null;
		if (Math.min(2, listfields.size()) == 0)
			return null;
		
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		XYMultipleSeriesRenderer mrend = new XYMultipleSeriesRenderer(Math.min(2, listfields.size()));
		int cnt = 0;
		for (int f = 0; f < Math.min(2, listfields.size()); f++)
		{
			String field = listfields.get(f);
			for (int i = 0; i < listpayloads.size(); i++)
			{
				String call = listpayloads.get(i);
				if (_data.containsKey(call))
				{
					int findex = _data.get(call).telemetryConfig.getIndex(field);
					if (findex >= 0){
						TreeMap<Long,Telemetry_string> sen = _data.get(call).data;
						if (sen.size() > 1)
						{
							if (sen.firstKey().longValue() < minTime)
								minTime = sen.firstKey().longValue();
							if (sen.lastKey().longValue() > maxTime)
								maxTime = sen.lastKey().longValue();
							
							XYSeries series = new XYSeries(_data.get(call).callsign + " - " + field,f);
							
							for (TreeMap.Entry<Long,Telemetry_string> entry : sen.entrySet())
							{				
								if (field == "altitude")
								{
									if (entry.getValue().coords.alt_valid)						
										series.add(entry.getKey(),(int)entry.getValue().coords.altitude);	
								}
								else
								{
									if (entry.getValue().getExtraFieldExists(findex))
										series.add(entry.getKey(),entry.getValue().getExtraFields(findex));
								}
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
			SimpleDateFormat dtf = new SimpleDateFormat("HH:mm");

			mrend.addXTextLabel(minTime + i*inc, dtf.format(dt));
		}
		
		mrend.setChartTitle("Altitude Plot");
		mrend.setYTitle("Altitude (m)");
		
		mrend.setShowGrid(true);
		
		mrend.setYLabelsAngle(270);
		
		mrend.setXLabels(0);
				
		return ChartFactory.getLineChartView(context, dataset, mrend);
		
	}
	
}
