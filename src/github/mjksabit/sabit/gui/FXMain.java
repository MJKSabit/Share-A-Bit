package github.mjksabit.sabit.gui;

import github.mjksabit.sabit.gui.controller.Start;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FXMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("./view/start.fxml"));
//        Parent root = loader.load();
//        Scene scene = new Scene(root);
//
//        primaryStage.setScene(scene);
//        primaryStage.show();
        Start startPage = JFXLoader.loadFXML("start");
        startPage.setStage(primaryStage);
        startPage.show("Hello World!");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
