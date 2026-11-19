package com.cipherquest;

import com.cipherquest.ui.ScreenNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        stage.setMinWidth(700);
        stage.setMinHeight(560);
        ScreenNavigator.setPrimaryStage(stage);
        ScreenNavigator.navigateToMainMenu();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
