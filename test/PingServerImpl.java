package test;

import rmi.RMIException;

import java.io.Serializable;

public class PingServerImpl implements PingServerInterface, Serializable {
    @Override
    public String ping(int idNumber) throws RMIException {
        return "Pong" + idNumber;
    }
}
