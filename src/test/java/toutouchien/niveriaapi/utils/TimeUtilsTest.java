package toutouchien.niveriaapi.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.mock.MockBukkitHelper;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeUtilsTest {
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
    @DisplayName("Test parseMillis with various durations")
    void testParseMillis() {
        assertEquals("0 seconde", TimeUtils.parseMillis(500));
        assertEquals("1 seconde", TimeUtils.parseMillis(1000));
        assertEquals("1 minute 1 seconde", TimeUtils.parseMillis(61000));
        assertEquals("1 heure 1 minute 1 seconde", TimeUtils.parseMillis(3661000));
        assertEquals("1 jour 1 heure 1 minute 1 seconde", TimeUtils.parseMillis(90061000));
        assertEquals("1 semaine 1 jour 1 heure 1 minute 1 seconde", TimeUtils.parseMillis(694861000));
        assertEquals("11 mois 2 jours 1 minute 1 seconde", TimeUtils.parseMillis(26784061000L));
        assertEquals("1 an 1 mois 1 semaine 1 jour 1 minute 1 seconde", TimeUtils.parseMillis(32140861000L));
    }

    @Test
    @DisplayName("Test ticks conversion")
    void testTicks() {
        assertEquals(20L, TimeUtils.ticks(1000L, TimeUnit.MILLISECONDS));
        assertEquals(1200L, TimeUtils.ticks(Duration.ofMinutes(1)));
    }
}
