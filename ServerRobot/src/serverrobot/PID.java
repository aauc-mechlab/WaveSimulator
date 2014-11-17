package serverrobot;


import java.io.Serializable;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author laht
 */
public class PID implements Serializable{

    private final double windup_guard = 10;
    private double Kp, Ti, Td;
    private transient double integral, lastError;

    public PID() {
        Kp = 100;
        Ti = 0;//7.00;
        Td = 0.3;//1/Ti;//1.500;
    }

    public PID(double Kp, double Ti, double Td) {
        this.Kp = Kp;
        this.Ti = Ti;
        this.Td = Td;
    }

    public double regulate(double error, double dT) {
        if (dT == 0) {
            dT = Double.MIN_VALUE;
        }

        // integration with windup guarding
        integral += (error * dT);
        if (integral < -(windup_guard)) {
            integral = -(windup_guard);
        } else if (integral > windup_guard) {
            integral = windup_guard;
        }

        double derivate = (error - lastError) / dT;
        lastError = error;
        double result = (Kp * error) + (Ti * integral) + (Td * derivate);
        if (result > 10) {
            result = 10;
        } else if (result < -10) {
            result = -10;
        }
        return result;
    }

    public double[] getPIDParameters() {
        return new double[]{Kp, Ti, Td};
    }

    public void setPIDParameters(double... params) {
        if (params.length != 3) {
            throw new IllegalArgumentException("The number of parameters must be equal to 3! Was: " + params.length);
        }
        setKp(params[0]);
        setTi(params[1]);
        setTd(params[2]);
    }
    
    public void setParam(int i, double value) {
        switch(i) {
            case 1:
                setKp(value);
                break;
            case 2:
                 setTi(value);
                break;
            case 3:
                 setTd(value);
                break;
        }
    }

    public synchronized double getKp() {
        return Kp;
    }

    public synchronized void setKp(double Kp) {
        this.Kp = Kp;
    }

    public synchronized double getTi() {
        return Ti;
    }

    public synchronized void setTi(double Ti) {
        this.Ti = Ti;
    }

    public synchronized double getTd() {
        return Td;
    }

    public synchronized void setTd(double Td) {
        this.Td = Td;
    }

    @Override
    public String toString() {
        return "PID{" + "Kp=" + Kp + ", Ti=" + Ti + ", Td=" + Td + '}';
    }

}