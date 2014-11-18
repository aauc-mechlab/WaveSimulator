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
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

/**
 *
 * @author Humlen
 */
/**
 * This class is used for reading a joystic and it sends data to the server
 *
 * @author Håkon
 */
public final class JoystickReader extends Thread {

    private Controller currentController;
    private double xyz[] = new double[3];
    private DatagramSocket clientSocket;
    private byte[] sendData;
    private String message = "";
    private String ip; //"192.168.1.10";         // Odroid ip address
    private int port; //9876;
    private InetAddress ipAddress;
    
    /**
     * Check if there is a joystick connected in the usb port
     */

    public JoystickReader(String IP, String Port) {

        this.ip = IP;
        this.port = Integer.parseInt(Port);
        Startup();

        for (Controller c : ControllerEnvironment.getDefaultEnvironment().getControllers()) {
            Controller.Type type = c.getType();
            if (type == Controller.Type.STICK) {
                currentController = c;
                System.out.println(type + " found: " + c.getName());
            }
        }
    }

    /**
     * Starts up the UPD socket 
     */
    public void Startup() {
        try {
            clientSocket = new DatagramSocket();
            System.out.println("Client started, sending to: " + ip + " on port: " + port);
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
     * Reads the values from the joystick
     */
    @Override
    public void run() {
        try {
            Component[] components = currentController.getComponents();
            // X axis and Y axis
            int xAxisPercentage = 0;
            int yAxisPercentage = 0;
            int zAxisPercentage = 0;

            while (true) {
                currentController.poll();
                if (currentController.getType() == Controller.Type.STICK) {
                    for (int i = 0; i < components.length; i++) {
                        Component component = components[i];
                        Component.Identifier componentIdentifier = component.getIdentifier();

                        if (component.isAnalog()) {

                            float axisValue = component.getPollData();
                            int axisValueInPercentage = getAxisValueInPercentage(axisValue);

                            // X axis
                            if (componentIdentifier == Component.Identifier.Axis.X) {
                                xAxisPercentage = axisValueInPercentage;
                                continue; // Go to next component.
                            }
                            // Y axis
                            if (componentIdentifier == Component.Identifier.Axis.Y) {
                                yAxisPercentage = axisValueInPercentage;
                                continue; // Go to next component.
                            }
                            // Z rotation
                            if (component.getName().equals("Z-rotasjon")) {
                                zAxisPercentage = axisValueInPercentage;
                            }
                        }
                    }

                }
                sendDataToServer(yAxisPercentage,xAxisPercentage,zAxisPercentage);
            }
        } catch (Exception ex) {
            currentController = null;
        }
    }

    /**
     * Sends a string of data from the joystick to the server
     * @param xAxisPercentage
     * @param yAxisPercentage
     * @param zAxisPercentage 
     */
    public void sendDataToServer(int xAxisPercentage,int yAxisPercentage, int zAxisPercentage) {
        
        message = "" + (double)xAxisPercentage + "," + (double)yAxisPercentage + "," + (double)zAxisPercentage;
        sendData = message.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, port);
        
        try {
            clientSocket.send(sendPacket);
            Thread.sleep(30);

        } catch (IOException ex) {
            System.out.println("Error while sending packet!");
        }
        catch (InterruptedException ex) {
            Logger.getLogger(JoystickReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns the axis value of the joystick in perscentage
     *
     * @param axisValue
     * @return
     */
    public int getAxisValueInPercentage(float axisValue) {
        //System.out.println("axisvalue in percentage(before conversion): "+axisValue);
        return (int) (((2 - (1 - axisValue)) * 100) / 2);
    }

}
