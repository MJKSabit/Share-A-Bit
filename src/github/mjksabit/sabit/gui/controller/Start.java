package github.mjksabit.sabit.gui.controller;


import github.mjksabit.sabit.gui.JFXLoader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import java.io.IOException;

public class Start extends Controller{

    @FXML
    void startReceive(ActionEvent event) {
        try {
            ReceiverConnection receiverConnection = JFXLoader.loadFXML("receiver-start");
            receiverConnection.setStage(getStage());
            receiverConnection.show("Receiver");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void startSend(ActionEvent event) {
        try {
            SenderConnection sender = JFXLoader.loadFXML("sender-start");
            sender.setStage(getStage());
            sender.show("Sender");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
