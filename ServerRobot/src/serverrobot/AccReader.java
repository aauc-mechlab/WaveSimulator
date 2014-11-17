package serverrobot;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Enumeration;

/**
 * Classs to read the data from the Arduino over serial communication.
 * 
 * @author Hakon eikrem
 */
public class AccReader extends Thread implements SerialPortEventListener {

    private BufferedReader input; //BufferReader
    private static final int TIME_OUT = 2000; //waiting for port to open
    private static final int DATA_RATE = 115200;//9600; //Default data rate
    private double platformPos[] = new double[3];
    private final MyEvent event;
    long t0 = 0;
    long t1;
    private SerialPort serialPort;

    
    /**
     * Constructor to make a new object.
     * 
     * @param event 
     */
    public AccReader(MyEvent event) {
        this.event = event;
        initialize();
    }

    private static final String PORT_NAMES[] = {
        "COM7",
        "/dev/ttyUSB0", // Linux
        "COM3"
    };

    /**
     * This method initializing the serial port. 
     */
    private void initialize() {
        CommPortIdentifier portId = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
        //First, Find an instance of serial port as set in PORT_NAMES.
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            for (String portName : PORT_NAMES) {
                if (currPortId.getName().equals(portName)) {
                    portId = currPortId;
                    break;
                }
            }
        }
        if (portId == null) {
            System.out.println("Could not find COM port.");
            return;
        }
        try {
            // open serial port, and use class name for the appName.
            serialPort = (SerialPort) portId.open(this.getClass().getName(),
                    TIME_OUT);
            // set port parameters
            serialPort.setSerialPortParams(DATA_RATE,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            // open the streams
            input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));

            // add event listeners
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    /**
     * This should be called when you stop using the port. This will prevent
     * port locking on platforms like Linux.
     */
    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }

    /**
     * Handle an event on the serial port. Read the data and print it.
     * 
     * @param oEvent
     */
    public synchronized void serialEvent(SerialPortEvent oEvent) {

        String part1;
        String part2;
        int counter = 0;
        double roll;
        double pitch;
        double offsetPitch = 0;
        double offsetRoll = 0;
        double tempPos[] = new double[3];

        try {
            String inputLine = input.readLine();
            String[] parts = inputLine.split(",");
            part1 = parts[1];
            part2 = parts[0];

            roll = Float.parseFloat(part1);
            pitch = Float.parseFloat(part2);

            if (counter < 15) {
                offsetRoll = roll;
                offsetPitch = pitch;
                counter++;
            }
            roll = roll - offsetRoll;
            pitch = pitch - offsetPitch;

            roll = (int) (roll * 10);
            roll = (double) (roll / 10);
            pitch = (int) (pitch * 10);
            pitch = (double) (pitch / 10);

            tempPos[0] = roll;//roll;
            tempPos[1] = -pitch;//pitch;
            tempPos[2] = 0;

            if (event.getPlatformAnglesFlag() == MyEventState.DOWN) {
                setPlatformPos(tempPos);
                event.setPlatformAnglesFlag();
            }
            t1 = System.currentTimeMillis();
            //System.out.println("Acc angles time between new variables: " + (t1 - t0));
            t0 = t1;

        } catch (Exception e) {
            System.out.println("Error in accreader");
            e.printStackTrace();
        }
    }

    /**
     * Method to set the platformPos.
     * 
     * @param temp 
     */
    public void setPlatformPos(double[] temp) {
        platformPos = temp;
    }

    /**
     * Returning roll and Pitch from the platform
     * 
     * @return  
     */
    public double[] getPlatformPos() {
        return platformPos.clone();
    }
}
