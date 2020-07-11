package github.mjksabit.sabit.gui.controller;

import github.mjksabit.sabit.core.Connection;
import github.mjksabit.sabit.gui.model.FileNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.SocketException;
import java.util.List;

public class Connected extends Controller {
    Connection connection;
    String fileSaveDirectory = ".";

    public void setFileSaveDirectory (String fileSaveDirectory) {
        this.fileSaveDirectory = fileSaveDirectory;
    }

    public void setNames(String myName, String otherName) {
        otherUser.setText(otherName);
    }

    public void startConnection(Connection connection, ObservableList<String> sendFiles) throws SocketException, FileNotFoundException {
        this.connection = connection;

        connection.setFileSaveDirectory(fileSaveDirectory);

        connection.startReceiving(new FileNode(false, transmissionList));

        for (String sendFile : sendFiles)
            connection.sendFile(new File(sendFile), new FileNode(true, transmissionList));
    }

    ObservableList<FileNode> transmissionList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        transferFileList.setItems(transmissionList);
    }

    @FXML
    private ListView<FileNode> transferFileList;

    @FXML
    private Label speedText;

    @FXML
    private Label otherUser;

    @FXML
    void addFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File(s) to Send ...");
        List<File> selectedFiles =  fileChooser.showOpenMultipleDialog(getStage());

        if (selectedFiles != null) for (File file : selectedFiles) {
            try {
                connection.sendFile(file, new FileNode(true, transmissionList));
            } catch (FileNotFoundException | SocketException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void addFolder(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder to Send ...");
        File selectedFolder = directoryChooser.showDialog(getStage());
        if (selectedFolder != null) {
            try {
                connection.sendFile(selectedFolder, new FileNode(true, transmissionList));
            } catch (FileNotFoundException | SocketException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void cancelSendingAll(ActionEvent event) {
        connection.cancelSending();
        connection.cancelSendingCurrent();
    }
}
