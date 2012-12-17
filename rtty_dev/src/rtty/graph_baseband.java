package rtty;

import java.awt.BasicStroke;
import java.awt.Color;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class graph_baseband extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	private int _active = 0;

	private XYSeries fftseries = new XYSeries("FFT");
	private XYSeriesCollection dataset;
	private XYSeries series1 = new XYSeries("line1");
	private XYSeries series2 = new XYSeries("line2");
	private ChartPanel chartPanel;
	JFreeChart chart;
	XYPlot plot;


	public graph_baseband() {
		

    }

	public void showGraph()
	{
		
		
		dataset = new XYSeriesCollection();
		dataset.addSeries(fftseries);
	//	dataset.addSeries(series1);
		//dataset.addSeries(series2);
		
		chart = ChartFactory.createXYLineChart(
	            "XY Chart",                // Title
	            "x-axis",                  // x-axis Label
	            "y-axis",                  // y-axis Label
	            dataset,                   // Dataset
	            PlotOrientation.VERTICAL,  // Plot Orientation
	            true,                      // Show Legend
	            true,                      // Use tooltips
	            false);
		
		chartPanel = new ChartPanel(chart);
        // default size
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        // add it to our application
        setContentPane(chartPanel);
        plot = chart.getXYPlot();

        
        this.pack();
		this.setVisible(true);
		
		
		_active = 1;
        
	}
	
	public void drawfft(double[] data)
	{
		if (!fftseries.isEmpty())
			fftseries.clear();
		

		for (int i = 0; i < data.length; i++)
		{
			fftseries.add(i, 10*Math.log10(data[i]));			
		}
		
		if (_active == 0)
			showGraph();
		
	}
	
	public void drawlinedual(double[] data1,double[] data2,int count)
	{
		if (!series1.isEmpty())
		{
			series1.clear();
			series2.clear();
		}
		
		for (int i = 0; (i < data1.length) && (i < count); i++)
		{
			series1.add(i, (data1[i]));	
			series2.add(i, (data2[i]));	
		}
		
		if (_active == 0)
			showGraph();
		
		
	}
	
	public void clearlines()
	{
		if (_active > 0)
			dataset.removeAllSeries();
	}
	
	public void drawsingle(double[] data)
	{
		if (!fftseries.isEmpty())
			fftseries.clear();
		

		for (int i = 0; i < data.length; i++)
		{
			fftseries.add(i, (data[i]));			
		}
		
		if (_active == 0)
			showGraph();
		
	}
    
	public void addMarkers(double pos1, double pos2)	
	{
		if (!fftseries.isEmpty())
		{
			Marker m1 = new ValueMarker(pos1);
			Marker m2 = new ValueMarker(pos2);
			
			
			
		
			
	        m1.setStroke(new BasicStroke(2));
	        m2.setStroke(new BasicStroke(2));
	        m1.setPaint(Color.RED);
	        m2.setPaint(Color.GREEN);
	        plot.addDomainMarker(m1);
	        plot.addDomainMarker(m2);
		}
		
		
	}
	
	public void addMarkers(double[] peak)
	{
		if (_active > 0)
		{
			for (int i =0; i< peak.length; i++)
			{
				Marker m1 = new ValueMarker(peak[i]);			
		        m1.setStroke(new BasicStroke((float) .5));
		        m1.setPaint(Color.GRAY);
		        plot.addDomainMarker(m1);
			}
		}		
	}
	
	
	public void addMarkers(double peak)
	{
		if (_active > 0)
		{			
			Marker m1 = new ValueMarker(peak);			
	        m1.setStroke(new BasicStroke((float) .5));
	        m1.setPaint(Color.GRAY);
	        plot.addDomainMarker(m1);
		}
	}
	
	public void addMarkers(double peak, Color color)
	{
		if (_active > 0)
		{			
			Marker m1 = new ValueMarker(peak);			
	        m1.setStroke(new BasicStroke((float) 1));
	        m1.setPaint(color);
	        plot.addDomainMarker(m1);
		}
	}
	
	public void clearMarkers()
	{
		if (_active > 0)
			plot.clearDomainMarkers();
		
	}

	
}