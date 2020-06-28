package github.mjksabit.sabit.core;

import github.mjksabit.autoconnect.ClientSide;
import github.mjksabit.autoconnect.ServerDiscoveryObserver;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class Sender extends Connection {

    static public class ServerInfo {
        final InetAddress address;
        final String name;
        final int port;

        public ServerInfo(ClientSide.ServerInfo serverInfo) {
            this.address = serverInfo.getAddress();
            String[] responses = serverInfo.getResponseData().split(REGEX_SPLITTER);

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

    public Sender(String name, ServerDiscoveryObserver observer) throws IOException {
        super(name);

        clientSide = new ClientSide(observer, name, clientPort, listeningPort);
        clientSide.sendPresence();
    }

    @Override
    public String makeConnection(Socket connectionSocket, String connectionText) throws IOException {
        clientSide.stopListing();
        return super.makeConnection(connectionSocket, connectionText);
    }
}
