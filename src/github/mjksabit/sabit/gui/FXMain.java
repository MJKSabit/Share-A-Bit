package github.mjksabit.sabit.gui;

import github.mjksabit.sabit.gui.controller.Start;
import javafx.application.Application;
import javafx.stage.Stage;

public class FXMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Start startPage = JFXLoader.loadFXML("start");
        startPage.setStage(primaryStage);
        startPage.show("Hello World!");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
