package github.mjksabit.sabit.gui;

import github.mjksabit.sabit.gui.controller.Controller;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;

public class JFXLoader {
    public static <T extends Controller> T loadFXML(String fxmlName) throws IOException {
        String fxmlPath = "./view/" + fxmlName + ".fxml";
        FXMLLoader loader = new FXMLLoader(FXMain.class.getResource(fxmlPath));

        Parent rootNode = loader.load();
        Scene scene = new Scene(rootNode);
        T controller = loader.getController();
        controller.setRootNode(rootNode);

        return controller;
    }
}
