package toutouchien.niveriaapi.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.mock.MockBukkitHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StringUtilsTest {
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
    @DisplayName("Test capitalize method")
    void testCapitalize() {
        assertEquals("Hello", StringUtils.capitalize("hello"));
        assertEquals("World", StringUtils.capitalize("WORLD"));
        assertEquals("Niveria", StringUtils.capitalize("nIvErIa"));
        assertEquals("", StringUtils.capitalize(""));
    }

    @Test
    @DisplayName("Test match methods")
    void testMatchMethods() {
        enum TestEnum { VALUE_ONE, VALUE_TWO, VALUE_THREE }

        assertEquals(TestEnum.VALUE_ONE, StringUtils.match("value_one", TestEnum.class).orElse(null));
        assertEquals(TestEnum.VALUE_TWO, StringUtils.match("VALUE_TWO", TestEnum.class).orElse(null));
        assertNull(StringUtils.match("invalid_value", TestEnum.class).orElse(null));
        assertEquals(TestEnum.VALUE_THREE, StringUtils.match("value_three", TestEnum.class, TestEnum.VALUE_ONE));
        assertEquals(TestEnum.VALUE_ONE, StringUtils.match("invalid_value", TestEnum.class, TestEnum.VALUE_ONE));
    }

    @Test
    @DisplayName("Test pluralize methods")
    void testPluralizeMethods() {
        assertEquals("cats", StringUtils.pluralize("cat", 2));
        assertEquals("buses", StringUtils.pluralize("bus", "buses", 60));
        assertEquals("sheep", StringUtils.pluralize("sheep", 1));
    }
}
