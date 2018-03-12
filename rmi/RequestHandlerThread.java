package rmi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

/**
 * Handle each request from Stub
 */
public class RequestHandlerThread extends Thread {

    private Socket socket;
    private Skeleton<?> skeleton;
    private volatile boolean isReading;
    private volatile boolean isStopped;

    RequestHandlerThread(Socket socket, Skeleton<?> skeleton) {
        this.socket = socket;
        isReading = false;
        this.skeleton = skeleton;
    }

    @Override
    public void run() {
        Object result;
        ObjectOutputStream outputStream = null;
        ObjectInputStream inputStream = null;
        String methodName = null;
        Object[] paramList = null;
        Class<?>[] paramType = null;
        try {
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            while (inputStream.available() < 0) {
                // input stream not ready
            }
            isReading = true; // avoid terminate the thread while reading
            methodName = (String) inputStream.readObject();
            paramType = (Class<?>[]) inputStream.readObject();
            paramList = (Object[]) inputStream.readObject();

        } catch (Exception e) {
            skeleton.service_error(new RMIException("I/O related error in service thread."));
            if (isStopped) {
                return;
            } else {
                try {
                    socket.close();
                } catch (IOException e1) {
                    System.err.println(e1.getMessage());
                }
            }
        }

        try {
            Method method = skeleton.getServer().getClass().getMethod(methodName, paramType);
            method.setAccessible(true);
            result = method.invoke(skeleton.getServer(), paramList);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            result = e;
            System.err.println(e.getMessage());
        }

        try {
            if (outputStream != null) {
                outputStream.writeObject(result);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            isReading = false;
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                socket.close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    void terminate() {
        isStopped = true;
        if (!isReading) {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
