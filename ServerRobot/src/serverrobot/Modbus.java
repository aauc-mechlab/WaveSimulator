package serverrobot;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.wimpi.modbus.ModbusException;
import net.wimpi.modbus.ModbusSlaveException;
import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.ReadMultipleRegistersRequest;
import net.wimpi.modbus.msg.ReadMultipleRegistersResponse;
import net.wimpi.modbus.net.TCPMasterConnection;

/**
 * Modbus is used for collecting data from the PLC of the platform and in to the
 * program.
 *
 * @author Håkon
 */
public class Modbus extends Thread {

    private final MyEvent event;
    private TCPMasterConnection con;
    private ModbusTCPTransaction trans;
    private ReadMultipleRegistersRequest req;
    private ReadMultipleRegistersResponse res;
    private InetAddress ipAddress;
    private final String ip = "192.168.2.3"; // ip address of the plc running the platform
    private final int port = 502;  // port for connecting plc
    private final int ref = 12288; // address for reading and writing variables to the plc over modbus
    private double zOffset = 0;
    private double actualRoll = 0;
    private double actualPitch = 0;
    private long t0 = 0;
    private long t1;

    /**
     * Constructor to make a new object.
     *
     * @param event
     */
    public Modbus(MyEvent event) {
        this.event = event;
        startup();
    }

    /**
     * Starts up the modbus connection on the IPaddress to the PLC and on port
     * 502 (standard Modbus port).
     */
    public final void startup() {
        try {
            this.ipAddress = InetAddress.getByName(ip);
            this.con = new TCPMasterConnection(ipAddress);
            con.setPort(port);
            con.connect();
        } catch (UnknownHostException ex) {
            System.out.println("Error when creating InetAddress");
        } catch (Exception ex) {
            System.out.println("Error when connecting to plc");
        }
    }

    /**
     * Reads the actual Roll, pitch and Z-offset.
     */
    @Override
    public void run() {
        double tempOffset;
        double tempRoll;
        double tempPitch;
        int intOffset;
        int intRoll;
        int intPitch;
        int count = 20;
        while (true) {
            try {
                req = new ReadMultipleRegistersRequest(ref, count);
                trans = new ModbusTCPTransaction(con);
                trans.setRequest(req);
                trans.execute();

                res = (ReadMultipleRegistersResponse) trans.getResponse();
                intOffset = res.getRegisterValue(1) - (10000);
                intRoll = (res.getRegisterValue(10));
                intPitch = (res.getRegisterValue(15));
                tempOffset = (double) intOffset;
                tempRoll = (double) ((intRoll - 10000)) / 100;
                tempPitch = (double) ((intPitch - 10000)) / 100;

                if (event.getZOffsetFlag() == MyEventState.DOWN) {
                    setZOffset(tempOffset);
                    setActualRoll(tempRoll);
                    setActualPitch(tempPitch);
                    event.setZOffsetFlag();
                }

            } catch (ModbusSlaveException ex) {
                System.out.println("ModbusSlaveException");
            } catch (ModbusException ex) {
                System.out.println("ModbusExceptipon: ");
            }
            t1 = System.currentTimeMillis();
//            System.out.println("Modbus time between new variables: " + (t1 - t0));
            t0 = t1;
            try {
                Thread.sleep(10); // to avoid reading to fast.. 
            } catch (InterruptedException ex) {
                Logger.getLogger(Modbus.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Set the Zoffset value
     *
     * @param tempOffset
     */
    public void setZOffset(double tempOffset) {
        this.zOffset = tempOffset;
    }

    /**
     * returns the Zoffset value
     *
     * @return
     */
    public double getZOffset() {
        return zOffset;
    }

    /**
     * Set the actualRoll
     *
     * @param actualRoll
     */
    public void setActualRoll(double actualRoll) {
        this.actualRoll = actualRoll;
    }

    /**
     * Returns the actualRoll
     *
     * @return
     */
    public double getActualRoll() {
        return actualRoll;
    }

    /**
     * Set the actualPitch
     *
     * @param actualPitch
     */
    public void setActualPitch(double actualPitch) {
        this.actualPitch = actualPitch;
    }

    /**
     * Returns the actualPitch
     *
     * @return
     */
    public double getActualPitch() {
        return actualPitch;
    }
}
