package github.mjksabit.sabit.cli;

import github.mjksabit.autoconnect.ServerSide;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Receiver implements Closeable {
    ServerSide serverSide;
    ServerSocket serverSocket;

    Socket connectionSocket;
    DataInputStream inputStream;

    public Receiver(String name) throws IOException {
        serverSide = new ServerSide(name, Main.listeningPort, Main.clientPort, name + Main.REGEX_SPLITTER + Main.FTPPort);

        connect();
        inputStream = new DataInputStream(connectionSocket.getInputStream());
        String senderName = inputStream.readUTF();

        System.out.println("Sender: " + senderName);
        System.out.println("====================================");

        FileTransferProtocol.receive(".", inputStream);
    }

    private void connect() throws IOException {
        serverSide.startListening();
        serverSocket = new ServerSocket(Main.FTPPort);

        connectionSocket = serverSocket.accept();
        serverSide.stopListening();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
        connectionSocket.close();
        serverSocket.close();
    }
}
