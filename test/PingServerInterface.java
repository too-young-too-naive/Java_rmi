package test;

import rmi.RMIException;

public interface PingServerInterface {
    String ping(int idNumber) throws RMIException;
}
