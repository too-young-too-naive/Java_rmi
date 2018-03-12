package test;

import rmi.RMIException;
import rmi.Stub;

import java.net.InetSocketAddress;

public class PingPongClient {

    public static void main(String[] args) {
        InetSocketAddress address = new InetSocketAddress("34.234.95.71", 40000);
        PingServerInterface server = Stub.create(PingServerInterface.class, address);
        try {
            for (int i = 0; i < 4; i++) {
                System.out.println(server.ping(i));
            }
        } catch (RMIException e) {
            System.err.println(e.getMessage());
        }
    }
}
