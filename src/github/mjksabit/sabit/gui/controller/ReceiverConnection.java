package github.mjksabit.sabit.gui.controller;


import github.mjksabit.sabit.core.Receiver;
import github.mjksabit.sabit.core.Sender;
import github.mjksabit.sabit.gui.JFXLoader;
import javafx.application.Platform;
import javafx.collections.FXCollections;

import java.io.IOException;

public class ReceiverConnection extends Controller {

    Receiver receiver = null;
    String name;
    String senderName;
    String fileSaveDirectory;

    public void startReceiver(String name, String fileSaveDirectory) {
        this.name = name;
        this.fileSaveDirectory = fileSaveDirectory;
        receiver = new Receiver(name);

        new Thread(() -> {
            try {
                senderName = receiver.waitForSender();

                Connected connected = JFXLoader.loadFXML("connected");
                connected.setFileSaveDirectory(fileSaveDirectory);

                Platform.runLater(() -> {
                    connected.setStage(getStage());
                    connected.show("Share A BIT");
                    connected.setNames(name, senderName);
                });

                connected.startConnection(receiver, FXCollections.observableArrayList());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
