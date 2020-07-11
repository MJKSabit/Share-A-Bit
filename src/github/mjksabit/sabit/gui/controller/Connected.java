package github.mjksabit.sabit.gui.controller;

import com.jfoenix.controls.JFXAlert;
import com.jfoenix.controls.JFXProgressBar;
import github.mjksabit.sabit.core.Connection;
import github.mjksabit.sabit.core.ftp.IFTP;
import github.mjksabit.sabit.gui.JFXLoader;
import github.mjksabit.sabit.gui.model.FileNode;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Connected extends Controller {
    Connection connection;
    String fileSaveDirectory = ".";

    final static int UPDATE_TIME = 1;
    volatile Long byteTransferredInTime = 0l;

    ExecutorService speedUpdaterThread = Executors.newSingleThreadExecutor();

    private final static int bytePerMB = 1024*1024;
    private String getInMB(long size) {
        return String.format("%.3f MB", (double) size / bytePerMB);
    }

    private class ReceiveProgress implements IFTP.ProgressUpdater {
        private long lastProgress = 0;
        private boolean firstTime = true;
        private long fileSize;

        @Override
        public void startProgress(File file) {
            Platform.runLater(() -> {
                receiveFileNameText.setText(file.getName());
                receivePane.setVisible(true);
            });
        }

        @Override
        public void continueProgress(long currentProgress, long totalProgress) {
            byteTransferredInTime += currentProgress - lastProgress;
            lastProgress = currentProgress;
            if (firstTime){
                firstTime = false;
                fileSize = totalProgress;
                Platform.runLater(()->receivedTotalInMB.setText(getInMB(totalProgress)));
            }
            double percentage = (double) currentProgress / totalProgress;
            String string = getInMB(currentProgress);
            Platform.runLater(() -> {
                receivedInMB.setText(string);
                receiveProgressBar.setProgress(percentage);
                receivedPercentage.setText(String.format("%.1f %%",percentage*100));
            });
        }

        @Override
        public void cancelProgress(File file) {
            Platform.runLater(() -> {
                receiveProgressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
                receiveFileNameText.setText("Canceled");
            });
        }

        @Override
        public void endProgress(File file) {
            byteTransferredInTime += fileSize - lastProgress;
            Platform.runLater(() -> {
                transmissionList.add(new FileNode(false, file.getName(), fileSize));
                receivePane.setVisible(false);
            });
        }
    }

    private class SendProgress implements IFTP.ProgressUpdater {
        private long lastProgress = 0;
        private int index;

        @Override
        public void startProgress(File file) {
            Platform.runLater(() -> {
                sendPane.setVisible(true);
                sendFileNameText.setText(file.getName());
                index = transmissionList.size();
                transmissionList.add(new FileNode(true, file.getName(), file.length()));
                sendTotalInMB.setText(getInMB(file.length()));
            });
        }

        @Override
        public void continueProgress(long currentProgress, long totalProgress) {
            byteTransferredInTime += currentProgress - lastProgress;
            lastProgress = currentProgress;
            double percentage = (double) currentProgress / totalProgress;
            String string = getInMB(currentProgress);
            Platform.runLater(() -> {
                sentInMB.setText(string);
                sendProgressBar.setProgress(percentage);
                sentPercentage.setText(String.format("%.1f %%",percentage*100));
            });
        }

        @Override
        public void cancelProgress(File file) {
            Platform.runLater(() -> {
                sendProgressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
                sendFileNameText.setText("Canceled");
            });
        }

        @Override
        public void endProgress(File file) {
            byteTransferredInTime += file.length() - lastProgress;
            Platform.runLater(() -> {
                transmissionList.get(index).markDone();
                sendPane.setVisible(false);
            });
        }
    }

    public void setFileSaveDirectory (String fileSaveDirectory) {
        this.fileSaveDirectory = fileSaveDirectory;
    }

    public void setNames(String myName, String otherName) {
        otherUser.setText(otherName);
    }

    public void startConnection(Connection connection, ObservableList<String> sendFiles) throws SocketException, FileNotFoundException {
        this.connection = connection;

        connection.setFileSaveDirectory(fileSaveDirectory);
        connection.startReceiving(new ReceiveProgress());

        speedUpdaterThread.execute(() -> {
            while (true) {
                try {
                    Thread.sleep(UPDATE_TIME*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                synchronized (byteTransferredInTime) {
                    long transferredNow = byteTransferredInTime;
                    byteTransferredInTime = 0l;
                    Platform.runLater(() -> speedText.setText(getInMB(transferredNow/UPDATE_TIME)));
                }
            }
        });

        for (String sendFile : sendFiles)
            connection.sendFile(new File(sendFile), new SendProgress());
    }

    ObservableList<FileNode> transmissionList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        transferFileList.setItems(transmissionList);
    }

    @FXML
    private ListView<FileNode> transferFileList;

    @FXML
    private Label otherUser;

    @FXML
    private Label speedText;

    @FXML
    private Pane sendPane;

    @FXML
    private Label sendFileNameText;

    @FXML
    private JFXProgressBar sendProgressBar;

    @FXML
    private Label sentInMB;

    @FXML
    private Label sendTotalInMB;

    @FXML
    private Label sentPercentage;

    @FXML
    private Pane receivePane;

    @FXML
    private Label receiveFileNameText;

    @FXML
    private JFXProgressBar receiveProgressBar;

    @FXML
    private Label receivedInMB;

    @FXML
    private Label receivedTotalInMB;

    @FXML
    private Label receivedPercentage;

    @FXML
    void endShare(ActionEvent event) {
        if (connection.isActive()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Still Transferring Files, Cancel on both sides!");
            alert.setHeaderText("Connection Active");
            alert.show();
        }
        else {
            try {
                connection.close();
                Start startPage = JFXLoader.loadFXML("start");
                startPage.setStage(getStage());
                startPage.show("Share A BIT");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void addFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File(s) to Send ...");
        List<File> selectedFiles =  fileChooser.showOpenMultipleDialog(getStage());

        if (selectedFiles != null) for (File file : selectedFiles) {
            try {
                connection.sendFile(file, new SendProgress());
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
                connection.sendFile(selectedFolder, new SendProgress());
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
