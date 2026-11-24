package com.cipherquest;

import com.cipherquest.ui.ScreenNavigator;
import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        Font.loadFont(Main.class.getResource("/fonts/Sarabun-Regular.ttf").toExternalForm(), 14);
        Font.loadFont(Main.class.getResource("/fonts/Sarabun-Bold.ttf").toExternalForm(), 14);
        stage.setMinWidth(700);
        stage.setMinHeight(560);
        ScreenNavigator.setPrimaryStage(stage);
        ScreenNavigator.navigateToMainMenu();
        stage.show();
        ScreenNavigator.startAudio();
    }

    @Override
    public void stop() {
        ScreenNavigator.stopAudio();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
