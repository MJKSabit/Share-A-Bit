package github.mjksabit.sabit.gui.controller;

import github.mjksabit.autoconnect.ClientSide;
import github.mjksabit.autoconnect.ServerDiscoveryObserver;
import github.mjksabit.sabit.cli.Main;
import github.mjksabit.sabit.core.Sender;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SenderConnection extends Controller implements ServerDiscoveryObserver {

    ObservableList<String> files = FXCollections.observableArrayList();

    ObservableList<String> receivers = FXCollections.observableArrayList();
    Map<String, Sender.ServerInfo> servers = new HashMap<>();

    @FXML
    private ListView<String> fileList;

    @FXML
    private ListView<String> receiverList;

    private Sender sender = null;

    @FXML
    public void initialize() {
        fileList.setItems(files);
        receiverList.setItems(receivers);

        try {
            sender = new Sender(Start.name, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void addFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File(s) to Send ...");
        List<File> selectedFiles =  fileChooser.showOpenMultipleDialog(getStage());

        if (selectedFiles != null) for (File file : selectedFiles)
            files.add(file.getAbsolutePath());
    }

    @FXML
    void addFolder(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder to Send ...");
        File selectedFolder = directoryChooser.showDialog(getStage());
        if (selectedFolder != null)
            files.add(selectedFolder.getAbsolutePath());
    }

    @FXML
    void send(ActionEvent event) {
        String selectedReceiver = receiverList.getSelectionModel().getSelectedItem();
        Sender.ServerInfo info = servers.getOrDefault(selectedReceiver, null);

        if (info == null) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Invalid Receiver");
            alert.setContentText("Please select the Receiver and then click [SEND]");
            alert.showAndWait();
        } else {
            try {
                Socket connectionSocket = new Socket(info.getAddress(), info.getPort());
                sender.makeConnection(connectionSocket, Start.name);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void serverDiscovered(ClientSide.ServerInfo serverInfo) {
        Sender.ServerInfo info = new Sender.ServerInfo(serverInfo);
        receivers.add(info.getName());
        servers.put(info.getName(), info);
    }
}
