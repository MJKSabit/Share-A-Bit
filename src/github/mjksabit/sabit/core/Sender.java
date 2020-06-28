package github.mjksabit.sabit.core;

import github.mjksabit.autoconnect.ClientSide;
import github.mjksabit.autoconnect.ServerDiscoveryObserver;
import github.mjksabit.sabit.cli.Main;
import github.mjksabit.sabit.core.ftp.IFTP;
import github.mjksabit.sabit.core.partial.Progress;


import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Sender extends Connection implements ServerDiscoveryObserver {

    private class ServerInfo {
        final InetAddress address;
        final String name;
        final int port;

        public ServerInfo(ClientSide.ServerInfo serverInfo) throws Exception {
            this.address = serverInfo.getAddress();
            String[] responses = serverInfo.getResponseData().split(Main.REGEX_SPLITTER);

            name = responses[0];
            port = Integer.parseInt(responses[1]);
        }
    }

    @Override
    public void serverDiscovered(ClientSide.ServerInfo serverInfo) {
        try {
            ServerInfo info = new ServerInfo(serverInfo);
            System.out.println(listReceivers.size() + "\t: " + info.name);
            listReceivers.add(info);
        } catch (Exception e) {
            System.err.println("Response Type Mismatch!");
        }

    }

    ArrayList<ServerInfo> listReceivers;
    ClientSide clientSide;

    Socket connectionSocket;

    Scanner scanner = new Scanner(System.in);

    public Sender(String name) throws IOException {
        super(name);
        listReceivers = new ArrayList<>();
        clientSide = new ClientSide(this, name, Main.clientPort, Main.listeningPort);

        clientSide.sendPresence();

        ServerInfo data = listReceivers.get(selectOption());
        clientSide.stopListing();

        connectionSocket = new Socket(data.address, data.port);
        String receiver = makeConnection(connectionSocket, name);

        System.out.println("Receiver: " + receiver);
        System.out.println("==================================");
//        protocol = new ExtendedFTP(inputStream, outputStream);

        System.out.print("Enter File Path(s): ");

        String filename;
        while ((filename = scanner.nextLine()) != null) {
            File file = new File(filename);

            try {
                sendFile(file, new Progress());
            } catch (FileNotFoundException e) {
                break;
            }
        }

        while (ftp.isSending()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
        super.close();
        connectionSocket.close();
    }
}
