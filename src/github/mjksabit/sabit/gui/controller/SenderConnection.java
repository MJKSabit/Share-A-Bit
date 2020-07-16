package github.mjksabit.sabit.gui.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXProgressBar;
import github.mjksabit.autoconnect.ServerDiscoveryObserver;
import github.mjksabit.autoconnect.SharedInfo;
import github.mjksabit.sabit.core.Sender;
import github.mjksabit.sabit.gui.JFXLoader;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SenderConnection extends Controller implements ServerDiscoveryObserver {

    ObservableList<String> files = FXCollections.observableArrayList();

    ObservableList<String> receivers = FXCollections.observableArrayList();
    Map<String, Sender.ReceiverInfo> servers = new HashMap<>();

    @FXML
    private Pane fileSelectionPane;

    @FXML
    private Pane receiverSelectionPane;

    @FXML
    private JFXProgressBar searchReceiverProgress;

    @FXML
    private ListView<String> fileList;

    @FXML
    private ListView<String> receiverList;

    @FXML
    private JFXButton refreshButton;

    private Sender sender = null;
    private String name;
    private String fileSaveDirectory;

    public void startSender(String name, String fileSaveDirectory) {
        fileList.setItems(files);
        receiverList.setItems(receivers);
        this.name = name;
        this.fileSaveDirectory = fileSaveDirectory;
        try {
            sender = new Sender(name, this);
        } catch (SocketException | JSONException e) {
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
        Sender.ReceiverInfo info = servers.getOrDefault(selectedReceiver, null);

        if (info == null) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Invalid Receiver");
            alert.setContentText("Please select the Receiver and then click [SEND]");
            alert.showAndWait();
        } else {
            try {
                Socket connectionSocket = new Socket(info.getAddress(), info.getPort());
                String receiver = sender.makeConnection(connectionSocket, name);
                Connected connected = JFXLoader.loadFXML("connected");
                connected.setFileSaveDirectory(fileSaveDirectory);
                connected.setStage(getStage());
                connected.setNames(name, receiver);
                connected.show("Share A BIT");
                connected.startConnection(sender, files);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void serverDiscovered(SharedInfo sharedInfo) {
        Sender.ReceiverInfo info = new Sender.ReceiverInfo(sharedInfo);
        Platform.runLater(() -> receivers.add(info.getName()));
        servers.put(info.getName(), info);
    }

    @FXML
    void senderSelection(ActionEvent event) {
        fileSelectionPane.setVisible(false);
        fileSelectionPane.setDisable(true);

        receiverSelectionPane.setVisible(true);
        receiverSelectionPane.setDisable(false);

        new Thread( () -> {
            Platform.runLater(() -> {
                searchReceiverProgress.setVisible(true);
                searchReceiverProgress.setProgress(JFXProgressBar.INDETERMINATE_PROGRESS);
                refreshButton.setDisable(true);
                receivers.clear();
            });

            try {
                sender.sendPresence();
            } catch (ConnectException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Platform.runLater(() -> {
                searchReceiverProgress.setVisible(false);
                refreshButton.setDisable(false);
            });
        }).start();
    }

    @FXML
    void cancelSending(ActionEvent event) {
        sender.stopListening();
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
