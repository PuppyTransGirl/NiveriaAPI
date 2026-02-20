package toutouchien.niveriaapi.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EnumUtilsTest {
    @Test
    @DisplayName("Test match with case-insensitive")
    void matchIsCaseInsensitive() {
        assertEquals(TestEnum.VALUE_ONE, EnumUtils.match("value_one", TestEnum.class).orElse(null));
        assertEquals(TestEnum.VALUE_TWO, EnumUtils.match("VALUE_TWO", TestEnum.class).orElse(null));
        assertEquals(TestEnum.value_four, EnumUtils.match("VALUE_FOUR", TestEnum.class).orElse(null));
    }

    @Test
    @DisplayName("Test match with unknown key")
    void matchReturnsEmptyForUnknownKey() {
        assertNull(EnumUtils.match("invalid_value", TestEnum.class).orElse(null));
    }

    @Test
    @DisplayName("Test match with with case-insensitive with default falls back")
    void matchWithDefaultReturnsMatchedValue() {
        assertEquals(TestEnum.VALUE_THREE, EnumUtils.match("value_three", TestEnum.class, TestEnum.VALUE_ONE));
    }

    @Test
    @DisplayName("Test match with default falls back when no match")
    void matchWithDefaultFallsBackWhenNoMatch() {
        assertEquals(TestEnum.VALUE_ONE, EnumUtils.match("invalid_value", TestEnum.class, TestEnum.VALUE_ONE));
    }

    @SuppressWarnings("java:S115")
    enum TestEnum {VALUE_ONE, VALUE_TWO, VALUE_THREE, value_four}
}
