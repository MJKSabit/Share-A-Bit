package github.mjksabit.sabit.gui.controller;


import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import github.mjksabit.sabit.cli.Main;
import github.mjksabit.sabit.gui.FXMain;
import github.mjksabit.sabit.gui.JFXLoader;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Start extends Controller{
    private final StringProperty nameProperty = new SimpleStringProperty();
    private final StringProperty receiveDirectoryProperty = new SimpleStringProperty();

    @FXML
    private Pane topPane;

    @FXML
    private AnchorPane contentStack;

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
    private AnchorPane leftNavigation;

    @FXML
    private JFXButton homeButton;

    @FXML
    private JFXButton settingsButton;

    @FXML
    private JFXButton aboutButton;

    public void loadSettings(String name, String savePath) {
        nameText.setText(name);
        receiveDirectoryProperty.set(savePath);
    }

    @FXML
    public void saveSettings() {
        File settingsFile = new File(FXMain.SETTINGS_FILE);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(settingsFile))) {
            writer.write(nameProperty.get());
            writer.newLine();
            writer.write(receiveDirectoryProperty.get());
            writer.newLine();
            System.out.println("Settings Saved!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
        String link = "https://mjksabit.github.io/projects/share-a-bit/";
        System.out.println("Please Visit: "+link);
        try {
            Desktop.getDesktop().browse(new URI(link));
        } catch (IOException | URISyntaxException ignored) {

        }
    }
}
