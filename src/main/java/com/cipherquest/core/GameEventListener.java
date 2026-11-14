package com.cipherquest.core;

import com.cipherquest.levels.Level;
import com.cipherquest.player.Player;

/**
 * [ABSTRACTION - การแยกส่วนและลดการผูกมัด (Decoupling)]
 * อินเตอร์เฟสผู้ฟังเหตุการณ์ของเกม (Observer Pattern)
 *
 * ช่วยให้ Core Game Logic สื่อสารและส่งข้อมูลไปยัง UI Layer หรือระบบอื่นๆ ได้
 * โดยที่ GameEngine ไม่จำเป็นต้อง import หรือรู้จักคลาสของ JavaFX เลยแม้แต่บรรทัดเดียว
 */
public interface GameEventListener {

    /**
     * แจ้งเตือนเมื่อสถานะของเกมเปลี่ยน (เช่น NOT_STARTED -> PLAYING)
     */
    default void onGameStateChanged(GameState oldState, GameState newState) {}

    /**
     * แจ้งเตือนเมื่อโหลดด่านใหม่สำเร็จ
     */
    default void onLevelLoaded(Level level) {}

    /**
     * แจ้งเตือนผลการประเมินคำตอบ (ถูก / ผิด / ด่านจบ / เกมจบ)
     */
    default void onAnswerEvaluated(AnswerResult result) {}

    /**
     * แจ้งเตือนเมื่อคะแนนมีการเปลี่ยนแปลง
     */
    default void onScoreChanged(int currentScore, int delta) {}

    /**
     * แจ้งเตือนเมื่อพลังชีวิตลดลงหรือเพิ่มขึ้น
     */
    default void onLivesChanged(int remainingLives) {}

    /**
     * แจ้งเตือนเมื่อผู้เล่นกดขอคำใบ้
     */
    default void onHintUsed(String hint) {}

    /**
     * แจ้งเตือนเมื่อผู้เล่นผ่านครบทั้ง 7 Checkpoint (ชนะเกม)
     */
    default void onGameCompleted(Player player) {}

    /**
     * แจ้งเตือนเมื่อผู้เล่นตอบผิดจนพลังชีวิตหมด (แพ้เกม)
     */
    default void onGameOver(Player player) {}
}

