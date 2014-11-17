package serverrobot;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Plots of the platform angles, setpoint and actualRobot position.
 *
 * @author Humlen
 */
public class Graph {

    private File file;
    private BufferedReader br;
    private String line;
    private ArrayList<String> lines;
    private ArrayList<Double> xAct = new ArrayList<>();
    private ArrayList<Double> yAct = new ArrayList<>();
    private ArrayList<Double> zAct = new ArrayList<>();
    private ArrayList<Double> xSet = new ArrayList<>();
    private ArrayList<Double> ySet = new ArrayList<>();
    private ArrayList<Double> zSet = new ArrayList<>();
    private ArrayList<Double> time = new ArrayList<>();

    private JFrame frame;
    private JFreeChart chart1;
    private JFreeChart chart2;
    private JFreeChart chart3;
    private ChartPanel panel;
    private ChartPanel chartPanel1;
    private ChartPanel chartPanel2;
    private ChartPanel chartPanel3;

    final XYSeriesCollection dataset1 = new XYSeriesCollection();
    final XYSeriesCollection dataset2 = new XYSeriesCollection();
    final XYSeriesCollection dataset3 = new XYSeriesCollection();

    /**
     * constructor 1 for taking in a text file and plot the graphs.
     *
     * @param file
     */
    public Graph(File file) {
        this.file = file;
        lines = new ArrayList<>();
        openFile();
        getValuesFromList();
        startPlottingGraph();
    }

    /**
     * Constructor 2 for taking in a Array of string and plot the graphs.
     *
     * @param strings
     */
    public Graph(ArrayList<String> strings) {
        this.lines = strings;
        getValuesFromList();
        startPlottingGraph();
    }

    /**
     * Converting from string to Double.
     */
    public void getValuesFromList() {
        for (String txt : lines) {
            //  System.out.println(txt);
            String[] strings = txt.split(",");
            time.add(Double.parseDouble(strings[0]));
            xSet.add(Double.parseDouble(strings[1]));
            ySet.add(Double.parseDouble(strings[2]));
            zSet.add(Double.parseDouble(strings[3]));
            xAct.add(Double.parseDouble(strings[4]));
            yAct.add(Double.parseDouble(strings[5]));
            zAct.add(Double.parseDouble(strings[6]));
        }
    }

    /**
     * Read from file and add it to the lines arraylist.
     */
    public void openFile() {
        try {
            br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            if (line == null) {
                //        System.out.println("Done reading");
                br.close();
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Graph.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Graph.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Start the plotting of the Graphs.
     */
    public void startPlottingGraph() {
        frame = new JFrame("Plotted Graph");
        createDataSets();

        chart1 = createChart(dataset1, "X");
        chart2 = createChart(dataset2, "Y");
        chart3 = createChart(dataset3, "Z");
        chartPanel1 = new ChartPanel(chart1);
        chartPanel2 = new ChartPanel(chart2);
        chartPanel3 = new ChartPanel(chart3);

        JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout());
        panel1.add(chartPanel1, BorderLayout.NORTH);

        JPanel panel2 = new JPanel();
        panel2.setBackground(Color.WHITE);
        JButton takePic = new JButton("Save as Image");
        panel2.add(takePic);

        panel1.add(chartPanel2, BorderLayout.CENTER);
        panel1.add(chartPanel3, BorderLayout.SOUTH);

        JPanel panel = new JPanel();
        GridLayout grid = new GridLayout(3, 1);
        panel.setLayout(grid);
        panel.add(chartPanel1);
        panel.add(chartPanel2);
        panel.add(chartPanel3);
//        panel.add(takePic);

        frame.setContentPane(panel);
        frame.setSize(1400, 800);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    /**
     * Creating datasets to use in the plots.
     */
    private void createDataSets() {

        final XYSeries series1 = new XYSeries("X Desired");
        for (int i = 0; i < time.size(); i++) {
            series1.add(time.get(i), xSet.get(i));
        }
        final XYSeries series2 = new XYSeries("X Actual");
        for (int i = 0; i < time.size(); i++) {
            series2.add(time.get(i), xAct.get(i));
        }
        final XYSeries series3 = new XYSeries("Y Desired");
        for (int i = 0; i < time.size(); i++) {
            series3.add(time.get(i), ySet.get(i));
        }
        final XYSeries series4 = new XYSeries("Y Actual");
        for (int i = 0; i < time.size(); i++) {
            series4.add(time.get(i), yAct.get(i));
        }
        final XYSeries series5 = new XYSeries("Z Desired");
        for (int i = 0; i < time.size(); i++) {
            series5.add(time.get(i), zSet.get(i));
        }
        final XYSeries series6 = new XYSeries("Z Actual");
        for (int i = 0; i < time.size(); i++) {
            series6.add(time.get(i), zAct.get(i));
        }

        dataset1.addSeries(series1);
        dataset1.addSeries(series2);

        dataset2.addSeries(series3);
        dataset2.addSeries(series4);

        dataset3.addSeries(series5);
        dataset3.addSeries(series6);
    }

    /**
     * Returns a JFreeChart that is used to plot the graphs.
     * 
     * @param dataset
     * @param graphName
     * @return 
     */
    private JFreeChart createChart(final XYDataset dataset, String graphName) {
        final JFreeChart chart = ChartFactory.createXYLineChart(
                graphName, // chart title
                "Time (ms)", // x axis label
                "Displacement (m)", // y axis label
                dataset, // data
                PlotOrientation.VERTICAL,
                true, // include legend
                true, // tooltips
                false // urls
        );

        chart.setBackgroundPaint(Color.white);

        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(new Color(232, 232, 232));

        plot.setDomainGridlinePaint(Color.BLACK);
        plot.setRangeGridlinePaint(Color.BLACK);

        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setBaseShapesVisible(false);
        renderer.setBaseStroke(new BasicStroke(100));
        plot.setRenderer(renderer);

        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        return chart;
    }

}
