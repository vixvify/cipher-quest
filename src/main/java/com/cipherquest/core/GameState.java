package com.cipherquest.core;

/**
 * สถานะของวงจรชีวิตเกม (Game Lifecycle States)
 */
public enum GameState {
    /** เกมยังไม่เริ่ม (อยู่หน้า Main Menu) */
    NOT_STARTED,

    /** ผู้เล่นกำลังแก้ปริศนาในด่านปัจจุบัน */
    PLAYING,

    /** ผ่านด่านปัจจุบันแล้ว และพร้อมจะก้าวสู่ด่านถัดไป */
    CHECKPOINT_CLEARED,

    /** ผู้เล่นตอบผิดจนพลังชีวิตหมด (จบเกม) */
    GAME_OVER,

    /** ผู้เล่นเคลียร์ครบทั้ง 7 Checkpoint (ชนะเกม) */
    VICTORY
}

