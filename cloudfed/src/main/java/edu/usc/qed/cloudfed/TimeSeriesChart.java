package edu.usc.qed.cloudfed;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYDataset;

//https://www.javatpoint.com/jfreechart-timeseries-chart
public class TimeSeriesChart extends JFrame {
    public JFreeChart chart;
    private static final long serialVersionUID = 1L;
    public TimeSeriesChart (String title, XYDataset data) {
        super (title);
        XYDataset dataset = data; 
        chart = ChartFactory.createTimeSeriesChart(title, "time", "rejection rate", dataset);
        ChartPanel panel = new ChartPanel(chart);  
        setContentPane(panel);  
    }
}