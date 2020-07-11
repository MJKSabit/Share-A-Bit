package github.mjksabit.sabit.gui.model;

import com.jfoenix.controls.JFXProgressBar;
import github.mjksabit.sabit.core.ftp.IFTP;
import github.mjksabit.sabit.gui.JFXLoader;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import java.io.File;
import java.io.IOException;

public class FileNode extends AnchorPane implements IFTP.ProgressUpdater{

    private final static int bytePerMB = 1024*1024;

    private boolean sending = true;
    private long transferred = 0;
    private long fileSize = -1;

    private ObservableList<FileNode> list;
    private int index;

    public FileNode(boolean sending, ObservableList<FileNode> list) {
        this.sending = sending;
        this.list = list;

        try {
            FXMLLoader loader = JFXLoader.getFXMLLoader("file-list-cell");
            loader.setController(this);
            loader.setRoot(this);
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private Label fileNameLabel;

    @FXML
    private JFXProgressBar progressBar;

    @FXML
    private Label transferredSize;

    @FXML
    private Label totalFileSize;

    @FXML
    private ImageView upImage;

    @FXML
    private ImageView downImage;

    private String getInMB(long size) {
        return String.format("%.3f MB", (double) size / bytePerMB);
    }

    @Override
    public void startProgress(File file) {
        Platform.runLater(() -> {
            if (sending)
                upImage.setVisible(true);
            else
                downImage.setVisible(true);
            fileNameLabel.setText(file.getName());
            synchronized (list) {
                index = list.size();
                list.add(this);
            }
        });
    }

    @Override
    public void continueProgress(long currentProgress, long totalProgress) {
        if (fileSize == -1){
            fileSize = totalProgress;
            Platform.runLater( () -> totalFileSize.setText(getInMB(totalProgress)));
        }
        Platform.runLater(() -> {
            transferredSize.setText(getInMB(currentProgress));
            progressBar.setProgress((double) currentProgress/totalProgress);
        });
    }

    @Override
    public void cancelProgress(File file) {
        Platform.runLater(() -> {
            totalFileSize.setText("Canceled");
            progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        });
    }

    @Override
    public void endProgress(File file) {
        Platform.runLater(() -> {
            transferredSize.setText(getInMB(fileSize));
            progressBar.setProgress(1);

            synchronized (list) {
                list.remove(index);
                list.add(index,this);
            }
        });
    }
}
