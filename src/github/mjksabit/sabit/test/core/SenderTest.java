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

    static String FILE = "/media/sabit/Data/TV Series/Breathe.S01.720p.Web-Rip.x265";

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
    void testCancelSending() {
        File file = new File(FILE);
        try {
            sender.sendFile(file, new Progress());
        } catch (FileNotFoundException | SocketException e) {
            e.printStackTrace();
        }

        try{
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sender.cancelSending();

        while (sender.isActive()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @org.junit.jupiter.api.Test
    void sendFile() {
        File file = new File(FILE);
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