package com.cipherquest.ui;

import com.cipherquest.core.GameSession.CampaignProgress;
import com.cipherquest.core.GameSession.CheckpointStatus;
import com.cipherquest.exploration.ArchiveMap;
import com.cipherquest.exploration.ArchiveWalk;
import com.cipherquest.exploration.FieldNote;
import com.cipherquest.exploration.FieldActivity;
import com.cipherquest.exploration.RoomWorkshop;
import com.cipherquest.util.ResourceLoader;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

public class ArchiveController {
    private static final long FRAME_INTERVAL = 25_000_000;
    private static final Set<KeyCode> MOVEMENT_KEYS = Set.of(
            KeyCode.A, KeyCode.D, KeyCode.W, KeyCode.S, KeyCode.LEFT, KeyCode.RIGHT, KeyCode.UP, KeyCode.DOWN);

    @FXML private StackPane archiveRoot;
    @FXML private StackPane mapPane;
    @FXML private Canvas mapCanvas;
    @FXML private HBox progressTrack;
    @FXML private Label scoreLabel;
    @FXML private Button journalButton;
    @FXML private Label promptTitle;
    @FXML private Label promptDetail;
    @FXML private Button interactButton;
    @FXML private Button soundButton;

    private final Set<KeyCode> keys = EnumSet.noneOf(KeyCode.class);
    private final Properties stationNames = ResourceLoader.loadProperties("/data/exploration/stations.properties");
    private ArchiveWalk walk;
    private CampaignProgress progress;
    private Optional<ArchiveMap.Station> nearby = Optional.empty();
    private Optional<FieldNote> nearbyNote = Optional.empty();
    private int activeCheckpoint;
    private long lastFrame;
    private boolean interactPressed;
    private boolean workshopPaused;
    private ArchiveRenderer.Reveal reveal;

    @FXML
    private void initialize() {
        walk = ScreenNavigator.archiveWalk();
        if (walk == null) {
            walk = new ArchiveWalk(ArchiveMap.load());
        }
        progress = ScreenNavigator.SESSION.campaignProgress();
        activeCheckpoint = progress.checkpoints().indexOf(CheckpointStatus.AVAILABLE) + 1;
        nearby = walk.nearbyStation();
        nearbyNote = walk.nearbyNote(activeCheckpoint);
        showProgress();
        showEvidenceCount();
        showPrompt();
        GameAudio.updateButton(soundButton);

        mapCanvas.widthProperty().bind(mapPane.widthProperty().subtract(2));
        mapCanvas.heightProperty().bind(mapPane.heightProperty().subtract(2));
        mapCanvas.setLayoutX(1);
        mapCanvas.setLayoutY(1);
        mapPane.setOnMousePressed(event -> archiveRoot.requestFocus());
        archiveRoot.addEventFilter(KeyEvent.KEY_PRESSED, this::onKeyPressed);
        archiveRoot.addEventFilter(KeyEvent.KEY_RELEASED, this::onKeyReleased);
        archiveRoot.focusedProperty().addListener((property, before, focused) -> {
            if (!focused) {
                clearKeys();
            }
        });

        ArchiveRenderer renderer = new ArchiveRenderer(mapCanvas);
        AnimationTimer animation = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update(renderer, now);
            }
        };
        archiveRoot.sceneProperty().addListener((property, before, scene) -> {
            if (scene == null) {
                animation.stop();
                clearKeys();
                lastFrame = 0;
            } else {
                animation.start();
                Platform.runLater(archiveRoot::requestFocus);
            }
        });
    }

    private void update(ArchiveRenderer renderer, long now) {
        if (lastFrame != 0 && now - lastFrame < FRAME_INTERVAL) {
            return;
        }
        double step = lastFrame == 0 ? 0 : (now - lastFrame) / 1_000_000_000.0;
        lastFrame = now;
        int horizontal = axis(KeyCode.D, KeyCode.RIGHT) - axis(KeyCode.A, KeyCode.LEFT);
        int vertical = axis(KeyCode.S, KeyCode.DOWN) - axis(KeyCode.W, KeyCode.UP);
        walk.move(horizontal, vertical, step, checkpoint -> progress.status(checkpoint) != CheckpointStatus.LOCKED);
        Optional<ArchiveMap.Station> current = walk.nearbyStation();
        Optional<FieldNote> nextNote = walk.nearbyNote(activeCheckpoint);
        if (!current.equals(nearby) || !nextNote.equals(nearbyNote) || walk.workshop().isPresent()) {
            nearby = current;
            nearbyNote = nextNote;
            showPrompt();
        }
        if (!workshopPaused) {
            walk.updateWorkshop(step).ifPresent(this::showWorkshopReward);
        }
        renderer.draw(walk, progress, activeCheckpoint, nearby, nearbyNote, reveal, now / 1_000_000_000.0,
                Platform.getPreferences().isReducedMotion());
    }

    private int axis(KeyCode letter, KeyCode arrow) {
        return keys.contains(letter) || keys.contains(arrow) ? 1 : 0;
    }

    private void onKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        if (MOVEMENT_KEYS.contains(code)) {
            keys.add(code);
            event.consume();
        } else if (code == KeyCode.E) {
            interactPressed = true;
            event.consume();
        } else if (walk.workshop().isPresent() && code == KeyCode.R) {
            walk.resetWorkshop();
            clearKeys();
            showPrompt();
            event.consume();
        } else if (walk.workshop().isPresent() && code == KeyCode.ESCAPE) {
            walk.leaveWorkshop();
            clearKeys();
            showPrompt();
            event.consume();
        }
    }

    private void onKeyReleased(KeyEvent event) {
        KeyCode code = event.getCode();
        if (MOVEMENT_KEYS.contains(code)) {
            keys.remove(code);
            event.consume();
        } else if (code == KeyCode.E && interactPressed) {
            interactPressed = false;
            event.consume();
            onInteract();
        }
    }

    private void clearKeys() {
        keys.clear();
        interactPressed = false;
    }

    @FXML
    private void onInteract() {
        Optional<RoomWorkshop.Marker> nearbySwitch = walk.workshop().flatMap(room -> room.nearbySwitch(walk.position()));
        if (nearbySwitch.isPresent()) {
            if (walk.useWorkshopSwitch()) {
                GameAudio.play(GameAudio.Effect.CONNECT);
            }
            showPrompt();
            walk.updateWorkshop(0).ifPresent(this::showWorkshopReward);
            return;
        }
        Optional<FieldNote> note = walk.nearbyNote(activeCheckpoint);
        if (note.isPresent()) {
            FieldNote target = note.orElseThrow();
            clearKeys();
            if (target.activity() instanceof FieldActivity.Source) {
                if (completeNote(target, new FieldActivity.Attempt.Read())) {
                    workshopPaused = true;
                    FieldActivityDialog.showSource(interactButton, target, this::resumeWorkshop);
                }
            } else if (target.activity() instanceof FieldActivity.Workshop) {
                if (walk.startWorkshop(activeCheckpoint, target)) {
                    GameAudio.play(GameAudio.Effect.CONNECT);
                }
                showPrompt();
            } else {
                FieldActivityDialog.show(interactButton, target, walk.collectedNotes(activeCheckpoint),
                        attempt -> completeNote(target, attempt),
                        archiveRoot::requestFocus);
            }
            return;
        }
        walk.nearbyStation().ifPresent(station -> {
            if (ScreenNavigator.SESSION.enterCheckpoint(station.checkpoint())) {
                walk.leaveWorkshop();
                GameAudio.play(GameAudio.Effect.CONNECT);
                ScreenNavigator.navigateToCurrentLevel();
            }
        });
    }

    private boolean completeNote(FieldNote target, FieldActivity.Attempt attempt) {
        if (walk.completeNote(activeCheckpoint, target, attempt).isEmpty()) {
            return false;
        }
        nearbyNote = Optional.empty();
        reveal = new ArchiveRenderer.Reveal(target.position(), System.nanoTime() / 1_000_000_000.0);
        showPickup(target.title(), target.text());
        return true;
    }

    private void showPickup(String title, String detail) {
        GameAudio.play(GameAudio.Effect.PICKUP);
        showEvidenceCount();
        showPrompt();
        promptTitle.setText("บันทึกแล้ว · " + title);
        promptDetail.setText(ThaiTypography.withWordBreaks(detail));
        archiveRoot.requestFocus();
    }

    private void showWorkshopReward(FieldNote reward) {
        nearbyNote = walk.nearbyNote(activeCheckpoint);
        reveal = new ArchiveRenderer.Reveal(walk.position(), System.nanoTime() / 1_000_000_000.0);
        showPickup(reward.title(), reward.text());
    }

    private void resumeWorkshop() {
        workshopPaused = false;
        archiveRoot.requestFocus();
    }

    @FXML
    private void onToggleSound() {
        GameAudio.toggle(soundButton);
        archiveRoot.requestFocus();
    }

    @FXML
    private void onOpenJournal() {
        clearKeys();
        workshopPaused = true;
        MissionSupportDialog.showFieldNotes(journalButton, walk.collectedNotes(activeCheckpoint), this::resumeWorkshop);
    }

    private void showEvidenceCount() {
        int found = walk.collectedNotes(activeCheckpoint).size();
        int total = found + walk.uncollectedNotes(activeCheckpoint).size();
        journalButton.setVisible(total > 0);
        journalButton.setManaged(total > 0);
        journalButton.setText("สมุด " + found + "/" + total);
    }

    private void showProgress() {
        scoreLabel.setText(progress.score() + " คะแนน");
        for (int index = 0; index < progress.checkpoints().size(); index++) {
            CheckpointStatus status = progress.checkpoints().get(index);
            Label step = new Label(Integer.toString(index + 1));
            step.getStyleClass().add("archive-marker");
            Tooltip.install(step, new Tooltip(stationName(index + 1)));
            if (status == CheckpointStatus.AVAILABLE) {
                step.getStyleClass().add("available");
            } else if (status == CheckpointStatus.COMPLETED) {
                step.getStyleClass().add("completed");
            }
            progressTrack.getChildren().add(step);
        }
    }

    private void showPrompt() {
        var workshop = walk.workshop();
        var device = workshop.flatMap(room -> room.nearbySwitch(walk.position()));
        if (device.isPresent()) {
            showWorkshopPrompt(workshop.orElseThrow(), device);
            return;
        }
        if (nearbyNote.isPresent()) {
            FieldNote note = nearbyNote.orElseThrow();
            if (note.activity() instanceof FieldActivity.Workshop && workshop.isPresent()) {
                showWorkshopPrompt(workshop.orElseThrow(), Optional.empty());
                return;
            }
            promptTitle.setText(note.title());
            promptDetail.setText(note.activity() instanceof FieldActivity.Source
                    ? "เปิดอ่านและเก็บข้อมูลลงสมุดภาคสนาม" : "ใช้ข้อมูลภาคสนามประกอบกันเพื่อกู้เบาะแส");
            interactButton.setText(switch (note.activity()) {
                case FieldActivity.Source ignored -> "อ่านเอกสาร · E";
                case FieldActivity.RecordDesk ignored -> "เปิดโต๊ะตรวจ · E";
                case FieldActivity.Workshop ignored -> "เริ่มกิจกรรม · E";
            });
            interactButton.setDisable(false);
            return;
        }
        interactButton.setText("เปิดสถานี · E");
        if (nearby.isPresent()) {
            var station = nearby.orElseThrow();
            CheckpointStatus status = progress.status(station.checkpoint());
            promptTitle.setText(stationName(station.checkpoint()));
            promptDetail.setText(switch (status) {
                case LOCKED -> "สถานีล็อกอยู่ ต้องผ่านสถานีก่อนหน้า";
                case COMPLETED -> "ผ่านแล้ว · ไปต่อที่ " + nextStationName();
                case AVAILABLE -> "กด E เพื่อเปิดภารกิจ";
            });
            interactButton.setDisable(status != CheckpointStatus.AVAILABLE);
            return;
        }
        if (workshop.isPresent()) {
            showWorkshopPrompt(workshop.orElseThrow(), Optional.empty());
            return;
        }
        boolean cluesRemain = !walk.hasRecoveredClue(activeCheckpoint);
        promptTitle.setText("ไปที่ " + nextStationName());
        promptDetail.setText(cluesRemain
                ? "เอกสารสีทองให้ข้อมูล · โต๊ะสีฟ้าใช้กู้เบาะแส · สถานีสีเขียวเปิดภารกิจ"
                : "เดินด้วย WASD หรือปุ่มลูกศร");
        interactButton.setDisable(true);
    }

    private void showWorkshopPrompt(RoomWorkshop workshop, Optional<RoomWorkshop.Marker> device) {
        String title = switch (workshop.kind()) {
            case POWER -> "ระบบจ่ายไฟ";
            case CARGO -> "ลานเข็นลัง";
            case VAULT -> "จุดตรวจห้องนิรภัย";
        };
        promptTitle.setText(title + "  " + workshop.completed() + "/" + workshop.total());
        String guidance = switch (workshop.signal()) {
            case WRONG_ORDER -> "จุดนี้ยังรับสัญญาณไม่ได้ ลองดูขั้นตอนในสมุด";
            case SCANNER_RESET -> "ลำแสงล้างจุดตรวจแล้ว ใช้ทางด้านข้างหลบแนวสแกน";
            case BLOCKED -> "ลังติดสิ่งกีดขวาง ลองอ้อมไปเข็นอีกด้าน";
            default -> switch (workshop.kind()) {
                case POWER -> "เดินเข้าใกล้จุดแล้วกด E เพื่อต่อไฟ";
                case CARGO -> "เดินชนลังตรง ๆ เพื่อเข็นเข้าช่อง";
                case VAULT -> "หลบแนวสแกนแล้วกด E ที่จุดตรวจ";
            };
        };
        promptDetail.setText(ThaiTypography.withWordBreaks(guidance + " · R เริ่มใหม่ · Esc ออก"));
        interactButton.setText(device.map(marker -> "ใช้จุด " + marker.label() + " · E").orElse("สำรวจพื้นที่"));
        interactButton.setDisable(device.isEmpty());
    }

    private String nextStationName() {
        return activeCheckpoint > 0 ? stationName(activeCheckpoint) : "สถานีที่เปิดอยู่";
    }

    private String stationName(int checkpoint) {
        return stationNames.getProperty("station." + checkpoint, "สถานี " + checkpoint);
    }
}
