package toutouchien.niveriaapi.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.mock.MockBukkitHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MathUtilsTest {
    @BeforeEach
    void setUp() {
        MockBukkitHelper.safeMock();
        MockBukkit.load(NiveriaAPI.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkitHelper.safeUnmock();
    }

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
