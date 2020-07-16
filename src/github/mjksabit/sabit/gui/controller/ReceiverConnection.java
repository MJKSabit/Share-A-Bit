package github.mjksabit.sabit.gui.controller;


import github.mjksabit.sabit.core.Constant;
import github.mjksabit.sabit.core.Receiver;
import github.mjksabit.sabit.gui.JFXLoader;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReceiverConnection extends Controller {

    @FXML
    private Label usernameText;

    private Receiver receiver = null;
    private ExecutorService connectionThread = Executors.newSingleThreadExecutor();
    private String name;
    private String senderName;
    private String fileSaveDirectory;

    public void startReceiver(String name, String fileSaveDirectory) {
        this.name = name;
        usernameText.setText(name);

        this.fileSaveDirectory = fileSaveDirectory;
        try {
            receiver = new Receiver(name);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Try Again When the Port is Available");
            alert.setContentText("Either another instance of this app or Other App using Port no "+ Constant.LISTENING_PORT + "\n\n");
            alert.showAndWait();
            cancelReceiving(null);
            return;
        }

        connectionThread.execute(() -> {
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
        });
    }

    @FXML
    void cancelReceiving(ActionEvent event) {
        try {
            receiver.stopListening();
            receiver.close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }

        Start startPage = null;
        try {
            startPage = JFXLoader.loadFXML("start");
        } catch (IOException e) {
            e.printStackTrace();
        }
        startPage.setStage(getStage());
        startPage.show("Share A BIT");
    }
}
