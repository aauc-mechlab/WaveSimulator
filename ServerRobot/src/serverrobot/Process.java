package serverrobot;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.swing.JFrame;
import org.ejml.simple.SimpleMatrix;
import org.jfree.ui.RefineryUtilities;

/**
 * Process thread to calculate new position to the robot.
 *
 * @author Håkon
 */
public class Process extends Thread {

    private final AccReader accReader;
    private final ServerJoystick serverJoystick;
    private final MyEvent event;
    private final Communication communication;
    private final Modbus serverZOffset;
    private DynamicGraph dynamicGraph;
    private double[] platformAngles = new double[3];
    private double[] robotAngles = new double[6];
    private double[] setPoint = new double[3];
    private double[] actualRobotPos = new double[3];
    private double[] joystickPos = new double[3];
    private double[] error = new double[3];
    private double zOffset = 0;
    private DatagramSocket clientSocket;
    private byte[] sendData;
    private final String ip = "192.168.2.7";  // laptop/joystick/gui address
    private final int port = 4321;
    private InetAddress ipAddress;
    private final double scaleFactor = 0.01;
    private int counter = -1;
    private int count = 0;
// Limit the set points world coordinates for robot
    private final double 
            x_MIN = 0.55,
            x_MAX = 0.70,
            y_MIN = -0.2,
            y_MAX = 0.15,
            z_MIN = 0.55,
            z_MAX = 0.75;

    private double roll, pitch;
    private int listSize = 5000;  // number of points in the list that gets saved
    private ArrayList<String> list = new ArrayList<>(listSize);
    long T0 = 0;
    long T1;

    /**
     * Constructor creates an Process object.
     *
     * @param accReader
     * @param server
     * @param event
     * @param communication
     * @param serverOffset
     */
    public Process(AccReader accReader, ServerJoystick server, MyEvent event, Communication communication, Modbus serverOffset) {
        this.accReader = accReader;
        this.serverJoystick = server;
        this.event = event;
        this.communication = communication;
        this.serverZOffset = serverOffset;
        Startup();
        // Create a set point in absolute coordinates
        setPoint[0] = 0.6;
        setPoint[1] = -0.2;
        setPoint[2] = 0.7;
        System.out.println("Set point: " + Arrays.toString(setPoint));
    }

    /**
     * Start UDP client.
     */
    public void Startup() {
        try {
            clientSocket = new DatagramSocket();
            System.out.println("Client started!");
            sendData = new byte[1024];
            try {
                ipAddress = InetAddress.getByName(ip);
            } catch (UnknownHostException ex) {
                System.out.println("Error on ip address!");
            }
        } catch (SocketException ex) {
            System.out.println("Error on.. ");
        }
    }

    /**
     * The process that calculates new the new value to the robot.
     */
    @Override
    public void run() {
        T0 = System.currentTimeMillis();
        String temp = "";
        while (true) {

            // if loop to avoid the process to run multiple times before the 
            // communication thread send the new position to the robot
            if ((counter == -1) || (event.getState() == MyEventState.UP)) {
                counter = 10;
                event.resetState();
                try {
                    // Waiting for reciving threads to read in a new value.
                    event.awaitFlags(MyEventState.UP, MyEventState.UP, MyEventState.UP, MyEventState.UP);

                    platformAngles = accReader.getPlatformPos();    // these angles is not in use now, now we read angles directly from the platform
                    robotAngles = communication.getAngels();
                    joystickPos = serverJoystick.getJoystickPos();
                    zOffset = serverZOffset.getZOffset() / 1000;  // convert zOffset from millimeters to meters
                    // if you are going to use the measured angles to calculate the actual robot pos
//                    roll = platformAngles[0];
//                    pitch = platformAngles[1];
                    // if you are going to use the actual roll and pitch from the PLC to calculate the actual robot pos
                    roll = serverZOffset.getActualRoll();
                    pitch = serverZOffset.getActualPitch();
                    


                    // System.out.println("zOffset from plc: " + zOffset);
                    // avoid the center position of the joistick to something else than 0
                    for (int i = 0; i < 3; i++) {
                        if ((joystickPos[i] > 0) && (joystickPos[i] <= 0.3)) {
                            joystickPos[i] = 0;
                        }
                        if ((joystickPos[i] < 0) && (joystickPos[i] >= -0.3)) {
                            joystickPos[i] = 0;
                        }
                    }

                    // calculate new set point from joystick movement
                    setPoint = changeSetPointPos();
                    // calculate how much the robot must move to keep it selfe on the set point
                    calculateError();

                    // create a string with values to be stored in a .csv file, or get directly plotted in a graph
                    synchronized (this) {
                        temp = "" + (System.currentTimeMillis() - T0) + "," + setPoint[0] + "," + setPoint[1] + "," + setPoint[2] + "," + actualRobotPos[0] + "," + actualRobotPos[1] + "," + actualRobotPos[2];
                        if (list.size() == listSize) {
                            list.remove(0);
                        }
                        list.add(temp);
                    }

                    event.setErrorFlag();
                    // send live feed to the client (actual position, set point...) 
                    sendInfoToClient();
                    
                    // update values in the live graph
                    if(dynamicGraph != null){
                        //System.out.println("Update values");
                        dynamicGraph.updateValue("roll", platformAngles[0]);
                        dynamicGraph.updateValue("pitch", platformAngles[1]);
                        dynamicGraph.updateValue("rollAct", roll);
                        dynamicGraph.updateValue("pitchAct", pitch);
                        dynamicGraph.updateValue("setX", setPoint[0]);
                        dynamicGraph.updateValue("setY", setPoint[1]);
                        dynamicGraph.updateValue("setZ", setPoint[2]);
                        dynamicGraph.updateValue("actX", actualRobotPos[0]);
                        dynamicGraph.updateValue("actY", actualRobotPos[1]);
                        dynamicGraph.updateValue("actZ", actualRobotPos[2]);
                    }

                } catch (InterruptedException ex) {
                    System.out.println("Await error");
                }
            }
        }
    }

    /**
     * Change the Setpoint with the joystick.
     *
     * @return
     */
    public double[] changeSetPointPos() {
        double temp[] = new double[3];
        for (int i = 0; i < 3; i++) {
            temp[i] = setPoint[i] + (joystickPos[i] * scaleFactor);
        }
        // create a box to avoid the set point to go too far 
        if (temp[0] < x_MIN) {
            temp[0] = x_MIN;
        }
        if (temp[0] > x_MAX) {
            temp[0] = x_MAX;
        }
        if (temp[1] < y_MIN) {
            temp[1] = y_MIN;
        }
        if (temp[1] > y_MAX) {
            temp[1] = y_MAX;
        }
        if (temp[2] < z_MIN) {
            temp[2] = z_MIN;
        }
        if (temp[2] > z_MAX) {
            temp[2] = z_MAX;
        }
        return temp;
    }

    /**
     * Calculate the Error on the robot (set point - actual point).
     */
    public synchronized void calculateError() {
        double tempError[] = new double[3];
        SimpleMatrix origin = makeTranslationMatrix(new double[]{0, 0, zOffset}).mult(makeRotationMatrix("Y", new double[3], -pitch)).mult(makeRotationMatrix("X", new double[3], -roll));
        actualRobotPos = getPosition(origin, robotAngles);
        for (int i = 0; i < 3; i++) {
            tempError[i] = (setPoint[i] - actualRobotPos[i]);
        }
        setError(tempError);
    }

    /**
     * Set the Error value.
     *
     * @param error
     */
    public void setError(double[] error) {
        this.error = error.clone();
    }

    /**
     * Returns the Error of the robot (Set point - actual position) 
     *
     * @return
     */
    public double[] getError() {
        return error.clone();
    }

    /**
     * Show the graph of the platform and robotpos last 5000 points.
     * It also save the list of the values setpointX, setpointY, setpointZ,
     * actualX, actualY, actualZ to a .csv file
     */
    public synchronized void showGraph() {
        ArrayList<String> tempList = (ArrayList<String>) list.clone();
        Graph graph = new Graph(tempList);

        // Save arraylist list to a .csv file
        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileWriter("data" + count + ".csv"));
            for (String text : list) {
                out.println(text);
            }
        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());

        } finally {
            if (out != null) {
                out.close();
            }
        }
        count++;
    }
    
    /**
     * Show a live graph of the avtual position and set point position
     * in a seperate windos.
     * Can show platform angles (roll and pitch) if you change the string in 
     * getDynamicGraphFrame. "Position" = XYZ values, "Platform" = roll and pitch
     * @param typeOfPlot
     */
    public void showLiveGraph(String typeOfPlot){
        dynamicGraph = new DynamicGraph();
        JFrame plotFrame = dynamicGraph.getDynamicGraphFrame(typeOfPlot);
        plotFrame.pack();
        RefineryUtilities.centerFrameOnScreen(plotFrame);
        plotFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        plotFrame.setVisible(true);
    }

    /**
     * Sending a string with the roll,pitch,setPoint X, setPoint Y, setPoint Z,
     * actualRobotPos X, actualRobotPos Y, actualRobotPos Z.
     */
    public void sendInfoToClient() {
        String message = ("" + roll + "," + pitch + "," + setPoint[0] + "," + setPoint[1] + "," + setPoint[2] + "," + actualRobotPos[0] + "," + actualRobotPos[1] + "," + actualRobotPos[2]);
        //System.out.println("Message to client: " + message);
        sendData = message.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, port);
        try {
            clientSocket.send(sendPacket);
        } catch (IOException ex) {
            System.out.println("Error while sending packet!");
        }
    }

    /**
     * Returns the X,Y,Z position in world coordinates
     *
     * @param origin
     * @param values
     * @return
     */
    private double[] getPosition(SimpleMatrix origin, double[] values) {
        SimpleMatrix tm = getTransformationMatrix(origin, values);
        double x = tm.get(0, 3);
        double y = -tm.get(1, 3);
        double z = tm.get(2, 3);
        return new double[]{x, y, z};
    }

    /**
     * Returns the Transformation matrix.
     */
    private SimpleMatrix getTransformationMatrix(SimpleMatrix origin, double[] values) {

        int i = 0;
        List<SimpleMatrix> matrices = new ArrayList<>();
        matrices.add(makeRotationMatrix("Z", new double[]{0, 0, 0}, values[i++]));
        matrices.add(makeRotationMatrix("Y", new double[]{0, 0, 0.4}, values[i++]));
        matrices.add(makeRotationMatrix("Y", new double[]{0.455, 0, 0}, values[i++]));
        matrices.add(makeRotationMatrix("X", new double[]{0.42, 0, 0}, values[i++]));
        matrices.add(makeRotationMatrix("Y", new double[]{0, 0, 0}, values[i++]));
        matrices.add(makeRotationMatrix("X", new double[]{0.08, 0, 0}, values[i++]));
        matrices.add(makeTranslationMatrix(new double[]{0, 0, -0.1}));
        matrices.add(makeTranslationMatrix(new double[]{0.12, 0, 0}));

        SimpleMatrix result = origin == null ? SimpleMatrix.identity(4) : origin;
        for (SimpleMatrix m : matrices) {
            result = result.mult(m);
        }

        return result;
    }

    /**
     * Returns a Rotation matrix for eiter X, Y or Z.
     *
     * @param axis
     * @param relPos
     * @param val
     * @return
     */
    private SimpleMatrix makeRotationMatrix(String axis, double[] relPos, double val) {
        if (relPos.length != 3) {
            throw new IllegalArgumentException("relPos.length != 3");
        }
        double x = relPos[0];
        double y = relPos[1];
        double z = relPos[2];

        val = Math.toRadians(val);

        switch (axis) {
            case "X":
                return new SimpleMatrix(new double[][]{
                    {1, 0, 0, x},
                    {0, Math.cos(val), -Math.sin(val), y},
                    {0, Math.sin(val), Math.cos(val), z},
                    {0, 0, 0, 1}});
            case "Y":
                return new SimpleMatrix(new double[][]{
                    {Math.cos(val), 0, Math.sin(val), x},
                    {0, 1, 0, y},
                    {-Math.sin(val), 0, Math.cos(val), z},
                    {0, 0, 0, 1}});
            case "Z":
                return new SimpleMatrix(new double[][]{
                    {Math.cos(val), -Math.sin(val), 0, x},
                    {Math.sin(val), Math.cos(val), 0, y},
                    {0, 0, 1, z},
                    {0, 0, 0, 1}});
            default:
                throw new IllegalArgumentException("Axis didn't match X, Y or Z!");
        }
    }

    /**
     * Return a Translation Matrix with the X, Y or Z values.
     *
     * @param relPos
     * @return
     */
    private SimpleMatrix makeTranslationMatrix(double[] relPos) {
        if (relPos.length != 3) {
            throw new IllegalArgumentException("relPos.length != 3");
        }
        double x = relPos[0];
        double y = relPos[1];
        double z = relPos[2];

        return new SimpleMatrix(new double[][]{
            {1, 0, 0, x},
            {0, 1, 0, y},
            {0, 0, 1, z},
            {0, 0, 0, 1}});
    }

}
