package com.cipherquest.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StrictScoreCalculatorTest {

    @Test
    void testCalculateScore() {
        ScoreCalculator calculator = new StrictScoreCalculator();

        assertEquals(100, calculator.calculateScore(1, 1, false));
        assertEquals(50, calculator.calculateScore(1, 1, true));
        assertEquals(75, calculator.calculateScore(1, 2, false));
        assertEquals(25, calculator.calculateScore(1, 2, true));
        assertEquals(0, calculator.calculateScore(1, 5, true));
    }
}
