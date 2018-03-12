package rmi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;

public class RemoteInvocationHandler implements InvocationHandler, Serializable {

    private static final int PRIME = 3;
    private InetSocketAddress inetSocketAddress;
    private Class<?> aClass;

    RemoteInvocationHandler(Class<?> c, InetSocketAddress addr) {
        this.inetSocketAddress = addr;
        this.aClass = c;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Object result;
        ObjectOutputStream outputStream = null;
        ObjectInputStream inputStream = null;
        Socket socket = new Socket();

        String methodName = method.getName();
        if (methodName.equals("toString")) {
            return String.format("Remote Interface: %s, Hostname of connected skeleton: %s, Port of connected skeleton: %s",
                    aClass.getName(),
                    inetSocketAddress.getHostName(),
                    inetSocketAddress.getPort());
        }
        if (methodName.equals("equals")) {
            return args[0] != null && PRIME * aClass.hashCode() + inetSocketAddress.hashCode() == args[0].hashCode();
        }
        if (methodName.equals("hashCode")) {
            return PRIME * aClass.hashCode() + inetSocketAddress.hashCode();
        }

        try {
            // output stream: 1. method name 2. parameters types 3. parameters
            socket = new Socket(inetSocketAddress.getHostName(), inetSocketAddress.getPort());
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            // serialize the methods to the server
            outputStream.writeObject(method.getName());
            outputStream.writeObject(method.getParameterTypes());
            outputStream.writeObject(args);
            // must flush the output stream before creating the input stream
            outputStream.flush();
            inputStream = new ObjectInputStream(socket.getInputStream());
            while (inputStream.available() < 0) {
                // input stream not ready
            }
            result = inputStream.readObject();
        } catch (Exception e) {
            throw new RMIException("Remote Method Invocation failed.");
        } finally {
            try {
                // if the socket is closed by the server, an exception may be raised, ignore the exception
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                socket.close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
        if (result instanceof InvocationTargetException) {
            throw ((InvocationTargetException) result).getTargetException();
        }
        return result;
    }
}