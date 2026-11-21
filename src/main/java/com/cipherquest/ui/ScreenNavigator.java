package com.cipherquest.ui;

import com.cipherquest.core.GameEngine;
import com.cipherquest.core.AnswerResult;
import com.cipherquest.core.GameSession;
import javafx.animation.PauseTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class ScreenNavigator {

    static final GameSession SESSION = new GameSession(GameEngine.getInstance());
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
        SESSION.currentMission().ifPresentOrElse(ScreenNavigator::showMission, ScreenNavigator::navigateToMainMenu);
    }

    private static void showMission(GameSession.Mission mission) {
        String title = "CipherQuest — " + mission.title();
        switch (mission.screenType()) {
            case PASSKEY -> loadScene(
                    "/fxml/passkey-screen.fxml",
                    new String[]{"/css/monitor-shared.css", "/css/passkey-screen.css"},
                    title
            );
            case TERMINAL -> loadScene(
                    "/fxml/terminal-screen.fxml",
                    new String[]{"/css/monitor-shared.css", "/css/terminal-screen.css"},
                    title
            );
            case SQL_WORKBENCH -> loadScene(
                    "/fxml/sql-workbench-screen.fxml",
                    new String[]{"/css/monitor-shared.css", "/css/sql-workbench-screen.css"},
                    title
            );
        }
    }

    static String checkpointText(GameSession.Mission mission) {
        return "CHECKPOINT " + mission.checkpoint() + " / " + mission.total();
    }

    public static void navigateToVictoryScreen() {
        loadScene(
                "/fxml/victory-screen.fxml",
                new String[]{"/css/monitor-shared.css", "/css/victory-screen.css"},
                "CipherQuest — Mission Accomplished"
        );
    }

    static void continueAfterFeedback(Node screenControl, AnswerResult answer, Duration delay) {
        PauseTransition pause = new PauseTransition(delay);
        pause.setOnFinished(event -> {
            if (screenControl.getScene() == null) {
                return;
            }
            if (answer.getStatus() == AnswerResult.Status.GAME_CLEARED) {
                navigateToVictoryScreen();
            } else if (SESSION.advance()) {
                navigateToCurrentLevel();
            }
        });
        pause.play();
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
        } catch (IOException ignored) {
        }
    }
}
