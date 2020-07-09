package github.mjksabit.sabit.gui.controller;

import github.mjksabit.sabit.core.Connection;
import github.mjksabit.sabit.core.ftp.IFTP;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class Connected extends Controller implements IFTP.ProgressUpdater {
    final int bytePerMB = 1024*1024;
    Connection connection;

    public void setNames(String myName, String otherName) {
        nameMy.setText(myName);
        nameOther.setText(otherName);
    }

    public void startConnection(Connection connection, ObservableList<String> sendFiles) throws SocketException, FileNotFoundException {
        this.connection = connection;
        receiveList.setItems(receiveFileList);

        connection.startReceiving(new IFTP.ProgressUpdater() {
            @Override
            public void startProgress(File file) {
                Platform.runLater(() -> {
                    receiveFileName.setText(file.getName());
                    receiveCurrentProgress.setText("0 MB");
                    receiveProgress.setProgress(0);
                    receiveTotalProgress.setText("Unknown");
                });
            }

            @Override
            public void continueProgress(long currentProgress, long totalProgress) {
                double received = (double) currentProgress / bytePerMB;
                double total = (double) totalProgress / bytePerMB;
                Platform.runLater(() -> {
                    receiveCurrentProgress.setText(String.format("%.3f MB", received));
                    receiveProgress.setProgress(received/total);
                    receiveTotalProgress.setText(String.format("%.3f MB", total));
                });
            }

            @Override
            public void cancelProgress(File file) {
                Platform.runLater(() -> {
                    receiveProgress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
                    receiveTotalProgress.setText("Canceled");
                });
            }

            @Override
            public void endProgress(File file) {
                receiveFileList.add(file.getAbsolutePath());
                continueProgress(file.length(), file.length());
            }
        });

        sendFileList = FXCollections.observableArrayList(sendFiles);
        sendList.setItems(sendFileList);
        for (int i=0; i<sendFiles.size(); i++)
            connection.sendFile(new File(sendFiles.get(i)), this);
    }

    @FXML
    private Label nameMy;

    @FXML
    private Label nameOther;

    ObservableList<String> receiveFileList = FXCollections.observableArrayList();

    @FXML
    private ListView<String> receiveList;

    @FXML
    private Label receiveFileName;

    @FXML
    private ProgressBar receiveProgress;

    @FXML
    private Label receiveCurrentProgress;

    @FXML
    private Label receiveTotalProgress;

    ObservableList<String> sendFileList;

    @FXML
    private ListView<String> sendList;

    @FXML
    private Label sendFileName;

    @FXML
    private ProgressBar sendProgress;

    @FXML
    private Label sendCurrentProgress;

    @FXML
    private Label sendTotalProgress;

    @FXML
    void sendFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File(s) to Send ...");
        List<File> selectedFiles =  fileChooser.showOpenMultipleDialog(getStage());

        if (selectedFiles != null) for (File file : selectedFiles) {
            try {
                connection.sendFile(file, this);
                sendFileList.add(file.getAbsolutePath());
            } catch (FileNotFoundException | SocketException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void sendFolder(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder to Send ...");
        File selectedFolder = directoryChooser.showDialog(getStage());
        if (selectedFolder != null) {
            try {
                connection.sendFile(selectedFolder, this);
                sendFileList.add(selectedFolder.getAbsolutePath());
            } catch (FileNotFoundException | SocketException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void startProgress(File file) {
        Platform.runLater(() -> {
            sendFileName.setText(file.getName());
            sendCurrentProgress.setText("0 MB");
            sendProgress.setProgress(0);
            sendTotalProgress.setText(String.format("%.3f MB", (double)file.length()/ bytePerMB));
        });
    }

    @Override
    public void continueProgress(long currentProgress, long totalProgress) {
        double received = (double) currentProgress / bytePerMB;
        double total = (double) totalProgress / bytePerMB;
        Platform.runLater(() -> {
            sendCurrentProgress.setText(String.format("%.3f MB", received));
            sendProgress.setProgress(received/total);
            sendTotalProgress.setText(String.format("%.3f MB", total));
        });
    }

    @Override
    public void cancelProgress(File file) {
        Platform.runLater(() -> {
            sendTotalProgress.setText("Canceled");
        });
    }

    @Override
    public void endProgress(File file) {
        Platform.runLater(() -> {
            continueProgress(file.length(), file.length());
        });
    }

    @FXML
    void cancelAll(ActionEvent event) {
        connection.cancelSending();
    }

    @FXML
    void cancelCurrent(ActionEvent event) {
        connection.cancelSendingCurrent();
    }
}
