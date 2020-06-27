package github.mjksabit.sabit.cli;

import github.mjksabit.autoconnect.ClientSide;
import github.mjksabit.autoconnect.newServerObserver;


import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Sender implements newServerObserver, Closeable {
    ArrayList<ClientSide.ServerData> listReceivers;
    ClientSide clientSide;

    Socket connectionSocket;
    DataOutputStream outputStream;

    Scanner scanner = new Scanner(System.in);

    public Sender(String name) throws IOException {
        listReceivers = new ArrayList<>();
        clientSide = new ClientSide(this, name, Main.clientPort, Main.listeningPort);

        clientSide.sendPresence();

        ClientSide.ServerData data = listReceivers.get(selectOption());
        connect(data);

        System.out.println("Receiver: " + data.getName());
        System.out.println("==================================");
        outputStream.writeUTF(name);

        System.out.print("Enter File Path: ");
        File file = new File(scanner.nextLine());

        FileTransferProtocol.send(file, outputStream);
    }



    private void connect(ClientSide.ServerData data) throws IOException {
        connectionSocket = new Socket(data.getAddress(), data.getPort());
        outputStream = new DataOutputStream(connectionSocket.getOutputStream());

        clientSide.stopListing();

    }

    @Override
    public void addServer(ClientSide.ServerData serverData) {
        System.out.println(listReceivers.size() + " - " + serverData.getName());
        listReceivers.add(serverData);
    }

    private int selectOption() {
        int choice = scanner.nextInt();
        scanner.nextLine();

        if(0>choice || choice>=listReceivers.size()) {
            System.err.println("Invalid Choice, Choose Between [0, "+listReceivers.size()+")");
            return selectOption();
        }
        return choice;
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
        connectionSocket.close();
    }
}
