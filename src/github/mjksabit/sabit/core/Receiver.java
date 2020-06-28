package github.mjksabit.sabit.core;

import github.mjksabit.autoconnect.ServerSide;
import github.mjksabit.sabit.cli.Main;
import github.mjksabit.sabit.core.ftp.SimpleFTP;
import github.mjksabit.sabit.core.ftp.IFTP;
import github.mjksabit.sabit.core.partial.Progress;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Receiver implements Closeable {
    ServerSide serverSide;
    ServerSocket serverSocket;

    Socket connectionSocket;
    DataInputStream inputStream;
    DataOutputStream outputStream;
    IFTP protocol;

    public Receiver(String name) throws IOException {
        serverSide = new ServerSide(name, Main.listeningPort, Main.clientPort, name + Main.REGEX_SPLITTER + Main.FTPPort);

        connect();
        inputStream = new DataInputStream(connectionSocket.getInputStream());
        outputStream = new DataOutputStream(connectionSocket.getOutputStream());

        protocol = new SimpleFTP(inputStream, outputStream);

        String senderName = inputStream.readUTF();
        outputStream.writeUTF(name);

        System.out.println("Sender: " + senderName);
        System.out.println("====================================");

        protocol.startReceiving(".", new Progress());

        while (protocol.isReceiving()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

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
