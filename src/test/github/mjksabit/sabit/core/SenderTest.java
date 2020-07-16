package github.mjksabit.sabit.core;

import github.mjksabit.autoconnect.ClientSide;
import github.mjksabit.autoconnect.ServerDiscoveryObserver;
import github.mjksabit.sabit.cli.Progress;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Semaphore;

import static org.junit.jupiter.api.Assertions.*;

class SenderTest /*implements ServerDiscoveryObserver*/ {

//    static String FILE = "/media/sabit/Data/TV Series/Breathe.S01.720p.Web-Rip.x265/Breathe.S01E01.720p.WEB-DL.x265.RMTeam.(Satina.CO).mkv";
//
//    Sender sender;
//    Semaphore connected = new Semaphore(1);
//    Sender.ReceiverInfo info;
//    private Socket socket;
//
//    @org.junit.jupiter.api.BeforeEach
//    void setUp() {
//        try {
//            connected.acquire();
//            sender = new Sender("Test Sender", this);
//            sender.sendPresence();
//            connected.acquire();
//            makeConnection();
//        } catch (InterruptedException | IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @org.junit.jupiter.api.AfterEach
//    void tearDown() {
//        try {
//            sender.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    void makeConnection() throws IOException {
//        socket = new Socket(info.getAddress(), info.getPort());
//        String receiver = sender.makeConnection(socket, "Test Sender");
//        System.out.println(receiver);
//    }
//
//
//    @org.junit.jupiter.api.Test
//    void testCancelSending() {
//        File file = new File(FILE);
//        try {
//            sender.sendFile(file, new Progress());
//        } catch (FileNotFoundException | SocketException e) {
//            e.printStackTrace();
//        }
//
//        try{
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        sender.cancelSending();
//
//        while (sender.isActive()) {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    @Test
//    void cancelSendingCurrent() {
//        File file = new File(FILE);
//        try {
//            sender.sendFile(file, new Progress());
//        } catch (FileNotFoundException | SocketException e) {
//            e.printStackTrace();
//        }
//
//        try{
//            Thread.sleep(500);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        sender.cancelSendingCurrent();
//
//        while (sender.isActive()) {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//
//        sendFile();
//    }
//
//    @org.junit.jupiter.api.Test
//    void sendFile() {
//        File file = new File(FILE);
//        try {
//            sender.sendFile(file, new Progress());
//        } catch (FileNotFoundException | SocketException e) {
//            e.printStackTrace();
//        }
//
//        do {
//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        } while (sender.isActive());
//
//        File received = new File("." + File.separator + file.getName());
//        assertEquals(file.length(), received.length(), "File Sent Successfully");
//
//        received.delete();
//    }
//
//    @org.junit.jupiter.api.Disabled
//    @org.junit.jupiter.api.Test
//    void startReceiving() {
//    }
//
//
//    @Override
//    public void serverDiscovered(ClientSide.ServerInfo serverInfo) {
//        info = new Sender.ReceiverInfo(serverInfo);
//        connected.release();
//    }
}