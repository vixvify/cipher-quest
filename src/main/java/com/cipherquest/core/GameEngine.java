package com.cipherquest.core;

import com.cipherquest.clues.Clue;
import com.cipherquest.levels.Level;
import com.cipherquest.levels.LevelManager;
import com.cipherquest.player.Player;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameEngine {

    private static GameEngine instance;

    private Player player;
    private GameState gameState;
    private int currentCheckpoint;
    private Level currentLevel;
    private boolean mainEndingUnlocked;

    private final LevelManager levelManager;
    private final ScoreCalculator scoreCalculator;
    private final CopyOnWriteArrayList<GameEventListener> eventListeners;
    private ProgressSaveManager saveManager;

    private static final int STARTING_CHECKPOINT = 1;

    private GameEngine() {
        this.levelManager = new LevelManager();
        this.scoreCalculator = new DefaultScoreCalculator();
        this.eventListeners = new CopyOnWriteArrayList<>();
        this.saveManager = new ProgressSaveManager();
        this.gameState = GameState.NOT_STARTED;
        this.mainEndingUnlocked = false;
    }

    public static synchronized GameEngine getInstance() {
        if (instance == null) {
            instance = new GameEngine();
        }
        return instance;
    }

    public void addListener(GameEventListener listener) {
        if (listener != null) {
            eventListeners.addIfAbsent(listener);
        }
    }

    public void removeListener(GameEventListener listener) {
        eventListeners.remove(listener);
    }

    public void clearListeners() {
        eventListeners.clear();
    }

    private void changeGameState(GameState newState) {
        if (this.gameState != newState) {
            GameState oldState = this.gameState;
            this.gameState = newState;
            for (GameEventListener listener : eventListeners) {
                listener.onGameStateChanged(oldState, newState);
            }
        }
    }

    public void startNewGame() {
        startNewGame("Agent");
    }

    public void startNewGame(String playerName) {
        this.player = new Player(playerName);
        this.player.unlockCheckpoint(STARTING_CHECKPOINT);
        this.mainEndingUnlocked = false;
        this.currentCheckpoint = STARTING_CHECKPOINT;
        loadCheckpoint(this.currentCheckpoint);
    }

    private void loadCheckpoint(int checkpointId) {
        if (this.currentLevel instanceof AutoCloseable closeable) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
        this.currentLevel = levelManager.createLevel(checkpointId);
        this.currentLevel.loadChallenge();
        if (this.player != null && this.player.hasCompletedCheckpoint(checkpointId)) {
            this.currentLevel.markCompleted();
        }
        changeGameState(GameState.PLAYING);

        for (GameEventListener listener : eventListeners) {
            listener.onLevelLoaded(this.currentLevel);
            listener.onScoreChanged(this.player.getScore(), 0);
        }
    }

    public AnswerResult submitAnswer(String input) {
        if (gameState != GameState.PLAYING || currentLevel == null || player == null) {
            return AnswerResult.error("เกมไม่ได้อยู่ในสถานะที่สามารถส่งคำตอบได้ในขณะนี้");
        }

        boolean isCorrect = currentLevel.checkAnswer(input);

        if (isCorrect) {
            boolean alreadyCompleted = player.hasCompletedCheckpoint(currentCheckpoint);
            currentLevel.markCompleted();
            player.markCheckpointCompleted(currentCheckpoint);

            if (currentCheckpoint < levelManager.getTotalCheckpoints()) {
                player.unlockCheckpoint(currentCheckpoint + 1);
            }

            int earnedScore = 0;
            if (!alreadyCompleted) {
                earnedScore = scoreCalculator.calculateScore(
                        currentCheckpoint,
                        currentLevel.getAttempts(),
                        currentLevel.hasUsedHint()
                );
                player.addScore(earnedScore);
            }

            for (GameEventListener listener : eventListeners) {
                listener.onScoreChanged(player.getScore(), earnedScore);
            }

            Optional<Clue> newClue = player.getClueArchive().unlockMainClueForCheckpoint(currentCheckpoint);
            if (newClue.isPresent()) {
                Clue unlocked = newClue.get();
                for (GameEventListener listener : eventListeners) {
                    listener.onClueDiscovered(unlocked);
                }
            }

            if (currentCheckpoint >= levelManager.getTotalCheckpoints()) {
                this.mainEndingUnlocked = true;
                saveProgress();
                changeGameState(GameState.VICTORY);
                AnswerResult result = AnswerResult.gameCleared(
                        "Main Story Completed — The True Lost Treasure: Memory Archive recovered! "
                                + "The Lost Treasure is not gold or physical wealth, but the complete memories, "
                                + "history, and accumulated knowledge of the ancient civilization.",
                        earnedScore,
                        player.getScore()
                );
                notifyAnswerEvaluated(result);
                for (GameEventListener listener : eventListeners) {
                    listener.onGameCompleted(player);
                }
                return result;
            } else {
                saveProgress();
                changeGameState(GameState.CHECKPOINT_CLEARED);
                AnswerResult result = AnswerResult.correct(
                        currentLevel.getSuccessFeedback(),
                        earnedScore,
                        player.getScore()
                );
                notifyAnswerEvaluated(result);
                return result;
            }
        } else {
            AnswerResult result = AnswerResult.wrong(
                    currentLevel.getFailureFeedback(),
                    player.getScore()
            );
            notifyAnswerEvaluated(result);
            return result;
        }
    }

    private void notifyAnswerEvaluated(AnswerResult result) {
        for (GameEventListener listener : eventListeners) {
            listener.onAnswerEvaluated(result);
        }
    }

    public boolean advanceToNextCheckpoint() {
        if (gameState != GameState.CHECKPOINT_CLEARED) {
            return false;
        }

        if (currentCheckpoint < levelManager.getTotalCheckpoints()) {
            currentCheckpoint++;
            loadCheckpoint(currentCheckpoint);
            saveProgress();
            return true;
        }
        return false;
    }

    public boolean selectCheckpoint(int checkpointId) {
        if (player == null || checkpointId < 1 || checkpointId > levelManager.getTotalCheckpoints()) {
            return false;
        }
        if (!isCheckpointUnlocked(checkpointId)) {
            return false;
        }
        this.currentCheckpoint = checkpointId;
        loadCheckpoint(checkpointId);
        return true;
    }

    public boolean isCheckpointUnlocked(int checkpointId) {
        if (player == null || checkpointId < 1 || checkpointId > levelManager.getTotalCheckpoints()) {
            return false;
        }
        return player.isCheckpointUnlocked(checkpointId);
    }

    public String requestHint() {
        if (currentLevel == null) {
            return "ไม่มีคำใบ้ในขณะนี้";
        }

        boolean firstTime = currentLevel.useHint();
        if (firstTime && player != null) {
            player.recordHintUsed();
        }

        String hint = currentLevel.getHint();
        for (GameEventListener listener : eventListeners) {
            listener.onHintUsed(hint);
        }
        return hint;
    }

    public void retryCurrentCheckpoint() {
        if (player != null && currentCheckpoint >= 1) {
            loadCheckpoint(currentCheckpoint);
        }
    }

    public void setSaveManager(ProgressSaveManager saveManager) {
        this.saveManager = Objects.requireNonNull(saveManager, "saveManager cannot be null");
    }

    public ProgressSaveManager getSaveManager() {
        return saveManager;
    }

    public boolean saveProgress() {
        return saveManager != null && saveManager.saveProgress(this);
    }

    public boolean loadSavedGame() {
        if (saveManager == null) {
            return false;
        }
        Optional<ProgressSaveManager.SaveData> dataOpt = saveManager.loadProgress();
        if (dataOpt.isEmpty()) {
            return false;
        }

        ProgressSaveManager.SaveData data = dataOpt.get();
        this.player = new Player(data.getUsername());
        this.player.restoreState(
                data.getScore(),
                data.getHintsUsed(),
                data.getCompletedCheckpoints(),
                data.getUnlockedCheckpoints(),
                data.getDiscoveredClueIds()
        );
        for (int cp : this.player.getCompletedCheckpoints()) {
            if (cp < levelManager.getTotalCheckpoints()) {
                this.player.unlockCheckpoint(cp + 1);
            }
        }
        this.mainEndingUnlocked = data.isMainEndingUnlocked()
                || this.player.hasCompletedCheckpoint(levelManager.getTotalCheckpoints());

        int targetCp = data.getCurrentCheckpoint();
        if (targetCp < 1 || targetCp > levelManager.getTotalCheckpoints() || !isCheckpointUnlocked(targetCp)) {
            targetCp = STARTING_CHECKPOINT;
        }
        this.currentCheckpoint = targetCp;
        loadCheckpoint(this.currentCheckpoint);
        return true;
    }

    public Player getPlayer() {
        return player;
    }

    public GameState getGameState() {
        return gameState;
    }

    public int getCurrentCheckpoint() {
        return currentCheckpoint;
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    public int getTotalCheckpoints() {
        return levelManager.getTotalCheckpoints();
    }

    public boolean isMainEndingUnlocked() {
        return mainEndingUnlocked;
    }
}
