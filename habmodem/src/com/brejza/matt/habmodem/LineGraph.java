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
import org.achartengine.renderer.BasicStroke;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import ukhas.Payload;
import ukhas.Telemetry_string;

import android.content.Context;
import android.graphics.Paint.Align;
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
	
	public boolean addField(String field)
	{
		int differentFields = 0;
		boolean gotTemp = false;
		/*
		if (listfields.size() < 2){
			listfields.add(field);
			return true;
		}
		else
			return false; */
		
		if (!listfields.contains(field))
		{
			for (int i = 0; i < listfields.size(); i++)
			{
				if (isTemperature(listfields.get(i)))
				{
					if (!gotTemp)
					{
						gotTemp = true;
						differentFields++;
					}
				}
				else
				{
					differentFields++;
				}				
			}
			
			
			if (differentFields < 2 || (gotTemp && differentFields == 2 && isTemperature(field)))
				{
					listfields.add(field);
					return true;
				}
				else
					return false;	
		}
		
		return true; 
	}
		
	private boolean isTemperature(String str)
	{
		return (str.contains("temperature"));
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
		
		int differentFields = 0;
		int tempLoc = -1;
		for (int i = 0; i < listfields.size(); i++){
			if (isTemperature(listfields.get(i))){
				if (tempLoc < 0){
					tempLoc = i;
					differentFields++;
				}
			}
			else
				differentFields++;						
		}
		
		
		
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		XYMultipleSeriesRenderer mrend = new XYMultipleSeriesRenderer( Math.min(2, differentFields));
		int cnt = 0;
		for (int f = 0; f < listfields.size(); f++)
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
							
							int axis = 0;
							if (!isTemperature(listfields.get(f)))
								axis = f;	
							else
								axis = tempLoc;
							
							XYSeries series = new XYSeries(_data.get(call).callsign + " - " + field,axis);
							
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
									
							//if (!isTemperature(listfields.get(f)))
								dataset.addSeries(axis,series);	
							//else
							//	dataset.addSeries(f,series);
							
							
							XYSeriesRenderer renderer = new XYSeriesRenderer();
							renderer.setColor(_data.get(call).colour);
							renderer.setLineWidth(4);

							//stroke style
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
		mrend.setYTitle("Altitude (m)",0);
	//	if (Math.min(2, differentFields) > 1){
	//		mrend.addYTextLabel(10, "New Test", 1);
	//		mrend.setYTitle("Hours", 1);
	//	}
		
		mrend.setShowGrid(true);
		
		mrend.setYLabelsAngle(270);
		
		
		
		mrend.setXLabels(0);
				
		return ChartFactory.getLineChartView(context, dataset, mrend);
		
	}
	
}
