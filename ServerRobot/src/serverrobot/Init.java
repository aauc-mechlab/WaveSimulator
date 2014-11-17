package serverrobot;

/**
 * The init class is used to initalize the objects and start the threads.
 * 
 * @author Håkon
 */
public class Init {
    
    public static void main(String[] args) {
        
        // Initializing the objects
        MyEvent event = new MyEvent();
        ServerJoystick server = new ServerJoystick(event);
        AccReader accReader = new AccReader(event);
        Communication communication = new Communication(event);
        Modbus serverZOffset = new Modbus(event);
        Process process = new Process(accReader, server, event, communication, serverZOffset);
        communication.setProcess(process);
        GUI gui = new GUI(process,communication);
        
        
        
        // start the threads..
        communication.start();
        accReader.start();
        server.start();
        serverZOffset.start();
        process.start();
    }
}
