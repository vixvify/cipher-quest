package com.cipherquest.ui;

import com.cipherquest.Main;
import com.cipherquest.core.GameEngine;
import com.cipherquest.exploration.ArchiveWalk;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

class LobbyVerification {
    static final Main GAME = new Main();
    static Stage stage;

    public static void main(String[] args) throws Exception {
        Platform.startup(() -> Platform.setImplicitExit(false));
        try {
            double startedAt = fx(() -> {
                stage = new Stage();
                stage.setOpacity(0);
                stage.setScene(new Scene(new StackPane(), 900, 610));
                GAME.start(stage);
                return 0.0;
            });
            for (int attempt = 0; attempt < 40 && startedAt <= 30; attempt++) {
                Thread.sleep(100);
                startedAt = fx(() -> {
                    var player = music();
                    return player == null ? 0 : player.getCurrentTime().toMillis();
                });
            }
            require(startedAt > 30, "Lobby player should advance on Home");
            double homeTime = startedAt;
            fx(() -> {
                fire(root(), ".hero-button");
                station(1);
                require(root().lookup("#passkeyField") != null, "Enter first station without the card");
                return null;
            });
            Thread.sleep(500);
            fx(() -> {
                require(music().getStatus() == MediaPlayer.Status.PLAYING
                        && music().getCurrentTime().toMillis() > homeTime + 150, "Music should keep playing inside mission");
                key(KeyCode.ESCAPE);
                fire(root(), "#soundButton");
                return null;
            });
            Thread.sleep(350);
            fx(() -> {
                require(music().getStatus() == MediaPlayer.Status.PAUSED, "Mute pauses playback");
                fire(root(), "#soundButton");
                checkpoint(2);
                activity(2, "terminal-route");
                var radio = dialog();
                fire(radio, "#confirmFrequency");
                require(dialog() == radio, "Wrong frequency must keep activity open");
                ((Slider) radio.lookup("#radioDial")).setValue(97);
                fire(radio, "#confirmFrequency");
                require(dialog() == null && hasNote(2, "terminal-route"), "Radio awards evidence");
                station(2);
                require(root().lookup("#commandField") != null, "Terminal mission");
                key(KeyCode.ESCAPE);

                checkpoint(4);
                activity(4, "log-keyword");
                var circuit = dialog();
                fire(circuit, "#circuitTile4");
                require(dialog() == circuit && !hasNote(4, "log-keyword"), "Incomplete circuit awards nothing");
                fire(circuit, "#resetCircuit");
                fire(circuit, "#circuitTile4");
                fire(circuit, "#circuitTile8");
                require(dialog() == null && hasNote(4, "log-keyword"), "Solved circuit awards the keyword");
                station(4);
                root().applyCss();
                require(root().lookup("#queryArea") != null, "SQL mission");
                key(KeyCode.ESCAPE);

                checkpoint(6);
                activity(6, "core-tag");
                var match = dialog();
                fire(match, "#matchOption0");
                require(dialog() == match && !hasNote(6, "core-tag"), "Wrong mark awards nothing");
                fire(match, "#matchOption2");
                require(dialog() == null && hasNote(6, "core-tag"), "Matching awards evidence");

                checkpoint(7);
                activity(7, "vault-location");
                var map = dialog();
                for (int index : new int[]{1, 0, 2}) {
                    fire(map, "#sequencePiece" + index);
                }
                fire(map, "#confirmSequence");
                require(dialog() == null && hasNote(7, "vault-location"), "Assembling the vault map awards evidence");
                return null;
            });
            Thread.sleep(400);
            fx(() -> {
                require(music().getStatus() == MediaPlayer.Status.PLAYING, "Unmute resumes playback");
                System.out.println("Lobby playback and all four activity types passed.");
                return null;
            });
        } finally {
            fx(() -> {
                GAME.stop();
                if (stage != null) stage.close();
                return null;
            });
            Platform.exit();
        }
    }

    static MediaPlayer music() throws Exception {
        var field = GameAudio.class.getDeclaredField("music");
        field.setAccessible(true);
        return (MediaPlayer) field.get(null);
    }

    static void checkpoint(int checkpoint) {
        var player = GameEngine.getInstance().getPlayer();
        for (int completed = 1; completed < checkpoint; completed++) {
            player.markCheckpointCompleted(completed);
        }
        player.unlockCheckpoint(checkpoint);
        ScreenNavigator.navigateToArchive();
    }

    static boolean hasNote(int checkpoint, String name) {
        return ScreenNavigator.archiveWalk().collectedNotes(checkpoint).stream().anyMatch(note -> note.id().equals(name));
    }

    static void activity(int checkpoint, String name) throws Exception {
        var position = ScreenNavigator.archiveWalk().uncollectedNotes(checkpoint).stream()
                .filter(note -> note.id().equals(name)).findFirst().orElseThrow().position();
        place(position.x(), position.y());
        key(KeyCode.E);
        require(dialog() != null, "Activity appears for " + name);
    }

    static void station(int checkpoint) throws Exception {
        var station = ScreenNavigator.archiveWalk().map().stations().stream()
                .filter(it -> it.checkpoint() == checkpoint).findFirst().orElseThrow();
        place(station.column() + 1.5, station.row() + 0.5);
        key(KeyCode.E);
    }

    static void place(double x, double y) throws Exception {
        var column = ArchiveWalk.class.getDeclaredField("x");
        var row = ArchiveWalk.class.getDeclaredField("y");
        column.setAccessible(true);
        row.setAccessible(true);
        column.setDouble(ScreenNavigator.archiveWalk(), x);
        row.setDouble(ScreenNavigator.archiveWalk(), y);
    }

    static Parent root() {
        return stage.getScene().getRoot();
    }

    static void fire(Parent root, String selector) {
        root.applyCss();
        ((Button) root.lookup(selector)).fire();
    }

    static DialogPane dialog() {
        return Window.getWindows().stream().filter(Window::isShowing).map(Window::getScene)
                .map(Scene::getRoot).filter(DialogPane.class::isInstance).map(DialogPane.class::cast)
                .findFirst().orElse(null);
    }

    static void key(KeyCode code) {
        Event.fireEvent(root(), new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false));
        Event.fireEvent(root(), new KeyEvent(KeyEvent.KEY_RELEASED, "", "", code, false, false, false, false));
    }

    static <T> T fx(Callable<T> call) throws Exception {
        var task = new FutureTask<>(call);
        Platform.runLater(task);
        return task.get(15, TimeUnit.SECONDS);
    }

    static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
