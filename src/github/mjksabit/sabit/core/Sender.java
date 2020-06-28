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

public class Sender extends Connection{

    static public class ServerInfo {
        final InetAddress address;
        final String name;
        final int port;

        public ServerInfo(ClientSide.ServerInfo serverInfo){
            this.address = serverInfo.getAddress();
            String[] responses = serverInfo.getResponseData().split(Main.REGEX_SPLITTER);

            name = responses[0];
            port = Integer.parseInt(responses[1]);
        }

        public InetAddress getAddress() {
            return address;
        }

        public String getName() {
            return name;
        }

        public int getPort() {
            return port;
        }
    }

    ClientSide clientSide;

    Socket connectionSocket;

    Scanner scanner = new Scanner(System.in);

    public Sender(String name, ServerDiscoveryObserver observer) throws IOException {
        super(name);

        clientSide = new ClientSide(observer, name, Main.clientPort, Main.listeningPort);

        clientSide.sendPresence();


    }

    @Override
    public String makeConnection(Socket connectionSocket, String connectionText) throws IOException {
        clientSide.stopListing();
        return super.makeConnection(connectionSocket, connectionText);
    }
}
