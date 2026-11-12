package com.cipherquest;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/main-menu.fxml"));

        Scene scene = new Scene(loader.load(), 1280, 720);

        scene.getStylesheets().add(
                getClass().getResource("/css/main-menu.css")
                        .toExternalForm());

        stage.setTitle("CipherQuest");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}