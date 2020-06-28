package github.mjksabit.sabit.cli;

import github.mjksabit.autoconnect.ClientSide;
import github.mjksabit.autoconnect.ServerDiscoveryObserver;
import github.mjksabit.sabit.cli.ftp.SimpleFTP;
import github.mjksabit.sabit.cli.ftp.IFTP;
import github.mjksabit.sabit.cli.partial.Progress;


import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Sender implements ServerDiscoveryObserver, Closeable {

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

    ArrayList<ServerInfo> listReceivers;
    ClientSide clientSide;

    Socket connectionSocket;
    DataOutputStream outputStream;
    DataInputStream inputStream;

    IFTP protocol;

    Scanner scanner = new Scanner(System.in);

    public Sender(String name) throws IOException {
        listReceivers = new ArrayList<>();
        clientSide = new ClientSide(this, name, Main.clientPort, Main.listeningPort);

        clientSide.sendPresence();

        ServerInfo data = listReceivers.get(selectOption());
        connect(data);

        System.out.println("Receiver: " + data.name);
        System.out.println("==================================");
        outputStream.writeUTF(name);

        protocol = new SimpleFTP(inputStream, outputStream);

        System.out.print("Enter File Path(s): ");

        String filename;
        while ((filename = scanner.nextLine()) != null) {
            File file = new File(filename);

            send(file, ".");
        }

        while (protocol.isSending()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        outputStream.writeUTF(Main.FINISHED_COMMAND);
    }

    private void send(File file, String parentPath) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();

            for (File iFile : files) {
                String childPath = parentPath+File.separator+file.getName();
                send(iFile, childPath);
            }
        }
        else protocol.addToSend(parentPath, file, new Progress());

    }

    private void connect(ServerInfo info) throws IOException {
        connectionSocket = new Socket(info.address, info.port);

        outputStream = new DataOutputStream(connectionSocket.getOutputStream());
        inputStream = new DataInputStream(connectionSocket.getInputStream());

        clientSide.stopListing();
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
        inputStream.close();
        connectionSocket.close();
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
}
