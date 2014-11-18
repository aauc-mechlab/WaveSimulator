package serverrobot;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to receive joystick position from client
 *
 * @author Håkon
 */
public class ServerJoystick extends Thread {

    private final MyEvent event;
    private DatagramSocket serverSocket;
    private double[] joystickPos;
    private byte[] receiveData;
    private String message;
    private final int port = 1234;
    private InetAddress ipAddress;
    private DatagramPacket receivePacket;
    private long t0 = 0;
    private long t1;

    /**
     * Constructor to create object.
     *
     * @param event
     */
    public ServerJoystick(MyEvent event) {
        this.event = event;
        startup();
    }

    /**
     * Creates a UDP connection.
     */
    private void startup() {
        try {
            serverSocket = new DatagramSocket(port);
            receiveData = new byte[1024];
            receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                System.out.println("Server for reading joystick pos started " + InetAddress.getLocalHost().getHostAddress() + " on port: " + port);
            } catch (UnknownHostException ex) {
                Logger.getLogger(ServerJoystick.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SocketException ex) {
            System.out.println("Error.. !");
        }
    }

    /**
     * Run method reads and sets the Joystick pos.
     */
    @Override
    public void run() {
        double tempJoystickPos[] = new double[3];
        String strings[];
        while (true) {
            try {
                serverSocket.receive(receivePacket);
                message = new String(receivePacket.getData());
                strings = message.split(",");

                try {
                    for (int i = 0; i < strings.length; i++) {
                        tempJoystickPos[i] = Double.parseDouble(strings[i]);
                    }
                } catch (NumberFormatException ex) {
                    System.out.println("Joystick server Double.parseDouble error");
                }

                if (event.getJoystickFlag() == MyEventState.DOWN) {
                    setJoystickPos(checkLimitsAndCorrectValue(tempJoystickPos));
                    event.setJoystickFlag();
                }
            } catch (IOException ex) {
                System.out.println("Error on receiving packet!!");
            }

            t1 = System.currentTimeMillis();
//            System.out.println("Joystick time between new variables: " + (t1 - t0));
            t0 = t1;
        }
    }

    /**
     * Checks the value of the joystick and corrects it.
     *
     * @param temp
     * @return
     */
    public double[] checkLimitsAndCorrectValue(double[] temp) {
        double tempList[] = new double[3];
        double tempList2[] = new double[3];
        tempList = temp;
        for (int i = 0; i < 3; i++) {
            if (tempList[i] < 0) {
                tempList[i] = 0;
            }
            if (tempList[i] > 100) {
                tempList[i] = 100;
            }
            tempList2[i] = ((tempList[i] - 50) / 50);
        }
        return tempList2;
    }

    /**
     * Set the JoyStickPos
     *
     * @param temp
     */
    public void setJoystickPos(double[] temp) {
        joystickPos = temp;
    }

    /**
     * Returns the joystick value.
     *
     * @return
     */
    public double[] getJoystickPos() {
        return joystickPos.clone();
    }
}
