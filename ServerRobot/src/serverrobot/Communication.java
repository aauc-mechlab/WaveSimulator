package serverrobot;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.hials.crosscom.CrossComClient;
import no.hials.crosscom.KRL.KRLBool;
import no.hials.crosscom.KRL.structs.KRLPos;

/**
 * Communication class handels all communication to the robot controller Using
 * JOpenShowVar
 *
 * @author Håkon
 */
public class Communication extends Thread {

    private double[] angles = new double[6];
    private double[] error = new double[3];
    private CrossComClient client;
    private KRLPos pos;
    private KRLBool flag;
    private final String ipAddress = "192.168.2.2";  // Ip address of the robot
    private final int port = 7000;            // port on robot
    private final MyEvent event;
    private Process process;
    private final double[] position = new double[3];
    long T0 = 0;
    long T1;
    long t0 = -1;
    private double kp = 90,
            ki = 0,
            kd = 0;

    private PID[] pids = new PID[3];

    /**
     * Constructor creates a new object.
     *
     * @param event
     */
    public Communication(MyEvent event) {
        this.event = event;

        for (int i = 0; i < pids.length; i++) {
            pids[i] = new PID();
        }
        startUp();
    }

    /**
     * startUp method creates a CrossComClient for communication with the robot,
     * a KRLPos to send the position to the Robot and a KRLbool used to avoid
     * the robot run the same position multiple times.
     */
    public void startUp() {
        try {
            client = new CrossComClient(ipAddress, port);
            pos = new KRLPos("MYPOS");
            flag = new KRLBool("MYBOOL");
            System.out.println("Communication thread started.. ");
        } catch (IOException ex) {
            System.out.println("Error creating crosscomclient");
            ex.printStackTrace();
        }
    }

    /**
     * Method to inialize a process.
     *
     * @param process
     */
    public void setProcess(Process process) {
        this.process = process;
    }

    /**
     * Run method to communicate with the robot.
     */
    @Override
    public void run() {
        double[] tempAngles = new double[6];
        while (true) {

            try {
                // read actual joint angles on robot
                tempAngles = client.readJointAngles();
                setAngles(tempAngles);
                event.setRobotAnglesFlag();

                // await a new pos to send to the robot
                event.awaitError(MyEventState.UP);
                sendRobotNewPos(process.getError());

            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (InterruptedException ex) {
                Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Sets the value of the robot angles
     *
     * @param temp
     */
    public void setAngles(double[] temp) {
        angles = temp;
    }

    /**
     * Returns the value of the robot angles
     *
     * @return
     */
    public double[] getAngels() {
        return angles.clone();
    }

    /**
     * Sends the New position to the Robot.
     *
     * @param error
     */
    public void sendRobotNewPos(double[] error) {
        if (t0 == -1) {
            t0 = System.currentTimeMillis();
        }
        this.error = error.clone();

        // calculate new outputs to the robot with PID's
        double[] gains = new double[3];
        double dT = (double) (System.currentTimeMillis() - t0) / 1000d;
        //System.out.println("Delta: " + dT);
        for (int i = 0; i < 3; i++) {
            gains[i] = pids[i].regulate(error[i], dT);
            if (gains[i] > 10) {
                gains[i] = 10;
            }
            if (gains[i] < -10) {
                gains[i] = -10;
            }
        }

        // Write new pos to robot
        pos.setX(gains[0]);
        pos.setY(gains[1]);
        pos.setZ(gains[2]);
        try {
            client.writeVariable(pos);
            flag.setValue(true);
            client.writeVariable(flag);
        } catch (IOException ex) {
            Logger.getLogger(Communication.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.print("Robot new pos relative to its current position:   ");
        System.out.println(Arrays.toString(gains));

        // reset flags so the receiver theads can set new values and the process 
        // calculate a new position to the robot
        event.resetErrorFlag();
        event.resetFlags();
        event.setState();

        T1 = System.currentTimeMillis();
//        System.out.println("Time between each new robot pos: " + (T1 - T0));
        T0 = T1;
        t0 = System.currentTimeMillis();
    }

    /**
     * Set the P in the PID's.
     *
     * @param Kp
     */
    public void setKp(double Kp) {
        this.kp = Kp;
        for (int i = 0; i < 3; i++) {
            pids[i].setKp(Kp);
        }
    }

    /**
     * Set the I in the PID's.
     *
     * @param Ki
     */
    public void setKi(double Ki) {
        this.ki = Ki;
        for (int i = 0; i < 3; i++) {
            pids[i].setTi(Ki);
        }
    }

    /**
     * Set the D in the PID's.
     *
     * @param Kd
     */
    public void setKd(double Kd) {
        this.kd = Kd;
        for (int i = 0; i < 3; i++) {
            pids[i].setTd(Kd);
        }
    }
}
