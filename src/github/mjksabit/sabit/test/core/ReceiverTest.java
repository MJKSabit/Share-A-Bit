package github.mjksabit.sabit.test.core;

import github.mjksabit.sabit.cli.partial.Progress;
import github.mjksabit.sabit.core.Receiver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.SocketException;

import static org.junit.jupiter.api.Assertions.*;

class ReceiverTest {

    Receiver receiver;

    @BeforeEach
    void setUp() {
        receiver = new Receiver("Receiver Test");
        try {
            String sender = receiver.waitForSender();
            System.out.println(sender);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @AfterEach
    void tearDown() {
        try {
            receiver.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void startReceiving() {
        try {
            receiver.startReceiving(new Progress());
        } catch (SocketException e) {
            e.printStackTrace();
        }

        while (receiver.isActive()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}