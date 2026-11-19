package com.cipherquest.core;

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
            return Optional.of(new PasskeyBrief(level.getCiphertext(), level.getHint()));
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

    public record Mission(int checkpoint, int total, String title, String description, Level.ScreenType screenType) {}

    public record PasskeyBrief(String ciphertext, String hint) {}

    public record SqlBrief(String task, List<String> tables) {}

    public record SqlSubmission(AnswerResult answer, QueryResult result) {}

    public record TerminalSubmission(AnswerResult answer, boolean clearOutput) {}
}
