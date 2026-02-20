package toutouchien.niveriaapi.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringUtilsTest {
    @Test
    @DisplayName("Test capitalize method")
    void testCapitalize() {
        assertEquals("Hello", StringUtils.capitalize("hello"));
        assertEquals("World", StringUtils.capitalize("WORLD"));
        assertEquals("Niveria", StringUtils.capitalize("nIvErIa"));
        assertEquals("", StringUtils.capitalize(""));
    }

    @Test
    @DisplayName("Test pluralize methods")
    void testPluralizeMethods() {
        assertEquals("cats", StringUtils.pluralize("cat", 2));
        assertEquals("buses", StringUtils.pluralize("bus", "buses", 60));
        assertEquals("sheep", StringUtils.pluralize("sheep", 1));
        assertEquals("cow", StringUtils.pluralize("cow", 0));
        assertEquals("puppies", StringUtils.pluralize("puppy", "puppies", -1403));
    }
}
