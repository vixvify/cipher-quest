package com.cipherquest.core;

/**
 * [INHERITANCE / POLYMORPHISM]
 * การคำนวณคะแนนแบบมาตรฐาน
 *
 * กฎการให้คะแนน:
 * - คะแนนฐานประจำด่าน: 100 คะแนน
 * - โบนัสตอบถูกตั้งแต่ครั้งแรก (First Try): +50 คะแนน
 * - โบนัสไม่ใช้คำใบ้ (No Hint Bonus): +50 คะแนน
 * - หากตอบผิดเกิน 1 ครั้ง: หัก 15 คะแนนต่อความพยายามที่ผิดพลาด (แต่ได้คะแนนไม่ต่ำกว่า 25)
 * - หากใช้คำใบ้: หัก 30 คะแนน
 */
public class DefaultScoreCalculator implements ScoreCalculator {

    private static final int BASE_SCORE = 100;
    private static final int FIRST_TRY_BONUS = 50;
    private static final int NO_HINT_BONUS = 50;
    private static final int MISTAKE_PENALTY = 15;
    private static final int HINT_PENALTY = 30;
    private static final int MINIMUM_SCORE = 25;

    @Override
    public int calculateScore(int checkpointId, int attempts, boolean hintUsed) {
        int earned = BASE_SCORE;

        // คำนวณโบนัส / ค่าปรับจากจำนวนครั้ง
        if (attempts <= 1) {
            earned += FIRST_TRY_BONUS;
        } else {
            int extraAttempts = attempts - 1;
            earned -= (extraAttempts * MISTAKE_PENALTY);
        }

        // คำนวณโบนัส / ค่าปรับจากการใช้ Hint
        if (!hintUsed) {
            earned += NO_HINT_BONUS;
        } else {
            earned -= HINT_PENALTY;
        }

        return Math.max(MINIMUM_SCORE, earned);
    }
}

