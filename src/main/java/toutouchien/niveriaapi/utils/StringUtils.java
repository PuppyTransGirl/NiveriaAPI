package toutouchien.niveriaapi.utils;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;

/**
 * Utility class providing static methods for string manipulations.
 */
public class StringUtils {
    private StringUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Capitalizes the first character of a string and converts the rest to lowercase.
     *
     * @param string The string to capitalize
     * @return The capitalized string, or the original string if null or blank
     */
    @NotNull
    public static String capitalize(@NotNull String string) {
        Preconditions.checkNotNull(string, "string cannot be null");

        if (string.isBlank())
            return string;

        return string.toUpperCase().charAt(0) + string.toLowerCase(Locale.ROOT).substring(1);
    }

    /**
     * Safely matches a string key to an enum constant.
     *
     * @param key       The string key to match
     * @param enumClass The enum class to search in
     * @param <T>       The enum type
     * @return An Optional containing the matched enum constant, or empty if no match found
     */
    @NotNull
    public static <T extends Enum<T>> Optional<T> match(@NotNull String key, @NotNull Class<T> enumClass) {
        Preconditions.checkNotNull(key, "key cannot be null");
        Preconditions.checkNotNull(enumClass, "enumClass cannot be null");

        try {
            return Optional.of(Enum.valueOf(enumClass, key.toUpperCase(Locale.ROOT)));
        } catch (NullPointerException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Safely matches a string key to an enum constant, returning a default value if no match is found.
     *
     * @param key          The string key to match
     * @param enumClass    The enum class to search in
     * @param <T>          The enum type
     * @param defaultValue The default value to return if no match is found
     * @return The matched enum constant, or the default value if no match found
     */
    public static <T extends Enum<T>> T match(@NotNull String key, @NotNull Class<T> enumClass, @Nullable T defaultValue) {
        Preconditions.checkNotNull(key, "key cannot be null");
        Preconditions.checkNotNull(enumClass, "enumClass cannot be null");

        try {
            return Enum.valueOf(enumClass, key.toUpperCase(Locale.ROOT));
        } catch (NullPointerException | IllegalArgumentException e) {
            return defaultValue;
        }
    }

    /**
     * Returns the plural form of a word based on the count.
     * Adds "s" to the singular form for pluralization.
     *
     * @param singular The singular form of the word
     * @param count    The count to determine singular or plural
     * @return The appropriate singular or plural form
     */
    @NotNull
    public static String pluralize(@NotNull String singular, int count) {
        Preconditions.checkNotNull(singular, "singular cannot be null");

        return pluralize(singular, singular + "s", count);
    }

    /**
     * Returns the appropriate form of a word based on the count.
     *
     * @param singular The singular form of the word
     * @param plural   The plural form of the word
     * @param count    The count to determine singular or plural
     * @return The appropriate singular or plural form
     */
    @NotNull
    public static String pluralize(@NotNull String singular, @NotNull String plural, int count) {
        Preconditions.checkNotNull(singular, "singular cannot be null");
        Preconditions.checkNotNull(plural, "plural cannot be null");

        return -1 <= count && count <= 1 ? singular : plural;
    }
}
