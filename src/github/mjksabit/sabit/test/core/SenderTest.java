package github.mjksabit.sabit.test.core;

import github.mjksabit.autoconnect.ClientSide;
import github.mjksabit.autoconnect.ServerDiscoveryObserver;
import github.mjksabit.sabit.cli.partial.Progress;
import github.mjksabit.sabit.core.Sender;
import org.junit.jupiter.api.Disabled;

import javax.print.attribute.standard.PresentationDirection;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Semaphore;

import static org.junit.jupiter.api.Assertions.*;

class SenderTest implements ServerDiscoveryObserver {

    Sender sender;
    Semaphore connected = new Semaphore(1);
    Sender.ServerInfo info;
    private Socket socket;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        try {
            connected.acquire();
            sender = new Sender("Test Sender", this);
            connected.acquire();
            makeConnection();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        try {
            sender.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void makeConnection() throws IOException {
        socket = new Socket(info.getAddress(), info.getPort());
        String receiver = sender.makeConnection(socket, "Test Sender");
        System.out.println(receiver);
    }

    @Disabled
    @org.junit.jupiter.api.Test
    void stopSending() {
    }

    @Disabled
    @org.junit.jupiter.api.Test
    void testStopSending() {
    }

    @org.junit.jupiter.api.Test
    void sendFile() {
        File file = new File("/media/sabit/Data/@CODE/Java/Share-A-Bit/improved-cli/src/github/mjksabit/sabit/core/ftp/ExtendedFTP.java");
        try {
            sender.sendFile(file, new Progress());
        } catch (FileNotFoundException | SocketException e) {
            e.printStackTrace();
        }

        while (sender.isActive()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        File received = new File("." + File.separator + file.getName());
        assertEquals(file.length(), received.length(), "File Sent Successfully");

        received.delete();
    }

    @org.junit.jupiter.api.Disabled
    @org.junit.jupiter.api.Test
    void startReceiving() {
    }


    @Override
    public void serverDiscovered(ClientSide.ServerInfo serverInfo) {
        info = new Sender.ServerInfo(serverInfo);
        connected.release();
    }
}