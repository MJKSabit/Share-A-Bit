package github.mjksabit.sabit.gui.controller;


import github.mjksabit.sabit.gui.JFXLoader;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.io.IOException;

public class Start extends Controller{
    private StringProperty nameProperty = new SimpleStringProperty();

    @FXML
    TextField nameText;

    @FXML
    void initialize() {
        nameProperty.bind(nameText.textProperty());
    }

    @FXML
    void startReceive(ActionEvent event) {
        try {
            ReceiverConnection receiverConnection = JFXLoader.loadFXML("receiver-start");
            receiverConnection.setStage(getStage());
            receiverConnection.show("Receiver");
            receiverConnection.startReceiver(nameProperty.get());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void startSend(ActionEvent event) {
        try {
            SenderConnection sender = JFXLoader.loadFXML("sender-start");
            sender.startSender(nameProperty.get());
            sender.setStage(getStage());
            sender.show("Sender");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
