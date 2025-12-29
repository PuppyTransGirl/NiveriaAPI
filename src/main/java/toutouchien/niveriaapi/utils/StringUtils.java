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
     * Matches a string to an enum constant, ignoring case.
     *
     * @param key       The string to match
     * @param enumClass The enum class to match against
     * @param <T>       The type of the enum
     * @return An Optional containing the matched enum constant, or empty if no match is found
     */
    @NotNull
    public static <T extends Enum<T>> Optional<T> match(@Nullable String key, @NotNull Class<T> enumClass) {
        Preconditions.checkNotNull(enumClass, "enumClass cannot be null");
        if (key == null)
            return Optional.empty();

        try {
            return Optional.of(Enum.valueOf(enumClass, key.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Matches a string to an enum constant, ignoring case.
     *
     * @param key          The string to match
     * @param enumClass    The enum class to match against
     * @param defaultValue The default value to return if no match is found
     * @param <T>          The type of the enum
     * @return The matched enum constant, or the default value if no match is found
     */
    @Nullable
    public static <T extends Enum<T>> T match(@Nullable String key, @NotNull Class<T> enumClass, @Nullable T defaultValue) {
        Preconditions.checkNotNull(enumClass, "enumClass cannot be null");
        if (key == null)
            return defaultValue;

        try {
            return Enum.valueOf(enumClass, key.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
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
