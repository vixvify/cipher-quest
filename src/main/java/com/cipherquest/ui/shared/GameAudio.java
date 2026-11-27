package com.cipherquest.ui.shared;

import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class GameAudio {
    private static final ExecutorService PLAYER = Executors.newSingleThreadExecutor(
            Thread.ofPlatform().name("cipherquest-audio").daemon().factory());
    private static final Map<Effect, Clip> effects = new EnumMap<>(Effect.class);
    private static final System.Logger LOGGER = System.getLogger(GameAudio.class.getName());
    private static MediaPlayer music;
    private static volatile boolean muted;

    private GameAudio() {
    }

    public static void start() {
        updateMusic();
    }

    public static void toggle(Button button) {
        muted = !muted;
        updateButton(button);
        updateMusic();
        PLAYER.execute(() -> {
            if (muted) {
                effects.values().stream().filter(clip -> clip != null).forEach(Clip::stop);
            }
        });
    }

    public static void updateButton(Button button) {
        String action = muted ? "เปิดเพลงและเสียงเอฟเฟกต์" : "ปิดเพลงและเสียงเอฟเฟกต์";
        button.setText(muted ? "เสียงปิด" : "เสียงเปิด");
        button.setAccessibleText(action);
        button.setTooltip(new Tooltip(action));
    }

    public static void play(Effect effect) {
        PLAYER.execute(() -> {
            if (muted) {
                return;
            }
            if (!effects.containsKey(effect)) {
                effects.put(effect, open(effect.filename));
            }
            Clip clip = effects.get(effect);
            if (clip != null) {
                clip.stop();
                clip.setFramePosition(0);
                clip.start();
            }
        });
    }

    private static void updateMusic() {
        if (muted) {
            if (music != null) {
                music.pause();
            }
            return;
        }
        if (music == null) {
            try {
                var resource = GameAudio.class.getResource("/audio/lobby.mp3");
                if (resource == null) {
                    LOGGER.log(System.Logger.Level.WARNING, "Missing lobby.mp3");
                    return;
                }
                music = new MediaPlayer(new Media(resource.toExternalForm()));
                music.setCycleCount(MediaPlayer.INDEFINITE);
                music.setVolume(0.48);
                MediaPlayer player = music;
                player.setOnError(() -> {
                    LOGGER.log(System.Logger.Level.WARNING, "Lobby music unavailable", player.getError());
                    player.dispose();
                    if (music == player) {
                        music = null;
                    }
                });
            } catch (MediaException | LinkageError error) {
                LOGGER.log(System.Logger.Level.WARNING, "Lobby music unavailable", error);
                return;
            }
        }
        music.play();
    }

    private static Clip open(String filename) {
        var resource = GameAudio.class.getResourceAsStream("/audio/" + filename);
        if (resource == null) {
            return null;
        }
        Clip clip = null;
        try (var buffer = new BufferedInputStream(resource); var stream = AudioSystem.getAudioInputStream(buffer)) {
            clip = AudioSystem.getClip();
            clip.open(stream);
            return clip;
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException
                | IllegalArgumentException error) {
            if (clip != null) {
                clip.close();
            }
            LOGGER.log(System.Logger.Level.DEBUG, "Audio unavailable", error);
            return null;
        }
    }

    public static void shutdown() {
        if (music != null) {
            music.dispose();
            music = null;
        }
        PLAYER.execute(() -> effects.values().stream().filter(clip -> clip != null).forEach(Clip::close));
        PLAYER.shutdown();
    }

    public enum Effect {
        CONNECT("connect.wav"), PICKUP("pickup.wav"), CLEAR("clear.wav"), DENIED("denied.wav");

        private final String filename;

        Effect(String filename) {
            this.filename = filename;
        }
    }
}
