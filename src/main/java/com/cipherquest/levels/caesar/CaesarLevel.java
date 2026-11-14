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
        setDescription("ถอดรหัสข้อความที่ถูก encrypt ด้วย Caesar Cipher เพื่อค้นหา Passkey");
        setHint("ตัวอักษรแต่ละตัวถูกเลื่อนไปข้างหน้า 3 ตำแหน่ง ลองเลื่อนกลับ");
        this.ciphertext = "FLSKHUTZXHVW";
        this.expectedPlaintext = "CIPHERQUEST";
        this.shift = 3;
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
