package github.mjksabit.sabit.core;

import github.mjksabit.autoconnect.ServerSide;
import github.mjksabit.sabit.cli.Main;
import github.mjksabit.sabit.core.ftp.SimpleFTP;
import github.mjksabit.sabit.core.partial.Progress;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Receiver extends Connection {
    ServerSide serverSide;
    ServerSocket serverSocket;

    Socket connectionSocket;

    public Receiver(String name) throws IOException {
        super(name);
        serverSide = new ServerSide(name, Main.listeningPort, Main.clientPort, name + Main.REGEX_SPLITTER + Main.FTPPort);

        serverSide.startListening();
        serverSocket = new ServerSocket(Main.FTPPort);

        connectionSocket = serverSocket.accept();
        serverSide.stopListening();

        String senderName = makeConnection(connectionSocket, name);

        ftp = new SimpleFTP(inputStream, outputStream);


        System.out.println("Sender: " + senderName);
        System.out.println("====================================");

        startReceiving(new Progress());

        while (ftp.isReceiving()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void close() throws IOException {
        super.close();
        connectionSocket.close();
        serverSocket.close();
    }
}
