/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientv2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to receive platform position and robot position
 *
 * @author Håkon
 */
public class Server extends Thread {

    private DatagramSocket serverSocket;
    private double[] rollPitch = new double[2];
    private double[] actualRobotPos = new double[3];
    private double[] setPoint = new double[3];

    private byte[] receiveData;
    private final int Port = 4321;
    private final GUI gui;

    /**
     * Starts up the server 
     * @param gui 
     */
    public Server(GUI gui) {
        Startup();
        this.gui = gui;
    }

    /**
     * Sets up the UDP socket
     */
    public void Startup() {
        try {
            serverSocket = new DatagramSocket(Port);
            try {
                System.out.println("Server started " + InetAddress.getLocalHost().getHostAddress() + " on port: " + Port);
            } catch (UnknownHostException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
            receiveData = new byte[1024];
        } catch (SocketException ex) {
            System.out.println("Error.. !");
        }
    }

    /**
     * Gets the values for the roll, pitch, actual pos and set pos of the robot
     * from the client on the server side to update the graph in the GUI
     */
    public void run() {
        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            try {
                //System.out.println("Waiting for packet....");
                serverSocket.receive(receivePacket);
                String message = new String(receivePacket.getData());
                // split incoming string pharse to double
                String[] split = message.split(",");
                //System.out.println(message);

                for (int a = 0; a < 2; a++) {
                    rollPitch[a] = Double.parseDouble(split[a]);
                }

                for (int b = 2; b < 5; b++) {
                    setPoint[b - 2] = Double.parseDouble(split[b]);
                }

                for (int c = 5; c < 8; c++) {
                    actualRobotPos[c - 5] = Double.parseDouble(split[c]);
                }

            } catch (IOException ex) {
                System.out.println("Error!");
            } catch (NumberFormatException e) {
                System.out.println("Error when parsing string to double: ");
                e.printStackTrace();
            }
            // Updates the graph in the GUI
            gui.updateRoll(rollPitch[0]);
            gui.updatePitch(rollPitch[1]);
            gui.updateSetX(setPoint[0]);
            gui.updateSetY(setPoint[1]);
            gui.updateSetZ(setPoint[2]);
            gui.updateActualX(actualRobotPos[0]);
            gui.updateActualY(actualRobotPos[1]);
            gui.updateActualZ(actualRobotPos[2]);
            //System.out.println(Arrays.toString(setPoint));
        }
    }
}
