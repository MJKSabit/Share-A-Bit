package github.mjksabit.sabit.cli;

import github.mjksabit.autoconnect.ClientSide;
import github.mjksabit.autoconnect.newServerObserver;


import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Sender implements newServerObserver, Closeable {
    ArrayList<ClientSide.ServerData> listReceivers;
    ClientSide clientSide;

    Socket connectionSocket;

    Scanner scanner = new Scanner(System.in);

    public Sender(String name) throws IOException {
        listReceivers = new ArrayList<>();
        clientSide = new ClientSide(this, name, Main.clientPort, Main.listeningPort);

        clientSide.sendPresence();
        connect(listReceivers.get(selectOption()));

    }

    private void connect(ClientSide.ServerData data) throws IOException {
        connectionSocket = new Socket(data.getAddress(), data.getPort());

        if (connectionSocket.isConnected()) {
            clientSide.stopListing();
            System.out.println("Sending to " + data.getName());
        }

    }

    @Override
    public void addServer(ClientSide.ServerData serverData) {
        System.out.println(listReceivers.size() + " - " + serverData.getName());
        listReceivers.add(serverData);
    }

    private int selectOption() {
        int choice = scanner.nextInt();

        if(0>choice || choice>=listReceivers.size()) {
            System.err.println("Invalid Choice, Choose Between [0, "+listReceivers.size()+")");
            return selectOption();
        }
        return choice;
    }

    @Override
    public void close() throws IOException {
        connectionSocket.close();
    }
}
