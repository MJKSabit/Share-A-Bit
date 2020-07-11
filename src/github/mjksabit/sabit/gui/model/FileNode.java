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

public class FileNode extends AnchorPane {

    private final static int bytePerMB = 1024*1024;

    public FileNode(boolean sending, String filename, long fileSize) {
        try {
            FXMLLoader loader = JFXLoader.getFXMLLoader("file-list-cell");
            loader.setController(this);
            loader.setRoot(this);
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        fileNameLabel.setText(filename);
        totalFileSize.setText(getInMB(fileSize));
        if (sending)
            upImage.setVisible(true);
        else
            downImage.setVisible(true);
    }

    public void markDone() {
        this.setStyle("-fx-background-color: #53ff5c;");
    }

    @FXML
    private Label fileNameLabel;

    @FXML
    private Label totalFileSize;

    @FXML
    private ImageView upImage;

    @FXML
    private ImageView downImage;

    private String getInMB(long size) {
        return String.format("%.3f MB", (double) size / bytePerMB);
    }

}
