package github.mjksabit.sabit.gui.controller;


import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import github.mjksabit.sabit.gui.JFXLoader;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;

public class Start extends Controller{
    private final StringProperty nameProperty = new SimpleStringProperty();
    private final StringProperty receiveDirectoryProperty = new SimpleStringProperty();

    @FXML
    private Pane topPane;

    @FXML
    private StackPane contentStack;

    @FXML
    private Pane homePane;

    @FXML
    private JFXButton sendButton;

    @FXML
    private JFXButton receiveButton;

    @FXML
    private Pane settingsPane;

    @FXML
    private JFXTextField nameText;

    @FXML
    private JFXTextField receiveDirectory;

    @FXML
    private JFXButton browseDirectory;

    @FXML
    private Pane aboutPane;

    @FXML
    private JFXButton updateChecker;

    @FXML
    private Pane bottomNavigation;

    @FXML
    private JFXButton homeButton;

    @FXML
    private JFXButton settingsButton;

    @FXML
    private JFXButton aboutButton;


    @FXML
    void initialize() {
        nameProperty.bind(nameText.textProperty());
        receiveDirectoryProperty.set(".");
        receiveDirectory.textProperty().bind(receiveDirectoryProperty);
        showHome(null);
    }

    @FXML
    void browseReceiveDirectory(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Receive Directory ...");
        File selectedFolder = chooser.showDialog(getStage());
        if (selectedFolder!=null)
            receiveDirectoryProperty.set(selectedFolder.getAbsolutePath());
    }

    void hideAllPaneExcept(Pane show) {
        homePane.setVisible(false);
        homePane.setDisable(true);

        settingsPane.setVisible(false);
        settingsPane.setDisable(true);

        aboutPane.setVisible(false);
        aboutPane.setDisable(true);

        show.setVisible(true);
        show.setDisable(false);
    }

    static private final String RESET_STYLE = "-fx-background-color: inherit;";
    static private final String SELECTED_STYLE = "-fx-background-color: #0087c6; -fx-font-weight: bold; ";
    void highlightNavigationButton(JFXButton button) {
        homeButton.setStyle(RESET_STYLE);
        settingsButton.setStyle(RESET_STYLE);
        aboutButton.setStyle(RESET_STYLE);
        
        button.setStyle(SELECTED_STYLE);
    }

    @FXML
    void showAbout(ActionEvent event) {
        hideAllPaneExcept(aboutPane);
        highlightNavigationButton(aboutButton);
    }

    @FXML
    void showHome(ActionEvent event) {
        hideAllPaneExcept(homePane);
        highlightNavigationButton(homeButton);
    }

    @FXML
    void showSettings(ActionEvent event) {
        hideAllPaneExcept(settingsPane);
        highlightNavigationButton(settingsButton);
    }

    @FXML
    void startReceive(ActionEvent event) {
        try {
            ReceiverConnection receiverConnection = JFXLoader.loadFXML("receiver-start");
            receiverConnection.setStage(getStage());
            receiverConnection.show("Receiver");
            receiverConnection.startReceiver(nameProperty.get(), receiveDirectoryProperty.get());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void startSend(ActionEvent event) {
        try {
            SenderConnection sender = JFXLoader.loadFXML("sender-start");
            sender.startSender(nameProperty.get(), receiveDirectoryProperty.get());
            sender.setStage(getStage());
            sender.show("Sender");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void checkUpdate(ActionEvent event) {
        System.out.println("Check Updates");
    }
}
