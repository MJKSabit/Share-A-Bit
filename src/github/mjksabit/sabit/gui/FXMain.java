package github.mjksabit.sabit.gui;

import github.mjksabit.sabit.gui.controller.Start;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FXMain extends Application {

    public static String SETTINGS_FILE = "settings.config";

    @Override
    public void start(Stage primaryStage) throws Exception {
        Start startPage = JFXLoader.loadFXML("start");
        startPage.setStage(primaryStage);

        File settingsFile = new File(SETTINGS_FILE);
        if (settingsFile.exists()) {
            try (BufferedReader settings = new BufferedReader(new FileReader(settingsFile))) {
                String name = settings.readLine();
                String savePath = settings.readLine();
                File saveDirectory = new File(savePath);
                if (saveDirectory.exists() && saveDirectory.isDirectory())
                    startPage.loadSettings(name, savePath);
                else
                    throw new IOException();
            } catch (IOException ignored) {
                System.out.println("Settings not Loaded!");
            }
        }

        startPage.show("Share A BIT");

        primaryStage.setOnCloseRequest(event -> System.exit(0));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
