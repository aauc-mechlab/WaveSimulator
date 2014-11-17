/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serverrobot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RefineryUtilities;

/**
 *
 * @author Håkon
 */
public class DynamicGraph {

    // time series
    private final TimeSeries roll;
    private final TimeSeries pitch;
    private final TimeSeries setPointX;
    private final TimeSeries setPointY;
    private final TimeSeries setPointZ;
    private final TimeSeries actPointX;
    private final TimeSeries actPointY;
    private final TimeSeries actPointZ;

    public DynamicGraph() {
        this.roll = new TimeSeries("Roll", Millisecond.class);
        this.pitch = new TimeSeries("Pitch", Millisecond.class);
        this.setPointX = new TimeSeries("Set point", Millisecond.class);
        this.setPointY = new TimeSeries("Set point", Millisecond.class);
        this.setPointZ = new TimeSeries("Set point", Millisecond.class);
        this.actPointX = new TimeSeries("Actual point", Millisecond.class);
        this.actPointY = new TimeSeries("Actual point", Millisecond.class);
        this.actPointZ = new TimeSeries("Actual point", Millisecond.class);
    }
    
    public JFrame getDynamicGraphFrame(String type){
        JFrame frame = new JFrame();
        JPanel panel = createPlot(type);
        frame.add(panel);
        return frame;
    }

    public JPanel createPlot(String type) {
        switch (type) {
            case ("Platform"):
                return createPlatformPlot();
            case ("Position"):
                return createPositionPlot();
            default:
                throw new IllegalArgumentException("Type did not match Platform or Position");
        }
    }

    public JPanel createPlatformPlot() {
        final TimeSeriesCollection dataset1 = createPlatformRollCollection();
        final TimeSeriesCollection dataset2 = createPlatformPitchCollection();
        final JFreeChart chart1 = createChart(dataset1, "Roll",-5,5);
        final JFreeChart chart2 = createChart(dataset2, "Pitch",-5,5);
        final ChartPanel chartPanel1 = new ChartPanel(chart1);
        final ChartPanel chartPanel2 = new ChartPanel(chart2);
        final JPanel panel = new JPanel(new BorderLayout());
        final GridLayout grid = new GridLayout(2, 1);
        panel.setLayout(grid);
        panel.add(chartPanel1);
        panel.add(chartPanel2);
        panel.setPreferredSize(new Dimension(1200, 600));
        return panel;
    }

    public JPanel createPositionPlot() {
        final TimeSeriesCollection dataset1 = createXPositionCollection();
        final TimeSeriesCollection dataset2 = createYPositionCollection();
        final TimeSeriesCollection dataset3 = createZPositionCollection();
        final JFreeChart chart1 = createChart(dataset1, "X",0.4,0.8);
        final JFreeChart chart2 = createChart(dataset2, "Y", -0.4,0.2);
        final JFreeChart chart3 = createChart(dataset3, "Z",0.5,1);
        final ChartPanel chartPanel1 = new ChartPanel(chart1);
        final ChartPanel chartPanel2 = new ChartPanel(chart2);
        final ChartPanel chartPanel3 = new ChartPanel(chart3);
        final JPanel panel = new JPanel(new BorderLayout());
        final GridLayout grid = new GridLayout(3, 1);
        panel.setLayout(grid);
        panel.add(chartPanel1);
        panel.add(chartPanel2);
        panel.add(chartPanel3);
        panel.setPreferredSize(new Dimension(1200, 900));
        return panel;
    }

    public TimeSeriesCollection createPlatformRollCollection() {
        final TimeSeriesCollection coll = new TimeSeriesCollection();
        coll.addSeries(roll);
        return coll;
    }

    public TimeSeriesCollection createPlatformPitchCollection() {
        final TimeSeriesCollection coll = new TimeSeriesCollection();
        coll.addSeries(pitch);
        return coll;
    }

    public TimeSeriesCollection createXPositionCollection() {
        final TimeSeriesCollection coll = new TimeSeriesCollection();
        coll.addSeries(setPointX);
        coll.addSeries(actPointX);
        return coll;
    }

    public TimeSeriesCollection createYPositionCollection() {
        final TimeSeriesCollection coll = new TimeSeriesCollection();
        coll.addSeries(setPointY);
        coll.addSeries(actPointY);
        return coll;
    }

    public TimeSeriesCollection createZPositionCollection() {
        final TimeSeriesCollection coll = new TimeSeriesCollection();
        coll.addSeries(setPointZ);
        coll.addSeries(actPointZ);
        return coll;
    }

    public JFreeChart createChart(XYDataset dataset, String pos, double minRange, double maxRange) {
        final JFreeChart chart = ChartFactory.createTimeSeriesChart(
                pos,
                "Time (ms)",
                "Displacement",
                dataset,
                true,
                true,
                false
        );
        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(new Color(232, 232, 232));
        plot.setDomainGridlinePaint(Color.BLACK);
        plot.setRangeGridlinePaint(Color.BLACK);
        plot.getRenderer().setSeriesPaint(0, Color.BLUE);
        plot.getRenderer().setSeriesPaint(1, Color.RED);

        ValueAxis axis = plot.getDomainAxis();
        //axis.setAutoRange(true);
        axis.setFixedAutoRange(20000.0);  // 60 seconds
        axis = plot.getRangeAxis();
        axis.setRange(minRange,maxRange);

        chart.setBackgroundPaint(Color.white);
        return chart;
    }

    public synchronized void updateValue(String type, double val) {
        switch (type) {
            case ("roll"):
                this.roll.addOrUpdate(new Millisecond(), val);
                break;
            case ("pitch"):
                this.pitch.addOrUpdate(new Millisecond(), val);
                break;
            case ("setX"):
                this.setPointX.addOrUpdate(new Millisecond(), val);
                break;
            case ("setY"):
                this.setPointY.addOrUpdate(new Millisecond(), val);
                break;
            case ("setZ"):
                this.setPointZ.addOrUpdate(new Millisecond(), val);
                break;
            case ("actX"):
                this.actPointX.addOrUpdate(new Millisecond(), val);
                break;
            case ("actY"):
                this.actPointY.addOrUpdate(new Millisecond(), val);
                break;
            case ("actZ"):
                this.actPointZ.addOrUpdate(new Millisecond(), val);
                break;
            default:
                throw new IllegalArgumentException("Did not match roll,pitch,zetX,zetY,setZ,actX,actY,actZ");
        }
    }
}
