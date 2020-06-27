package github.mjksabit.sabit.cli;

import github.mjksabit.autoconnect.ServerSide;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Receiver implements Closeable {
    ServerSide serverSide;
    ServerSocket serverSocket;
    Socket connectionSocket;

    public Receiver(String name) throws IOException {
        serverSide = new ServerSide(name, Main.listeningPort, Main.clientPort, Main.FTPPort);

        receive();
    }

    private void receive() throws IOException {
        serverSide.startListening();
        serverSocket = new ServerSocket(Main.FTPPort);

        connectionSocket = serverSocket.accept();
        serverSide.stopListening();
        System.out.println("Sender Connected");
    }

    @Override
    public void close() throws IOException {
        connectionSocket.close();
        serverSocket.close();
    }
}
