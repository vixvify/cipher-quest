package com.cipherquest.ui;

import com.cipherquest.core.GameEngine;
import com.cipherquest.levels.Level;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ScreenNavigator {

    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void navigateToMainMenu() {
        loadScene("/fxml/main-menu.fxml", "/css/main-menu.css", "CipherQuest - Main Menu");
    }

    public static void navigateToCurrentLevel() {
        Level currentLevel = GameEngine.getInstance().getCurrentLevel();
        if (currentLevel == null) {
            navigateToMainMenu();
            return;
        }

        Level.ScreenType screenType = currentLevel.getScreenType();
        switch (screenType) {
            case PASSKEY -> loadScene(
                    "/fxml/passkey-screen.fxml",
                    new String[]{"/css/monitor-shared.css", "/css/passkey-screen.css"},
                    "CipherQuest - Checkpoint " + currentLevel.getCheckpointId() + " (Passkey)"
            );
            case TERMINAL -> loadScene(
                    "/fxml/terminal-screen.fxml",
                    new String[]{"/css/monitor-shared.css", "/css/terminal-screen.css"},
                    "CipherQuest - Checkpoint " + currentLevel.getCheckpointId() + " (Terminal)"
            );
            case SQL_WORKBENCH -> loadScene(
                    "/fxml/sql-workbench-screen.fxml",
                    new String[]{"/css/monitor-shared.css", "/css/sql-workbench-screen.css"},
                    "CipherQuest - Checkpoint " + currentLevel.getCheckpointId() + " (SQL Workbench)"
            );
        }
    }

    private static void loadScene(String fxmlPath, String cssPath, String title) {
        loadScene(fxmlPath, new String[]{cssPath}, title);
    }

    private static void loadScene(String fxmlPath, String[] cssPaths, String title) {
        if (primaryStage == null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(ScreenNavigator.class.getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene = primaryStage.getScene();
            if (scene == null) {
                scene = new Scene(root, 1280, 720);
                primaryStage.setScene(scene);
            } else {
                scene.setRoot(root);
            }

            scene.getStylesheets().clear();
            for (String css : cssPaths) {
                var resource = ScreenNavigator.class.getResource(css);
                if (resource != null) {
                    scene.getStylesheets().add(resource.toExternalForm());
                }
            }

            primaryStage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
