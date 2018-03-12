package test;

import rmi.RMIException;
import rmi.Skeleton;

public class PingPongServer {
    public static void main(String[] args) {
        // remote object
        PingServerImpl server = PingServerFactory.makePingServer();
        Skeleton<PingServerInterface> skeleton = new Skeleton<>(PingServerInterface.class, server);
        try {
            skeleton.start();
        } catch (RMIException e) {
            System.err.println(e.getMessage());
        }
    }
}
