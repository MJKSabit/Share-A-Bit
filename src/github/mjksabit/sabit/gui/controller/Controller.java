package github.mjksabit.sabit.gui.controller;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

public abstract class Controller {
    private Node rootNode;

    public void setRootNode(Node rootNode) {
        this.rootNode = rootNode;
    }

    protected Scene getScene() {
        return rootNode.getScene();
    }

    public void setStage(Stage stage) {
        stage.setScene(getScene());
    }

    public Stage getStage() {
        return (Stage) getScene().getWindow();
    }

    public void show(String title) {
        if (getStage() == null) {
            setStage(new Stage());
        }

        getStage().setTitle(title);
        getStage().sizeToScene();
        getStage().show();
    }
}
