package com.dialog.dtg.integration.performance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SuitePerformanceIT {

    @Test
    void shouldExecuteSyntheticSuiteWithinBudget() {
        long start = System.currentTimeMillis();
        int total = 0;
        for (int i = 0; i < 50; i++) {
            total += i;
        }
        long elapsed = System.currentTimeMillis() - start;
        assertTrue(total > 0);
        assertTrue(elapsed < 2000);
    }
}
