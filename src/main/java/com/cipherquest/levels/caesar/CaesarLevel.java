package com.cipherquest.levels.caesar;

import com.cipherquest.levels.BaseLevel;
import com.cipherquest.levels.ChallengeDefinition;

public class CaesarLevel extends BaseLevel {

    private String ciphertext;
    private String expectedPlaintext;
    private int shift;

    public CaesarLevel(int checkpointId) {
        super(checkpointId);
    }

    @Override
    public void loadChallenge() {
        ChallengeDefinition challenge = ChallengeDefinition.load(1);
        loadCommonFields(challenge);
        ciphertext = challenge.required("ciphertext");
        expectedPlaintext = challenge.required("answer");
        shift = Integer.parseInt(challenge.required("shift"));
    }

    @Override
    public boolean checkAnswer(String input) {
        recordAttempt();
        return input != null && input.trim().equalsIgnoreCase(expectedPlaintext);
    }

    @Override
    public ScreenType getScreenType() {
        return ScreenType.PASSKEY;
    }

    public String getCiphertext() {
        return ciphertext;
    }

    public int getShift() {
        return shift;
    }
}
