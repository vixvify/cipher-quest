package com.cipherquest.ui;

import com.cipherquest.ui.shared.GameAudio;
import com.cipherquest.ui.shared.ScreenMotion;

import com.cipherquest.core.GameEngine;
import com.cipherquest.core.AnswerResult;
import com.cipherquest.core.GameSession;
import com.cipherquest.exploration.ArchiveMap;
import com.cipherquest.exploration.ArchiveWalk;
import javafx.animation.PauseTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class ScreenNavigator {

    private static final GameSession SESSION = new GameSession(GameEngine.getInstance());
    private static Stage primaryStage;
    private static ArchiveWalk archiveWalk;

    public static GameSession session() {
        return SESSION;
    }

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void startAudio() {
        GameAudio.start();
    }

    public static void stopAudio() {
        GameAudio.shutdown();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void navigateToMainMenu() {
        archiveWalk = null;
        loadScene("/fxml/main-menu.fxml", "/css/main-menu.css", "CipherQuest - Main Menu");
    }

    public static void startExploration() {
        SESSION.start();
        archiveWalk = new ArchiveWalk(ArchiveMap.load());
        navigateToArchive();
    }

    public static ArchiveWalk archiveWalk() {
        return archiveWalk;
    }

    public static void navigateToArchive() {
        if (archiveWalk == null) {
            navigateToMainMenu();
            return;
        }
        loadScene("/fxml/archive-screen.fxml", new String[]{"/css/monitor-shared.css", "/css/archive-screen.css"},
                "CipherQuest — Archive");
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

    public static String checkpointText(GameSession.Mission mission) {
        return "CHECKPOINT " + mission.checkpoint() + " / " + mission.total();
    }

    public static void navigateToVictoryScreen() {
        loadScene(
                "/fxml/victory-screen.fxml",
                new String[]{"/css/monitor-shared.css", "/css/victory-screen.css"},
                "CipherQuest — Mission Accomplished"
        );
    }

    public static void continueAfterFeedback(Node screenControl, AnswerResult answer, Duration delay) {
        PauseTransition pause = new PauseTransition(delay);
        pause.setOnFinished(event -> {
            if (screenControl.getScene() == null) {
                return;
            }
            if (answer.getStatus() == AnswerResult.Status.GAME_CLEARED) {
                navigateToVictoryScreen();
            } else if (SESSION.advance()) {
                if (archiveWalk == null) {
                    navigateToCurrentLevel();
                } else {
                    navigateToArchive();
                }
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
            if (!root.getStyleClass().contains("main-root")) {
                ScreenMotion.reveal(root, 7, 0.65, 240);
            }
        } catch (IOException error) {
            throw new IllegalStateException("Could not open screen " + fxmlPath, error);
        }
    }
}
