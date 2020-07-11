package github.mjksabit.sabit.core;

import github.mjksabit.autoconnect.ServerSide;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Receiver extends Connection {
    ServerSide serverSide;
    ServerSocket serverSocket;

    public Receiver(String name) {
        super(name);
        serverSide = new ServerSide(name, listeningPort, clientPort, name + REGEX_SPLITTER + FTPPort);
    }

    public String waitForSender() throws IOException {
        serverSide.startListening();
        serverSocket = new ServerSocket(FTPPort);

        Socket socket = serverSocket.accept();
        serverSide.stopListening();

        return makeConnection(socket, name);
    }

    public void stopListening() throws IOException {
        serverSide.stopListening();
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (connectionSocket!=null) connectionSocket.close();
        if (serverSocket!=null) serverSocket.close();
    }
}
