package com.cipherquest.core;

import com.cipherquest.clues.Clue;
import com.cipherquest.knowledge.SqlKnowledgeCenter;
import com.cipherquest.knowledge.SqlTopic;
import com.cipherquest.levels.Level;
import com.cipherquest.levels.caesar.CaesarLevel;
import com.cipherquest.levels.database.DatabaseLevel;
import com.cipherquest.levels.database.QueryResult;
import com.cipherquest.levels.linux.LinuxLevel;
import com.cipherquest.levels.linux.VirtualTerminal;

import java.util.List;
import java.util.Optional;

public final class GameSession {
    private final GameEngine engine;
    private final SqlKnowledgeCenter knowledgeCenter = new SqlKnowledgeCenter();

    public GameSession(GameEngine engine) {
        this.engine = engine;
    }

    public void start() {
        engine.startNewGame();
    }

    public Optional<Mission> currentMission() {
        return Optional.ofNullable(engine.getCurrentLevel()).map(level -> new Mission(
                level.getCheckpointId(), engine.getTotalCheckpoints(), level.getTitle(),
                level.getDescription(), level.getScreenType()));
    }

    public Optional<PasskeyBrief> passkeyBrief() {
        if (engine.getCurrentLevel() instanceof CaesarLevel level) {
            return Optional.of(new PasskeyBrief(level.getCiphertext()));
        }
        return Optional.empty();
    }

    public String terminalPath() {
        String home = VirtualTerminal.HOME;
        String path = engine.getCurrentLevel() instanceof LinuxLevel level
                ? level.getTerminal().getCurrentPath()
                : home;
        return path.equals(home) || path.startsWith(home + "/") ? "~" + path.substring(home.length()) : path;
    }

    public Optional<SqlBrief> sqlBrief() {
        if (engine.getCurrentLevel() instanceof DatabaseLevel level) {
            return Optional.of(new SqlBrief(level.getTask(), level.getDbEnvironment().getTableNames()));
        }
        return Optional.empty();
    }

    public AnswerResult submit(String input) {
        return engine.submitAnswer(input);
    }

    public TerminalSubmission runCommand(String command) {
        if (!(engine.getCurrentLevel() instanceof LinuxLevel level)) {
            return new TerminalSubmission(AnswerResult.error("ยังไม่ได้เปิดเทอร์มินัล"), false);
        }
        AnswerResult answer = submit(command);
        boolean clear = answer.getStatus() != AnswerResult.Status.ERROR && level.getTerminal().isClearRequested();
        return new TerminalSubmission(answer, clear);
    }

    public SqlSubmission runQuery(String query) {
        if (!(engine.getCurrentLevel() instanceof DatabaseLevel level)) {
            return new SqlSubmission(AnswerResult.error("ยังไม่ได้เปิดภารกิจ SQL"), null);
        }
        AnswerResult answer = submit(query);
        QueryResult result = answer.getStatus() == AnswerResult.Status.ERROR ? null : level.getLastQueryResult();
        return new SqlSubmission(answer, result);
    }

    public boolean advance() {
        return engine.advanceToNextCheckpoint();
    }

    public HintProgress hintProgress() {
        Level level = engine.getCurrentLevel();
        if (level == null) {
            return new HintProgress(List.of(), 0, false);
        }
        List<String> hints = level.getHints();
        int shown = Math.min(level.getHintLevel(), hints.size());
        boolean canReveal = engine.getGameState() == GameState.PLAYING && shown < hints.size();
        return new HintProgress(List.copyOf(hints.subList(0, shown)), hints.size(), canReveal);
    }

    public HintProgress revealHint() {
        if (hintProgress().canReveal()) {
            engine.requestHint();
        }
        return hintProgress();
    }

    public List<SqlTopic> currentSqlTopics() {
        return knowledgeCenter.findTopicsForLevel(engine.getCurrentLevel());
    }

    public List<Clue> discoveredClues() {
        if (engine.getPlayer() == null) {
            return List.of();
        }
        return engine.getPlayer().getClueArchive().getAllDiscoveredClues();
    }

    public Optional<Clue> currentMissionClue() {
        if (engine.getPlayer() == null) {
            return Optional.empty();
        }
        return engine.getPlayer().getClueArchive().findDiscoveredMainClue(engine.getCurrentCheckpoint());
    }

    public record Mission(int checkpoint, int total, String title, String description, Level.ScreenType screenType) {}

    public record PasskeyBrief(String ciphertext) {}

    public record HintProgress(List<String> revealed, int total, boolean canReveal) {}

    public record SqlBrief(String task, List<String> tables) {}

    public record SqlSubmission(AnswerResult answer, QueryResult result) {}

    public record TerminalSubmission(AnswerResult answer, boolean clearOutput) {}
}
