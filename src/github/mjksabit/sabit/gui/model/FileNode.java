package github.mjksabit.sabit.gui.model;

import github.mjksabit.sabit.gui.JFXLoader;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class FileNode extends AnchorPane {

    private final static int bytePerMB = 1024*1024;

    public FileNode(boolean sending, String filename, long fileSize) {
        FXMLLoader loader = JFXLoader.getFXMLLoader("file-list-cell");
        loader.setController(this);
        loader.setRoot(this);
        try {
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
        totalFileSize.setStyle("-fx-background-color: #8bff92; -fx-background-radius: 5px");
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
