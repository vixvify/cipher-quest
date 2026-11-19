package com.cipherquest.levels.linux;

import com.cipherquest.levels.BaseLevel;
import com.cipherquest.levels.ChallengeDefinition;

public class LinuxLevel extends BaseLevel implements AutoCloseable {

    private final VirtualTerminal terminal;

    public LinuxLevel(int checkpointId) {
        super(checkpointId);
        this.terminal = new VirtualTerminal();
    }

    @Override
    public void loadChallenge() {
        loadCommonFields(ChallengeDefinition.load(2));
        terminal.reset();
    }

    @Override
    public boolean checkAnswer(String input) {
        recordAttempt();
        terminal.execute(input);
        return terminal.isTargetExecuted();
    }

    @Override
    public String getSuccessFeedback() {
        return terminal.getLastOutput();
    }

    @Override
    public String getFailureFeedback() {
        return terminal.getLastOutput();
    }

    @Override
    public ScreenType getScreenType() {
        return ScreenType.TERMINAL;
    }

    public VirtualTerminal getTerminal() {
        return terminal;
    }

    @Override
    public void close() {
        terminal.close();
    }
}
