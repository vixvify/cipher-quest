package com.cipherquest.levels.caesar;

import com.cipherquest.levels.BaseLevel;

public class CaesarLevel extends BaseLevel {

    private String ciphertext;
    private String expectedPlaintext;
    private int shift;

    public CaesarLevel(int checkpointId) {
        super(checkpointId);
    }

    @Override
    public void loadChallenge() {
        setTitle("Operation Caesar");
        setDescription("ถอดรหัสข้อความ Ciphertext 5 ตัวอักษร เพื่อค้นหา Passkey");
        setHint("ตัวเลข Shift อยู่ในช่วง 3 - 7");
        this.ciphertext = "VZJXY";
        this.expectedPlaintext = "QUEST";
        this.shift = 5;
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
