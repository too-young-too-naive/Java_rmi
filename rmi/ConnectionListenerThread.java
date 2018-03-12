package rmi;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

/**
 * Listen for connection requests
 */
public class ConnectionListenerThread extends Thread {
    private ServerSocket serverSocket;
    // volatile wil force the thread not to use the cached value
    private volatile boolean isStopped = false;
    private Skeleton<?> skeleton;
    private LinkedList<RequestHandlerThread> threads;
    private static final int THRESHOLD = 20;

    ConnectionListenerThread(ServerSocket serverSocket, Skeleton<?> skeleton) {
        this.serverSocket = serverSocket;
        this.skeleton = skeleton;
        threads = new LinkedList<>();
    }

    @Override
    public void run() {
        Socket socket;
        while (!isStopped) {
            try {
                socket = serverSocket.accept();
                RequestHandlerThread requestHandlerThread = new RequestHandlerThread(socket, skeleton);
                threads.add(requestHandlerThread);
                requestHandlerThread.start();
                if (threads.size() > THRESHOLD) {
                    cleanUpThreads();
                }
            } catch (IOException e) {
                if (isStopped) {
                    return;
                }
                System.err.println(e.getMessage());
            }
        }
    }

    void terminate() {
        // stop request received
        isStopped = true;
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        for (RequestHandlerThread t : threads) {
            t.terminate();
        }
        for (RequestHandlerThread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * Periodically clean up the thread list
     */
    private void cleanUpThreads() {
        for (int i = 0; i < threads.size(); i++) {
            if (!threads.get(i).isAlive()) {
                threads.remove(i);
                i--;
            }
        }
    }
}
