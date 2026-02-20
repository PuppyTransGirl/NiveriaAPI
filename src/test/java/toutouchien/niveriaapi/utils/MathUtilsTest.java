package toutouchien.niveriaapi.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MathUtilsTest {
    @Test
    @DisplayName("Decimal round double")
    void decimalRoundDouble() {
        assertEquals(1.23, MathUtils.decimalRound(1.23456, 2));
        assertEquals(1.235, MathUtils.decimalRound(1.23456, 3));
        assertEquals(-1.23, MathUtils.decimalRound(-1.23456, 2));
        assertEquals(-1.235, MathUtils.decimalRound(-1.23456, 3));
    }

    @Test
    @DisplayName("Decimal round float")
    void decimalRoundFloat() {
        assertEquals(1.23F, MathUtils.decimalRound(1.23456F, 2));
        assertEquals(1.235F, MathUtils.decimalRound(1.23456F, 3));
        assertEquals(-1.23F, MathUtils.decimalRound(-1.23456F, 2));
        assertEquals(-1.235F, MathUtils.decimalRound(-1.23456F, 3));
    }
}
