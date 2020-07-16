package github.mjksabit.sabit.core;

import github.mjksabit.autoconnect.ExceptionHandler;
import github.mjksabit.autoconnect.ServerSide;
import org.json.JSONException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Receiver extends Connection {
    ServerSide serverSide = null;
    ServerSocket serverSocket = null;

    public Receiver(String name) throws IOException, JSONException {
        super(name);
        init();
    }

    public Receiver(String name, ExceptionHandler exceptionHandler) throws IOException, JSONException {
        super(name, exceptionHandler);
        init();
    }

    private void init() throws IOException, JSONException {
        serverSocket = new ServerSocket(Constant.ANY_OPEN_PORT);
        serverSocket.setSoTimeout(Constant.CONNECTION_TIME_OUT);
        int ftpPort = serverSocket.getLocalPort();
        serverSide = new ServerSide(name, Constant.LISTENING_PORT, ftpPort, Constant.EXTRA_DATA);
    }

    public String waitForSender() throws IOException {
        serverSide.startListening();

        Socket socket = null;

        while (!isClosed && socket==null) {
            try {
                socket = serverSocket.accept();
            } catch (SocketTimeoutException ignored) {
            }
        }

        serverSide.stopListening();
        serverSocket.close();

        if (socket == null)
            throw new IOException("Closed Before Making Initial Connection!");

        return makeConnection(socket, name);
    }

    public void stopListening() {
        serverSide.stopListening();
    }

    private volatile boolean isClosed = false;
    @Override
    public void close() throws IOException {
        isClosed = false;
        super.close();
        if (connectionSocket!=null) connectionSocket.close();
    }
}
