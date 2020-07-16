package github.mjksabit.sabit.core;

import github.mjksabit.autoconnect.ClientSide;
import github.mjksabit.autoconnect.ExceptionHandler;
import github.mjksabit.autoconnect.ServerDiscoveryObserver;
import github.mjksabit.autoconnect.SharedInfo;
import org.json.JSONException;

import java.io.*;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

public class Sender extends Connection {

    static public class ReceiverInfo {
        final InetAddress address;
        final String name;
        final int port;

        public ReceiverInfo(SharedInfo info) {
            this.address = info.getSenderAddress();

            name = info.getSenderName();
            port = info.getSenderPort();
        }

        public InetAddress getAddress() {
            return address;
        }

        public String getName() {
            return name;
        }

        public int getPort() {
            return port;
        }
    }

    ClientSide clientSide;

    public Sender(String name, ServerDiscoveryObserver observer) throws SocketException, JSONException {
        super(name);
        init(observer);
    }

    public Sender(String name, ServerDiscoveryObserver observer, ExceptionHandler exceptionHandler) throws SocketException, JSONException {
        super(name, exceptionHandler);
        init(observer);
    }

    private void init(ServerDiscoveryObserver observer) throws SocketException, JSONException {
        clientSide = new ClientSide(name, Constant.LISTENING_PORT, Constant.EXTRA_DATA, observer);
    }

    public void sendPresence() throws ConnectException {
        clientSide.sendPresence();
    }

    public void pauseListening() {
        clientSide.pauseListening();
    }

    public void stopListening() {
        clientSide.stopListening();
    }

    @Override
    public String makeConnection(Socket connectionSocket, String firstConnectionText) throws IOException {
        clientSide.stopListening();
        return super.makeConnection(connectionSocket, firstConnectionText);
    }
}
