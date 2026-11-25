package com.cipherquest;

import com.cipherquest.ui.ScreenNavigator;
import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        for (String weight : new String[]{"Regular", "Bold", "Black"}) {
            Font.loadFont(Main.class.getResource("/fonts/Kanit-" + weight + ".ttf").toExternalForm(), 14);
        }
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
