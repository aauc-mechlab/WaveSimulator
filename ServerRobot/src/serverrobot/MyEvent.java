package serverrobot;

/**
 * MyEvent handels the communication between the threads.
 *
 * @author Håkon
 */
public class MyEvent {

    protected MyEventState robotAnglesFlag;
    protected MyEventState platformAnglesFlag;
    protected MyEventState joystickFlag;
    protected MyEventState errorFlag;
    protected MyEventState state;
    protected MyEventState zOffsetFlag;

    /**
     * Constructor creates an object and sets the flags down.
     */
    public MyEvent() {
        robotAnglesFlag = MyEventState.DOWN;
        platformAnglesFlag = MyEventState.DOWN;
        joystickFlag = MyEventState.DOWN;
        errorFlag = MyEventState.DOWN;
        state = MyEventState.DOWN;
        zOffsetFlag = MyEventState.DOWN;
    }

    /**
     * Returns the state of the state event.
     *
     * @return
     */
    public synchronized MyEventState getState() {
        return state;
    }

    /**
     * Set the state event.
     */
    public synchronized void setState() {
        state = MyEventState.UP;
        notifyAll();
    }

    /**
     * Reset the state event.
     */
    public synchronized void resetState() {
        state = MyEventState.DOWN;
        notifyAll();
    }

    /**
     * Waiting for the error flag to be set to MyEventState.UP
     *
     * @param ErrorFlag
     * @throws InterruptedException
     */
    public synchronized void awaitError(MyEventState ErrorFlag) throws InterruptedException {
        while (this.errorFlag != ErrorFlag) {
            wait();
        }
    }

    /**
     * Waiting for all value flags to get the MyEventState.UP
     *
     * @param RobotAngles
     * @param PlatformAngles
     * @param JoystickPos
     * @param Zoffset
     * @throws InterruptedException
     */
    public synchronized void awaitFlags(MyEventState RobotAngles, MyEventState PlatformAngles, MyEventState JoystickPos, MyEventState Zoffset) throws InterruptedException {
        while (((this.robotAnglesFlag != RobotAngles) || (this.platformAnglesFlag != PlatformAngles) || (this.joystickFlag != JoystickPos) || (this.zOffsetFlag != Zoffset))) {
            wait();
        }
    }

    /**
     * Resets the Value flags to MyEventState.DOWN
     */
    public synchronized void resetFlags() {
        robotAnglesFlag = MyEventState.DOWN;
        platformAnglesFlag = MyEventState.DOWN;
        joystickFlag = MyEventState.DOWN;
        zOffsetFlag = MyEventState.DOWN;
        notifyAll();
    }

    /**
     * Set the zOffsetFlag to MyEventState.UP
     */
    public synchronized void setZOffsetFlag() {
        zOffsetFlag = MyEventState.UP;
//        System.out.println("Offset flag");
        notifyAll();
    }

    /**
     * Returns the state of zOffsetFlag
     *
     * @return
     */
    public synchronized MyEventState getZOffsetFlag() {
        return zOffsetFlag;
    }

    /**
     * Set the robotAnglesFlag to MyEventState.UP
     */
    public synchronized void setRobotAnglesFlag() {
        robotAnglesFlag = MyEventState.UP;
//        System.out.println("Robot flag");
        notifyAll();
    }

    /**
     * Returns the state of robotAnglesFlag
     *
     * @return
     */
    public synchronized MyEventState getRobotAnglesFlag() {
        return robotAnglesFlag;
    }

    /**
     * Set the platformAnglesFlag to MyEventState.UP
     */
    public synchronized void setPlatformAnglesFlag() {
        platformAnglesFlag = MyEventState.UP;
//        System.out.println("Platform flag");
        notifyAll();
    }

    /**
     * Returns the state of platformAnglesFlag
     *
     * @return
     */
    public synchronized MyEventState getPlatformAnglesFlag() {
        return platformAnglesFlag;
    }

    /**
     * Set the joystickFlag to MyEventState.UP
     */
    public synchronized void setJoystickFlag() {
        joystickFlag = MyEventState.UP;
//        System.out.println("Joystick flag");
        notifyAll();
    }

    /**
     * returns the state of the joystickFlag.
     * 
     * @return 
     */
    public synchronized MyEventState getJoystickFlag() {
        return joystickFlag;
    }

    /**
     * Set the errorFlag to MyEventState.UP
     */
    public synchronized void setErrorFlag() {
        errorFlag = MyEventState.UP;
        notifyAll();
    }

    /**
     * Reset the errorFlag to MyEventstate.DOWN
     */
    public synchronized void resetErrorFlag() {
        errorFlag = MyEventState.DOWN;
        notifyAll();
    }
}
